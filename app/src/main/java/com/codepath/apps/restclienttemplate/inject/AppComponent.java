package com.codepath.apps.restclienttemplate.inject;

import com.codepath.apps.restclienttemplate.activity.TimelineActivity;
import com.codepath.apps.restclienttemplate.activity.TweetActivity;
import com.codepath.apps.restclienttemplate.fragments.HomeTimelineFragment;
import com.codepath.apps.restclienttemplate.fragments.MentionsTimelineFragment;

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
    void inject(MentionsTimelineFragment frag);
    void inject(HomeTimelineFragment frag);
}
