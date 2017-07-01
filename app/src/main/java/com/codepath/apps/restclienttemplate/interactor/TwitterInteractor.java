package com.codepath.apps.restclienttemplate.interactor;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.model.Tweet;
import com.codepath.apps.restclienttemplate.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Range;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import cz.msebera.android.httpclient.Header;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;


/**
 * @author Joseph Gardi
 */
public class TwitterInteractor {
    private final String TAG = getClass().getName();

    private TwitterClient twitterClient;
    private Context context;
    private Realm realm;
    private SimpleDateFormat twitterDateFormat;

    private List<Tweet> tweets = new ArrayList<>();
    private ObjectMapper objectMapper = new ObjectMapper();
    private PublishSubject<Range<Integer>> newTweets = PublishSubject.create();

    @Inject
    public TwitterInteractor(TwitterClient client, Context context, Realm realm, SimpleDateFormat twitterDateFormat) {
        this.twitterClient = client;
        this.context = context;
        this.realm = realm;
        this.twitterDateFormat = twitterDateFormat;
    }


    public Observable<User> getUser() {
        return user;
    }

    public Observable<Range<Integer>> getHomeTimeline() {
        return homeTimeline;
    }

    public List<Tweet> getTweets() {
        return tweets;
    }


    private Observable<Range<Integer>> homeTimeline = Observable.create(new ObservableOnSubscribe<Range<Integer>>() {

        @Override
        public void subscribe(@NonNull final ObservableEmitter<Range<Integer>> emitter) throws Exception {
            if (isNetworkAvailable()) {
                String apiUrl = twitterClient.getApiUrl("statuses/home_timeline.json");

                RequestParams params = new RequestParams();
                params.put("count", 25);
                params.put("since_id", 1);

                try {
                    twitterClient.get(apiUrl, params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            loadFromJson(response).subscribe(new Consumer<Range<Integer>>() {
                                @Override
                                public void accept(@NonNull Range<Integer> range) throws Exception {
                                    emitter.onNext(range);
                                }
                            });
                        }
                    });
                } catch (Exception exception) {
                    loadTimelineFromDisk(emitter);

                    exception.printStackTrace();
                }
            } else {
                loadTimelineFromDisk(emitter);
            }
        }
    })
            .mergeWith(newTweets)
            .share();

    private void loadTimelineFromDisk(ObservableEmitter<Range<Integer>> emitter) {
        RealmResults<Tweet> results = realm.where(Tweet.class).findAllSorted("createdAt", Sort.DESCENDING);
        insertTweets(results, false);
        emitter.onNext(Range.closedOpen(tweets.size() - results.size(), tweets.size()));
    }


    private Observable<User> user = Observable.create(new ObservableOnSubscribe<User>() {
        @Override
        public void subscribe(@NonNull final ObservableEmitter<User> emitter) throws Exception {
            twitterClient.get(twitterClient.getApiUrl("account/verify_credentials.json"), null, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        final User user = objectMapper.readValue(response.toString(), User.class);
                        user.setIsCurrentUser(true);

                        realm.beginTransaction();
                        realm.insertOrUpdate(user);
                        realm.commitTransaction();

                        emitter.onNext(user);
                    } catch (Exception exception) {
                        emitter.onError(exception);
                    }
                }
            });
        }
    })
            .share();





    public Observable<Tweet> postTweet(final String status) {

        return Observable.create(new ObservableOnSubscribe<Tweet>() {
            @Override
            public void subscribe(@NonNull final ObservableEmitter<Tweet> emitter) throws Exception {
                String apiUrl = twitterClient.getApiUrl("statuses/update.json");

                RequestParams params = new RequestParams();
                params.put("status", status);

                twitterClient.post(apiUrl, params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            final Tweet tweet = objectMapper.readValue(response.toString(), Tweet.class);
                            tweets.add(0, tweet);
                            realm.executeTransactionAsync(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    Log.i(TAG, "saving new tweet to realm");
                                    realm.insertOrUpdate(tweet);
                                }
                            });

                            newTweets.onNext(Range.closed(0, 0));
                            emitter.onNext(tweet);
                        } catch (IOException e) {
                            emitter.onError(e);
                        }
                    }
                });
            }
        })
                .share();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void loadMoreTweet() {
        String apiUrl = twitterClient.getApiUrl("statuses/home_timeline.json");

        RequestParams params = new RequestParams();
        params.put("max_id", getTweets().get(getTweets().size() - 1).getId());

        twitterClient.get(apiUrl, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.i(TAG, "received: " + response.length());
                loadFromJson(response).subscribe(newTweets);
            }
        });
    }


    private Observable<Range<Integer>> loadFromJson(final JSONArray json) {
        Log.i(TAG, "loading");

        return Observable.create(new ObservableOnSubscribe<Range<Integer>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Range<Integer>> emitter) throws Exception {
                Log.i(TAG, json.toString());
                final List<Tweet> parsedTweets = objectMapper.readValue(json.toString(),
                        new TypeReference<List<Tweet>>() {});


                insertTweets(parsedTweets, true);

                emitter.onNext(Range.closedOpen(tweets.size() - parsedTweets.size(), tweets.size()));
            }
        });
    }

    private void insertTweets(List<Tweet> newTweets, boolean shouldOverwrite) {
        for (int i = 0; i < tweets.size(); i++) {
            for (int j = 0; j < newTweets.size(); j++) {
                if (tweets.get(i).getId() == newTweets.get(j).getId()) {
                    Log.i(TAG, "found a duplicate");
                    if (shouldOverwrite) {
                        tweets.set(i, newTweets.get(j));
                    }

                    newTweets.remove(j);


                    break;
                }
            }
        }

        // any newTweets left did not have matches in tweets
        // So safely add them.
        tweets.addAll(newTweets);
        Collections.sort(tweets, new Comparator<Tweet>() {
            @Override
            public int compare(Tweet tweet1, Tweet tweet2) {
                try {
                    Date date1 = twitterDateFormat.parse(tweet1.getCreatedAt());
                    Date date2 = twitterDateFormat.parse(tweet2.getCreatedAt());

                    return date1.compareTo(date2);
                } catch (ParseException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        });

        realm.beginTransaction();
        realm.insertOrUpdate(tweets);
        realm.commitTransaction();
    }
}
