package com.codepath.apps.restclienttemplate.interactor;

import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.realm.Realm;

/**
 * Retrieves and store the user who is logged in
 *
 * @author Joseph Gardi
 */
public class UserInteractor {

    private final String TAG = getClass().getName();

    private Observable<User> user;

    @Inject
    public UserInteractor(TwitterClient twitterClient, final Realm realm) {
        user = twitterClient.get("account/verify_credentials.json", null)
                            .map(new Function<String, User>() {
                               @Override
                               public User apply(@NonNull String response) throws Exception {
                                   ObjectMapper mapper = new ObjectMapper();
                                   final User user = mapper.readValue(response, User.class);
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
