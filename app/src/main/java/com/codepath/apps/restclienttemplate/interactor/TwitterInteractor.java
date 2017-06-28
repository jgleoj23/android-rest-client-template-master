package com.codepath.apps.restclienttemplate.interactor;

import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.model.Tweet;
import com.codepath.apps.restclienttemplate.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import cz.msebera.android.httpclient.Header;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.subjects.PublishSubject;


/**
 * @author Joseph Gardi
 */
public class TwitterInteractor {
    private final String TAG = getClass().getName();
    private TwitterClient twitterClient;
    private List<Tweet> tweets = new ArrayList<>();
    private ObjectMapper objectMapper = new ObjectMapper();
    private PublishSubject<List<Tweet>> tweetsChange = PublishSubject.create();

    @Inject
    public TwitterInteractor(TwitterClient client) {
        this.twitterClient = client;
    }


    public Observable<User> getUser() {
        return user;
    }

    public Observable<List<Tweet>> getHomeTimeline() {
        return homeTimeline;
    }

    public List<Tweet> getTweets() {
        return tweets;
    }


    private Observable<List<Tweet>> homeTimeline = Observable.create(new ObservableOnSubscribe<List<Tweet>>() {

        @Override
        public void subscribe(@NonNull final ObservableEmitter<List<Tweet>> emitter) throws Exception {
            String apiUrl = twitterClient.getApiUrl("statuses/home_timeline.json");

            RequestParams params = new RequestParams();
            params.put("count", 25);
            params.put("since_id", 1);

            twitterClient.get(apiUrl, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    try {
                        List<Tweet> parsedTweets = objectMapper.readValue(response.toString(), new TypeReference<List<Tweet>>() {});
                        tweets.addAll(parsedTweets);
                        emitter.onNext(tweets);
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                }
            });
        }
    })
            .mergeWith(tweetsChange)
            .share();



    private Observable<User> user = Observable.create(new ObservableOnSubscribe<User>() {
        @Override
        public void subscribe(@NonNull final ObservableEmitter<User> emitter) throws Exception {
            twitterClient.get(twitterClient.getApiUrl("account/verify_credentials.json"), null, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        emitter.onNext(objectMapper.readValue(response.toString(), User.class));
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
                            Tweet tweet = objectMapper.readValue(response.toString(), Tweet.class);
                            tweets.add(0, tweet);
                            tweetsChange.onNext(tweets);
                            emitter.onNext(tweet);
                        } catch (Exception exception) {
                            emitter.onError(exception);
                        }
                    }
                });
            }
        })
                .share();
    }
}
