package com.codepath.apps.restclienttemplate;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.interactor.TwitterInteractor;
import com.codepath.apps.restclienttemplate.model.User;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

public class TweetActivity extends AppCompatActivity {

    private final String TAG = getClass().getName();
    @BindView(R.id.ivProfilePhoto)
    ImageView ivProfilePhoto;
    @BindView(R.id.tvUsername)
    TextView tvUsername;
    @BindView(R.id.etStatus)
    EditText etStatus;

    @Inject
    TwitterInteractor twitterInteractor;


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

        twitterInteractor.getUser().subscribe(new Consumer<User>() {
            @Override
            public void accept(@NonNull User user) throws Exception {
                Glide.with(getApplicationContext())
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
        twitterInteractor.postTweet(etStatus.getText().toString())
                            .subscribe();

        finish();
    }
}
