package com.codepath.apps.restclienttemplate.tweetsources;

import android.content.Context;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.interactor.UserInteractor;
import com.codepath.apps.restclienttemplate.model.Tweet;
import com.codepath.apps.restclienttemplate.model.User;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.realm.Realm;
import io.realm.RealmQuery;

/**
 * @author Joseph Gardi
 */
public class MentionsSource implements TweetsSource {

    private String TAG = getClass().getName();

    private Observable<RealmQuery<Tweet>> fromDisk;
    private String source;


    @Inject
    public MentionsSource(final Realm realm, final Context context, UserInteractor userInteractor) {

        source = context.getString(R.string.mentionsTimelineSource);

        fromDisk = userInteractor.getUser()
                                     .map(new Function<User, RealmQuery<Tweet>>() {
            @Override
            public RealmQuery<Tweet> apply(@NonNull User user) throws Exception {
                return realm.where(Tweet.class)
                            .contains("text", "@" + user.getScreenName());
            }
        });
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
