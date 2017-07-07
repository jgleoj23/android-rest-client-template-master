package com.codepath.apps.restclienttemplate.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApplication;
import com.codepath.apps.restclienttemplate.fragments.TimelineFragment;
import com.codepath.apps.restclienttemplate.interactor.UserInteractor;
import com.codepath.apps.restclienttemplate.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

public class ProfileActivity extends AppCompatActivity {

    private String TAG = getClass().getName();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    UserInteractor userInteractor;
    @Inject
    Function<User, TimelineFragment> timelineFragmentFactory;

    @BindView(R.id.tvName)
    TextView tvName;
    @BindView(R.id.tvTagline)
    TextView tvTagline;
    @BindView(R.id.ivProfileImage)
    ImageView ivProfileImage;
    @BindView(R.id.tvFollowers)
    TextView tvFollowers;
    @BindView(R.id.tvFollowing)
    TextView tvFollowing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ((TwitterApplication) getApplication()).getAppComponent().inject(this);
        ButterKnife.bind(this);

        String json = getIntent().getStringExtra("user");
        Log.i(TAG, "got the user: " + json);
        if (json != null) {
            try {
                display(objectMapper.readValue(json, User.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            userInteractor.getUser().subscribe(new Consumer<User>() {
                @Override
                public void accept(@NonNull User user) throws Exception {
                    display(user);
                }
            });
        }
    }

    public void display(User user) {
        TimelineFragment timelineFragment = timelineFragmentFactory.apply(user);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.flContainer, timelineFragment);
        fragmentTransaction.commit();

        getSupportActionBar().setTitle(user.getScreenName());

        tvName.setText(user.getName());
        tvTagline.setText(user.getDescription());
        Log.i(TAG, "use: " + user.getDescription());
        tvFollowers.setText(user.getFollowersCount() + " Followers");
        tvFollowing.setText(user.getFriendsCount() + " Following");

        Picasso.with(ProfileActivity.this)
               .load(user.getProfileImageUrl())
               .into(ivProfileImage);
    }
}
