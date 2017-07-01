package com.codepath.apps.restclienttemplate;

import android.app.Application;
import android.content.Context;

import com.codepath.apps.restclienttemplate.interactor.TwitterInteractor;

import java.text.SimpleDateFormat;
import java.util.Locale;

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
    public TwitterClient newClient(Context context) {
        return new TwitterClient(context);
    }


    @Provides
    @Singleton
    public TwitterInteractor newTi(TwitterClient client, Context context, Realm realm, SimpleDateFormat twitterDateFormat) {
        return new TwitterInteractor(client, context, realm, twitterDateFormat);
    }


    @Provides
    @Singleton
    public TweetAdapter newAdapter(TwitterInteractor interactor, SimpleDateFormat twitterDateFormat) {
        return new TweetAdapter(interactor, twitterDateFormat);
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
}
