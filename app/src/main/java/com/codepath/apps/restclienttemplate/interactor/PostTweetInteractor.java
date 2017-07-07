package com.codepath.apps.restclienttemplate.interactor;

import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.model.Tweet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.RequestParams;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * @author Joseph Gardi
 */
public class PostTweetInteractor {

    private String TAG = getClass().getName();

    private TwitterClient twitterClient;
    private TimelineTweetsInteractor timelineTweetsInteractor;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    public PostTweetInteractor(TwitterClient twitterClient, TimelineTweetsInteractor timelineTweetsInteractor) {
        this.twitterClient = twitterClient;
        this.timelineTweetsInteractor = timelineTweetsInteractor;
    }


    public Observable<Tweet> postTweet(final String status) {
        RequestParams params = new RequestParams();
        params.put("status", status);
        return twitterClient.post("statuses/update.json", params)
                            .map(new Function<String, Tweet>() {
            @Override
            public Tweet apply(@NonNull String response) throws Exception {
                final Tweet tweet = objectMapper.readValue(response.toString(), Tweet.class);
                timelineTweetsInteractor.savePostedTweet(tweet);
                return tweet;
            }
        })
                .cache();
    }
}
