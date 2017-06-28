package com.codepath.apps.restclienttemplate;

import android.app.Application;
import android.content.Context;

import com.codepath.apps.restclienttemplate.interactor.TwitterInteractor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

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
    public TwitterInteractor newTi(TwitterClient client) {
        return new TwitterInteractor(client);
    }


    @Provides
    @Singleton
    public TweetAdapter newAdapter(TwitterInteractor interactor) {
        return new TweetAdapter(interactor);
    }
}
