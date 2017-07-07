package com.codepath.apps.restclienttemplate.inject;

import android.app.Application;
import android.content.Context;

import com.codepath.apps.restclienttemplate.TweetsAdapter;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.fragments.TimelineFragment;
import com.codepath.apps.restclienttemplate.interactor.PostTweetInteractor;
import com.codepath.apps.restclienttemplate.interactor.TimelineTweetsInteractor;
import com.codepath.apps.restclienttemplate.interactor.UserInteractor;
import com.codepath.apps.restclienttemplate.model.User;
import com.codepath.apps.restclienttemplate.tweetsources.HomeTimelineSource;
import com.codepath.apps.restclienttemplate.tweetsources.MentionsSource;
import com.codepath.apps.restclienttemplate.tweetsources.TweetsSource;
import com.codepath.apps.restclienttemplate.tweetsources.UserTweetsSource;
import com.google.common.base.Function;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.annotation.Nullable;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.annotations.NonNull;
import io.realm.Realm;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

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
    public TwitterClient newClient(Context context) {
        return new TwitterClient(context);
    }

    @Provides
    @HomeTimeline
    @Singleton
    public TimelineTweetsInteractor newTi(TwitterClient client, Realm realm, UserInteractor userInteractor, HomeTimelineSource source) {
        return new TimelineTweetsInteractor(client, realm, source);
    }

    @Provides
    @MentionsTimeline
    @Singleton
    public TimelineTweetsInteractor mentionsIn(TwitterClient client, Realm realm, MentionsSource mentionsSource) {
        return new TimelineTweetsInteractor(client, realm, mentionsSource);
    }

    @Provides
    @Singleton
    public Function<User, TimelineTweetsInteractor> tweetsInteractorFactory(final TwitterClient client,
                                                                            final Realm realm, final Function<User,TweetsSource> sourceFactory) {
        return new Function<User, TimelineTweetsInteractor>() {
            @Nullable
            @Override
            public TimelineTweetsInteractor apply(@Nullable User input) {
                return new TimelineTweetsInteractor(client, realm, sourceFactory.apply(input));
            }
        };
    }

    @Provides
    @Singleton
    public MentionsSource mTS(Realm realm, Context context, UserInteractor userInteractor) {
        return new MentionsSource(realm, context, userInteractor);
    }

    @Provides
    @Singleton
    public HomeTimelineSource hTS(Realm realm, Context context) {
        return new HomeTimelineSource(realm, context);
    }


    @Provides
    @HomeTimeline
    @Singleton
    public TimelineFragment homeTimeline(@HomeTimeline TimelineTweetsInteractor timelineTweetsInteractor) {
        return TimelineFragment.newInstance(timelineTweetsInteractor);
    }

    @Provides
    @MentionsTimeline
    @Singleton
    public TimelineFragment mentionsTimeline(@MentionsTimeline TimelineTweetsInteractor timelineTweetsInteractor) {
        return TimelineFragment.newInstance(timelineTweetsInteractor);
    }

    @Provides
    @Singleton
    public Function<User, TimelineFragment> timelineFragmentFactory(final Function<User, TimelineTweetsInteractor> tweetsInteractorFactory) {
        return new Function<User, TimelineFragment>() {
            @Nullable
            @Override
            public TimelineFragment apply(@Nullable User user) {
                return TimelineFragment.newInstance(tweetsInteractorFactory.apply(user));
            }
        };
    }

    @Provides
    @Singleton
    public Function<User, TweetsSource> uTS2(final Realm realm, final Context context) {
        return new Function<User, TweetsSource>() {
            @Override
            public TweetsSource apply(@NonNull User user) {
                return new UserTweetsSource(realm, context, user);
            }
        };
    }

    @Provides
    @Singleton
    public Realm defaultInstance(Context context) {
        Realm.init(context);
        return Realm.getDefaultInstance();
    }

    @Provides
    @Singleton
    public Function<TimelineTweetsInteractor, TweetsAdapter> tweetsAdapterFactory(final SimpleDateFormat twitterDateFormat) {
        return new Function<TimelineTweetsInteractor, TweetsAdapter>() {
            @Override
            public TweetsAdapter apply(@NonNull TimelineTweetsInteractor timelineTweetsInteractor) {
                return new TweetsAdapter(timelineTweetsInteractor, twitterDateFormat);
            }
        };
    }

    @Provides
    @Singleton
    public SimpleDateFormat twitterDateFormat() {
        return new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH);
    }

    @Provides
    @Singleton
    public PostTweetInteractor newTweeter(TwitterClient twitterClient, @HomeTimeline TimelineTweetsInteractor timelineTweets) {
        return new PostTweetInteractor(twitterClient, timelineTweets);
    }

    @Qualifier
    @Documented
    @Retention(RUNTIME)
    public @interface HomeTimeline {}
    @Qualifier
    @Documented
    @Retention(RUNTIME)
    public @interface MentionsTimeline{}
}
