package com.codepath.apps.restclienttemplate.interactor;

import android.util.Log;

import com.codepath.apps.restclienttemplate.model.Tweet;
import com.codepath.apps.restclienttemplate.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Range;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.Sort;

import static com.codepath.apps.restclienttemplate.StackPrinter.stackPrinter;


/**
 * Retrieves and stores tweets for the timeline.
 * Also used for adding more tweets to the timeline.
 *
 * @author Joseph Gardi
 */
public class TimelineTweetsInteractor {

    private final String TAG = getClass().getName();

    private String source;
    private TwitterClientInteractor twitterClient;
    private Realm realm;
    private CurrentUserInteractor currentUserInteractor;

    private ObjectMapper objectMapper = new ObjectMapper();

    private List<Tweet> tweets = new ArrayList<>();

    private PublishSubject<Integer> postedTweet = PublishSubject.create();


    private Observable<Range<Integer>> loadTweets = Observable.create(new ObservableOnSubscribe<Range<Integer>>() {
        @Override
        public void subscribe(@NonNull final ObservableEmitter<Range<Integer>> emitter) throws Exception {
            RequestParams params = new RequestParams();
            params.put("count", 25);
            if (tweets.size() > 0) {
                params.put("since_id", tweets.get(0).getId());
            } else {
                params.put("since_id", 1);
            }

            twitterClient.get(source, params).subscribe(new Consumer<String>() {
                @Override
                public void accept(@NonNull String response) throws Exception {
                    final List<Tweet> parsedTweets = objectMapper.readValue(response,
                                                                            new TypeReference<List<Tweet>>() {
                                                                            });
                    Log.i(TAG, "adding from disk: " + parsedTweets.get(0).getText() + ", " + parsedTweets.get(0).getUser().getScreenName());
                    int duplicateTweetIndex = indexOfDuplicate(tweets, 0, parsedTweets);

                    final List<Tweet> tweetsToInsert;
                    if (duplicateTweetIndex != -1) {
                        // a duplicate was found. Only add newer tweets
                        tweetsToInsert = parsedTweets.subList(0, duplicateTweetIndex);
                    } else {
                        // All the tweets are new. Add freely :)
                        tweetsToInsert = parsedTweets;
                    }

                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.insertOrUpdate(tweetsToInsert);
                        }
                    });

                    tweets.addAll(0, tweetsToInsert);
                    emitter.onNext(Range.closedOpen(0, tweetsToInsert.size()));
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(@NonNull Throwable throwable) throws Exception {
                    emitter.onError(throwable);
                }
            });
        }
    });


    private Observable<Range<Integer>> loadTweetsFromDisk = Observable.create(new ObservableOnSubscribe<Range<Integer>>() {
        @Override
        public void subscribe(@NonNull final ObservableEmitter<Range<Integer>> emitter) throws Exception {
            tweetsQuery.subscribe(new Consumer<RealmQuery<Tweet>>() {
                @Override
                public void accept(@NonNull RealmQuery<Tweet> query) throws Exception {
                    if (tweets.size() == 0) {
                        List<Tweet> tweetsFromDisk = sortAndLimitTweets(query);
                        tweets.addAll(tweetsFromDisk);
                        emitter.onNext(Range.closedOpen(0, tweets.size()));
                    }
                }
            });
        }
    });


    private Observable<Range<Integer>> loadOlderTweets = Observable.create(new ObservableOnSubscribe<Range<Integer>>() {
        @Override
        public void subscribe(@NonNull final ObservableEmitter<Range<Integer>> emitter) throws Exception {
            final int startingTweetsSize = tweets.size();
            tweetsQuery.subscribe(new Consumer<RealmQuery<Tweet>>() {
                @Override
                public void accept(@NonNull RealmQuery<Tweet> query) throws Exception {
                    RealmQuery<Tweet> fullQuery = query.lessThan("id", tweets.get(tweets.size() - 1).getId());
                    List<Tweet> results = sortAndLimitTweets(fullQuery);
                    if (results.size() > 0) {
                        tweets.addAll(results);
                        emitter.onNext(Range.closedOpen(startingTweetsSize, tweets.size()));
                    } else {
                        RequestParams params = new RequestParams();
                        params.put("max_id", getTweets().get(getTweets().size() - 1).getId());
                        twitterClient.get(source, params).subscribe(new Consumer<String>() {
                            @Override
                            public void accept(@NonNull String response) throws Exception {
                                Log.i(TAG, "received: " + response.length());
                                final List<Tweet> parsedTweets = objectMapper.readValue(response,
                                                                                        new TypeReference<List<Tweet>>() {});

                                // I need to avoid putting duplicate tweets
                                // So I get the index of the latest duplicate and only insert tweets from after that
                                int duplicateTweetIndex = indexOfDuplicate(tweets, tweets.size() - 1, parsedTweets);

                                Log.i(TAG, "duplicate: " + duplicateTweetIndex);
                                final List<Tweet> tweetsToInsert;
                                if (duplicateTweetIndex != -1) {
                                    // A duplicate was found. Only insert tweets from after that duplicate
                                    tweetsToInsert = parsedTweets.subList(duplicateTweetIndex + 1, parsedTweets.size());
                                } else {
                                    // no duplicate found. Add freely :)
                                    tweetsToInsert = parsedTweets;
                                }

                                realm.executeTransactionAsync(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        realm.insertOrUpdate(tweetsToInsert);
                                    }
                                });
                                tweets.addAll(tweetsToInsert);
                                emitter.onNext(Range.closedOpen(tweets.size() - tweetsToInsert.size(), tweets.size()));
                            }
                        });
                    }
                }
            });
        }
    });


    public TimelineTweetsInteractor(TwitterClientInteractor client, Realm realm, CurrentUserInteractor currentUserInteractor, String source) {
        this.twitterClient = client;
        this.realm = realm;
        this.source = source;
        this.currentUserInteractor = currentUserInteractor;
    }


    public void savePostedTweet(final Tweet tweet, int index) {
        tweets.add(index, tweet);
        postedTweet.onNext(tweets.size() - 1);


        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insert(tweet);
            }
        });
    }


    /**
     * Find the index of the first tweet in {@code tweetsToSearch} that has
     * the same id as {@code tweets.get(index) }
     * @return -1 if the tweet is not found or {@code index} is out of bounds.
     *  If the tweet is found return it's index in tweetsToSearch
     */
    private int indexOfDuplicate(List<Tweet> tweets, int index, List<Tweet> tweetsToSearch) {
        if (index < tweets.size()) {
            long id = tweets.get(index).getId();

            int duplicateTweetIndex = 0;
            while (duplicateTweetIndex < tweetsToSearch.size() &&
                    tweetsToSearch.get(duplicateTweetIndex).getId() != id) {
                duplicateTweetIndex++;
            }

            if (duplicateTweetIndex < tweets.size()) {
                return duplicateTweetIndex;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    private List<Tweet> sortAndLimitTweets(RealmQuery<Tweet> query) {
        List < Tweet > tweets = query.findAllSorted("id", Sort.DESCENDING);
        if (tweets.size() > 25) {
            return tweets.subList(0, 25);
        } else {
            return tweets;
        }
    }

    private Observable<RealmQuery<Tweet>> tweetsQuery = Observable.create(new ObservableOnSubscribe<RealmQuery<Tweet>>() {
        @Override
        public void subscribe(@NonNull final ObservableEmitter<RealmQuery<Tweet>> emitter) throws Exception {
            if (source.contains("mention")) {
                Log.i(TAG, "mentions");
                currentUserInteractor.getUser().subscribe(new Consumer<User>() {
                    @Override
                    public void accept(@NonNull User user) throws Exception {
                        emitter.onNext(realm.where(Tweet.class).contains("text", "@" + user.getScreenName()));
                    }
                }, stackPrinter);
            } else {
                emitter.onNext(realm.where(Tweet.class));
            }
        }
    });



    public Observable<Integer> getPostedTweet() {
        return postedTweet;
    }

    public Observable<Range<Integer>> getLoadTweets() {
        return loadTweets;
    }

    public Observable<Range<Integer>> getLoadOlderTweets() {
        return loadOlderTweets;
    }

    public List<Tweet> getTweets() {
        return tweets;
    }

    public Observable<Range<Integer>> getLoadTweetsFromDisk() {
        return loadTweetsFromDisk;
    }
}
