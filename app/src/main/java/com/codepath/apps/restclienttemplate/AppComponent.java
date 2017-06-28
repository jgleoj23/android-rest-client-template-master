package com.codepath.apps.restclienttemplate;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author Joseph Gardi
 */
@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

    void inject(TimelineActivity act);
    void inject(TweetActivity act);
}
