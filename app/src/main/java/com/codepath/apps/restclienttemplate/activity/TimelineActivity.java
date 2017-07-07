package com.codepath.apps.restclienttemplate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApplication;
import com.codepath.apps.restclienttemplate.fragments.TimelineFragment;
import com.codepath.apps.restclienttemplate.inject.AppModule;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TimelineActivity extends AppCompatActivity {

    private final String TAG = getClass().getName();

    @Inject
    @AppModule.HomeTimeline
    TimelineFragment homeTimelineFragment;
    @Inject
    @AppModule.MentionsTimeline
    TimelineFragment mentionsTimelineFragment;

    @BindView(R.id.view_pager)
    ViewPager vpPager;
    @BindView(R.id.sliding_tabs)
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((TwitterApplication) getApplication()).getAppComponent().inject(this);
        setContentView(R.layout.activity_timeline);
        ButterKnife.bind(this);

        vpPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            private List<String> tabTitles = Arrays.asList("Home", "Mentions");

            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return homeTimelineFragment;
                    case 1:
                        return mentionsTimelineFragment;
                    default:
                        throw new RuntimeException("Tab position is " + position);
                }
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return tabTitles.get(position);
            }
        });

        tabLayout.setupWithViewPager(vpPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timeline_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_tweet:
                startActivity(new Intent(this, TweetActivity.class));
                break;
            case R.id.profile:
                startActivity(new Intent(this, ProfileActivity.class));
                break;
        }

        return true;
    }
}
