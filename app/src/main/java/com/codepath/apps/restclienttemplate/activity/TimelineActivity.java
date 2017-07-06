package com.codepath.apps.restclienttemplate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApplication;

public class TimelineActivity extends AppCompatActivity {

    private final String TAG = getClass().getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((TwitterApplication) getApplication()).getAppComponent().inject(this);
        setContentView(R.layout.activity_timeline);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timeline_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_tweet) {
            startActivity(new Intent(this, TweetActivity.class));
        }

        return true;
    }
}
