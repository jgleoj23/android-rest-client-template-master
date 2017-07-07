package com.codepath.apps.restclienttemplate;

import com.codepath.apps.restclienttemplate.model.Tweet;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.realm.Realm;

/**
 * @author Joseph Gardi
 */
public class TweetCacher implements Consumer<List<Tweet>> {

    Realm realm;


    @Inject
    public TweetCacher(Realm realm) {
        this.realm = realm;
    }


    @Override
    public void accept(@NonNull final List<Tweet> tweets) throws Exception {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(tweets);
            }
        });
    }
}
