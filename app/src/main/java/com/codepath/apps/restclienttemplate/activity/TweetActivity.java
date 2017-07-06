package com.codepath.apps.restclienttemplate.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApplication;
import com.codepath.apps.restclienttemplate.interactor.CurrentUserInteractor;
import com.codepath.apps.restclienttemplate.interactor.PostTweetInteractor;
import com.codepath.apps.restclienttemplate.model.User;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * Compose a new tweet
 *
 * @author Joseph Gardi
 */
public class TweetActivity extends AppCompatActivity {

    private final String TAG = getClass().getName();
    @BindView(R.id.ivProfilePhoto)
    ImageView ivProfilePhoto;
    @BindView(R.id.tvUsername)
    TextView tvUsername;
    @BindView(R.id.etStatus)
    EditText etStatus;

    @Inject
    PostTweetInteractor postTweetInteractor;
    @Inject
    CurrentUserInteractor currentUserInteractor;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((TwitterApplication) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);
        ButterKnife.bind(this);

        currentUserInteractor.getUser().subscribe(new Consumer<User>() {
            @Override
            public void accept(@NonNull User user) throws Exception {
                Picasso.with(getApplicationContext())
                        .load(user.getProfileImageUrl())
                        .into(ivProfilePhoto);

                tvUsername.setText("@" + user.getScreenName());
            }
        });
    }


    @OnClick(R.id.btnCancel)
    public void goBack() {
        finish();
    }


    @OnClick(R.id.btnTweet)
    public void sendAndGoBack() {
        postTweetInteractor.postTweet(etStatus.getText().toString())
                            .subscribe();

        finish();
    }
}
