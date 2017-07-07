package com.codepath.apps.restclienttemplate.tweetsources;

import android.content.Context;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.model.Tweet;
import com.codepath.apps.restclienttemplate.model.User;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmQuery;

/**
 * @author Joseph Gardi
 */
public class UserTweetsSource implements TweetsSource {

    private final String TAG = getClass().getName();

    private Observable<RealmQuery<Tweet>> fromDisk;
    private String source;


    @Inject
    public UserTweetsSource(final Realm realm, final Context context, User user) {
        source = context.getString(R.string.userTimelineSource) + "?user_id=" + user.getId();

        fromDisk = Observable.just(realm.where(Tweet.class).equalTo("id", user.getId()));
    }


    @Override
    public Observable<RealmQuery<Tweet>> getFromDisk() {
        return fromDisk;
    }


    @Override
    public String getUrl() {
        return source;
    }
}
