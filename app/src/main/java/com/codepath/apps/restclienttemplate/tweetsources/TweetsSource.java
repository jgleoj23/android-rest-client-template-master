package com.codepath.apps.restclienttemplate.tweetsources;

import com.codepath.apps.restclienttemplate.model.Tweet;

import io.reactivex.Observable;
import io.realm.RealmQuery;

/**
 * @author Joseph Gardi
 */
public interface TweetsSource {

    Observable<RealmQuery<Tweet>> getFromDisk();
    String getUrl();
}
