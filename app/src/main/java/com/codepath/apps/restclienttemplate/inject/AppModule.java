package com.codepath.apps.restclienttemplate.inject;

import android.app.Application;
import android.content.Context;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TimelineView;
import com.codepath.apps.restclienttemplate.TweetsAdapter;
import com.codepath.apps.restclienttemplate.interactor.CurrentUserInteractor;
import com.codepath.apps.restclienttemplate.interactor.PostTweetInteractor;
import com.codepath.apps.restclienttemplate.interactor.TimelineTweetsInteractor;
import com.codepath.apps.restclienttemplate.interactor.TwitterClientInteractor;

import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;

/**
 * @author Joseph Gardi
 */
@Module
public class AppModule {

    private final Application app;

    public AppModule(Application app) {
        this.app = app;
    }


    @Provides
    @Singleton
    public Context provideContext() {
        return app;
    }

    @Provides
    @Singleton
    public TwitterClientInteractor newClient(Context context) {
        return new TwitterClientInteractor(context);
    }


    @Provides
    @Named("homeTimelineSource")
    @Singleton
    public TweetsAdapter homeAdapterFactory(final SimpleDateFormat format, @Named("homeTimelineSource") final TimelineTweetsInteractor timelineTweets) {
            return new TweetsAdapter(timelineTweets, format);
    }

    @Provides
    @Named("mentionsTimelineSource")
    @Singleton
    public TweetsAdapter mentionsAdapterFactory(final SimpleDateFormat format, @Named("mentionsTimelineSource") final TimelineTweetsInteractor timelineTweets) {
        return new TweetsAdapter(timelineTweets, format);
    }

    @Provides
    @Named("homeTimelineSource")
    @Singleton
    public TimelineTweetsInteractor newTi(TwitterClientInteractor client, Realm realm, CurrentUserInteractor currentUserInteractor) {
        return new TimelineTweetsInteractor(client, realm, currentUserInteractor, app.getString(R.string.homeTimelineSource));
    }

    @Provides
    @Named("homeTimelineSource")
    public TimelineView newHTiV(Context context,
                                @Named("homeTimelineSource") TweetsAdapter tweetsAdapter,
                                @Named("homeTimelineSource") TimelineTweetsInteractor timelineTweetsInteractor) {
        return new TimelineView(context, tweetsAdapter, timelineTweetsInteractor);
    }

    @Provides
    @Named("mentionsTimelineSource")
    public TimelineView newMTiV(Context context,
                                @Named("mentionsTimelineSource") TweetsAdapter tweetsAdapter,
                                @Named("mentionsTimelineSource") TimelineTweetsInteractor timelineTweetsInteractor) {
        return new TimelineView(context, tweetsAdapter, timelineTweetsInteractor);
    }

    @Provides
    @Named("mentionsTimelineSource")
    @Singleton
    public TimelineTweetsInteractor newMent(TwitterClientInteractor client, Realm realm, CurrentUserInteractor currentUserInteractor) {
        return new TimelineTweetsInteractor(client, realm, currentUserInteractor, app.getString(R.string.mentionsTimelineSource));
    }

    @Provides
    @Singleton
    public Realm defaultInstance(Context context) {
        Realm.init(context);
        return Realm.getDefaultInstance();
    }

    @Provides
    @Singleton
    public SimpleDateFormat twitterDateFormat() {
        return new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH);
    }

    @Provides
    @Singleton
    public PostTweetInteractor newTweeter(TwitterClientInteractor twitterClient, @Named("homeTimelineSource") TimelineTweetsInteractor timelineTweets) {
        return new PostTweetInteractor(twitterClient, timelineTweets);
    }
}
