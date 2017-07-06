package com.codepath.apps.restclienttemplate.interactor;

import android.util.Log;

import com.codepath.apps.restclienttemplate.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.realm.Realm;

/**
 * Retreives and store the user who is logged in
 *
 * @author Joseph Gardi
 */
public class CurrentUserInteractor {

    private final String TAG = getClass().getName();
    private ObjectMapper objectMapper = new ObjectMapper();

    private TwitterClientInteractor twitterClient;

    private Observable<User> user;

    @Inject
    public CurrentUserInteractor(TwitterClientInteractor client, final Realm realm) {
        this.twitterClient = client;
        Log.i(TAG, "getting current user");
        user = twitterClient.get("account/verify_credentials.json", null)
                            .map(new Function<String, User>() {
                               @Override
                               public User apply(@NonNull String response) throws Exception {
                                   final User user = objectMapper.readValue(response, User.class);
                                   user.setIsCurrentUser(true);

                                   realm.executeTransactionAsync(new Realm.Transaction() {
                                       @Override
                                       public void execute(Realm realm) {
                                           realm.insertOrUpdate(user);
                                       }
                                   });

                                   return user;
                               }
                            })
                            .onErrorReturn(new Function<Throwable, User>() {
                                @Override
                                public User apply(@NonNull Throwable throwable) throws Exception {
                                    return realm.where(User.class)
                                                .equalTo("isCurrentUser", true)
                                                .findFirst();
                                }
                            })
                            .cache();
    }


    public Observable<User> getUser() {
        return user;
    }
}
