package com.codepath.apps.restclienttemplate.interactor;

import android.util.Log;

import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.model.Tweet;
import com.codepath.apps.restclienttemplate.tweetsources.TweetsSource;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Range;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.Sort;


/**
 * Retrieves and stores tweets for the timeline.
 * Also used for adding more tweets to the timeline.
 *
 * @author Joseph Gardi
 */
public class TimelineTweetsInteractor {

    private final String TAG = getClass().getName();
    private List<Tweet> tweets = new ArrayList<>();

    private TwitterClient twitterClient;
    private Realm realm;

    private PublishSubject<Integer> postedTweet = PublishSubject.create();
    private Observable<Range<Integer>> loadTweets;
    private Observable<Range<Integer>> loadTweetsFromDisk;
    private Observable<Range<Integer>> loadOlderTweets;


    public TimelineTweetsInteractor(TwitterClient client, final Realm realm, final TweetsSource tweetsSource) {
        this.twitterClient = client;
        this.realm = realm;

        final Function<String, List<Tweet>> parseAndCache = new Function<String, List<Tweet>>() {
            private ObjectMapper objectMapper = new ObjectMapper();

            @Override
            public List<Tweet> apply(@NonNull String json) throws Exception {
                final List<Tweet> parsedTweets = objectMapper.readValue(json,
                                                                        new TypeReference<List<Tweet>>() {});
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.insertOrUpdate(parsedTweets);
                    }
                });
                return parsedTweets;
            }
        };

        loadTweets = Observable.defer(new Callable<ObservableSource<? extends Range<Integer>>>() {
            @Override
            public ObservableSource<? extends Range<Integer>> call() throws Exception {
                RequestParams params = new RequestParams();
                params.put("count", 25);
                if (tweets.size() > 0) {
                    params.put("since_id", tweets.get(0).getId());
                } else {
                    params.put("since_id", 1);
                }

                return twitterClient.get(tweetsSource.getUrl(), params).map(parseAndCache)
                                    .map(new Function<List<Tweet>, Range<Integer>>() {
                    @Override
                    public Range<Integer> apply(@NonNull List<Tweet> parsedTweets) throws Exception {
                        int duplicateTweetIndex = indexOfDuplicate(tweets, 0, parsedTweets);

                        final List<Tweet> tweetsToInsert;
                        if (duplicateTweetIndex != -1) {
                            // a duplicate was found. Only add newer tweets
                            tweetsToInsert = parsedTweets.subList(0, duplicateTweetIndex);
                        } else {
                            // All the tweets are new. Add freely :)
                            tweetsToInsert = parsedTweets;
                        }

                        tweets.addAll(0, tweetsToInsert);

                        return Range.closedOpen(0, tweetsToInsert.size());
                    }
                });
            }
        });

        loadTweetsFromDisk = tweetsSource.getFromDisk()
                                         .flatMap(new Function<RealmQuery<Tweet>, Observable<Range<Integer>>>() {
            @Override
            public Observable<Range<Integer>> apply(@NonNull RealmQuery<Tweet> query) throws Exception {
                if (tweets.size() == 0) {
                    List<Tweet> tweetsFromDisk = sortAndLimitTweets(query);
                    tweets.addAll(tweetsFromDisk);
                    return Observable.just(Range.closedOpen(0, tweets.size()));
                } else {
                    return Observable.empty();
                }
            }
        });

        loadOlderTweets = tweetsSource.getFromDisk().flatMap(new Function<RealmQuery<Tweet>, Observable<Range<Integer>>>() {
            @Override
            public Observable<Range<Integer>> apply(@NonNull RealmQuery<Tweet> query) throws Exception {
                Log.i(TAG, "flat mapping");
                final int startingTweetsSize = tweets.size();

                RealmQuery<Tweet> fullQuery = query.lessThan("id", tweets.get(tweets.size() - 1).getId());
                List<Tweet> results = sortAndLimitTweets(fullQuery);
                if (results.size() > 0) {
                    tweets.addAll(results);
                    return Observable.just(Range.closedOpen(startingTweetsSize, tweets.size()));
                } else {
                    RequestParams params = new RequestParams();
                    params.put("count", 25);
                    Log.i(TAG, "max is " + getTweets().get(getTweets().size() - 1).getId() + ", " + getTweets().get(getTweets().size() - 1).getText());
                    params.put("max_id", getTweets().get(getTweets().size() - 1).getId());

                    return twitterClient.get(tweetsSource.getUrl(), params).map(parseAndCache)
                                        .map(new Function<List<Tweet>, Range<Integer>>() {
                        @Override
                        public Range<Integer> apply(@NonNull List<Tweet> parsedTweets) throws Exception {
                            // I need to avoid putting duplicate tweets
                            // So I get the index of the latest duplicate and only insert tweets from after that
                            int duplicateTweetIndex = indexOfDuplicate(tweets, tweets.size() - 1, parsedTweets);
                            if (parsedTweets.size() == 0) {
                                Log.i(TAG, "new tweet is : " + parsedTweets.get(0).getId());
                            }

                            Log.i(TAG, "duplicate: " + duplicateTweetIndex + ", " + parsedTweets.size());
                            final List<Tweet> tweetsToInsert;
                            if (duplicateTweetIndex != -1 && duplicateTweetIndex + 1 < parsedTweets.size()) {
                                // A duplicate was found. Only insert tweets from after that duplicate
                                tweetsToInsert = parsedTweets.subList(duplicateTweetIndex + 1, parsedTweets.size());
                            } else {
                                // no duplicate found. Add freely :)
                                tweetsToInsert = parsedTweets;
                            }

                            tweets.addAll(tweetsToInsert);

                            return Range.closedOpen(tweets.size() - tweetsToInsert.size(), tweets.size());
                        }
                    });
                }
            }
        });
    }


    public void savePostedTweet(final Tweet tweet) {
        tweets.add(0, tweet);
        Log.i(TAG, "posting");
        postedTweet.onNext(42);

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(tweet);
            }
        });
    }


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

            if (duplicateTweetIndex < tweets.size() && duplicateTweetIndex < tweetsToSearch.size()) {
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
}
