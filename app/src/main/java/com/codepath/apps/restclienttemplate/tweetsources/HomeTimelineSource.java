package com.codepath.apps.restclienttemplate.tweetsources;

import android.content.Context;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.model.Tweet;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmQuery;

/**
 * @author Joseph Gardi
 */
public class HomeTimelineSource implements TweetsSource {

    private String TAG = getClass().getName();

    private Observable<RealmQuery<Tweet>> fromDisk;
    private String source;


    @Inject
    public HomeTimelineSource(final Realm realm, final Context context) {
        source = context.getString(R.string.homeTimelineSource);

        fromDisk = Observable.just(realm.where(Tweet.class));
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
