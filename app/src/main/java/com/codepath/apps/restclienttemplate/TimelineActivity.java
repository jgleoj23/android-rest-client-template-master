package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.codepath.apps.restclienttemplate.interactor.TwitterInteractor;

import javax.inject.Inject;

public class TimelineActivity extends AppCompatActivity {

    private final String TAG = getClass().getName();

    @Inject
    TweetAdapter tweetAdapter;
    @Inject
    TwitterInteractor twitterInteractor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((TwitterApplication) getApplication()).getAppComponent().inject(this);
        setContentView(R.layout.activity_timeline);

        RecyclerView rvTweets = (RecyclerView) findViewById(R.id.rvTweets);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvTweets.setLayoutManager(layoutManager);
        rvTweets.setAdapter(tweetAdapter);

        rvTweets.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {

            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(TAG, "loading more");
                twitterInteractor.loadMoreTweet();
            }
        });
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
