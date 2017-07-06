package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.interactor.TimelineTweetsInteractor;
import com.google.common.collect.Range;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

import static android.content.ContentValues.TAG;
import static com.codepath.apps.restclienttemplate.StackPrinter.stackPrinter;

/**
 * @author Joseph Gardi
 */
public class TimelineView extends RelativeLayout {

    private TimelineTweetsInteractor timelineTweetsInteractor;
    private TweetsAdapter tweetsAdapter;

    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;
    @BindView(R.id.rvTweets)
    RecyclerView rvTweets;

    public TimelineView(Context context, final TweetsAdapter tweetsAdapter,
                        final TimelineTweetsInteractor timelineTweetsInteractor) {
        super(context);
        this.timelineTweetsInteractor = timelineTweetsInteractor;
        this.tweetsAdapter = tweetsAdapter;
        inflate(context, R.layout.timeline, this);
        ButterKnife.bind(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvTweets.setLayoutManager(layoutManager);
        rvTweets.setAdapter(tweetsAdapter);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadTweets();
            }
        });

        rvTweets.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(TAG, "loading more");
                timelineTweetsInteractor.getLoadOlderTweets().subscribe(new Consumer<Range<Integer>>() {
                    @Override
                    public void accept(@NonNull final Range<Integer> integerRange) throws Exception {
                        rvTweets.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG, "running now: " + integerRange + ", " + timelineTweetsInteractor.getTweets().size());
                                tweetsAdapter.notifyItemRangeInserted(integerRange.lowerEndpoint(),
                                                                      integerRange.upperEndpoint() - integerRange.lowerEndpoint());
                            }
                        });
                    }
                }, stackPrinter);
            }
        });

        timelineTweetsInteractor.getLoadTweetsFromDisk().subscribe(new Consumer<Range<Integer>>() {
            @Override
            public void accept(@NonNull Range<Integer> integerRange) throws Exception {
                tweetsAdapter.notifyItemRangeInserted(integerRange.lowerEndpoint(), integerRange.upperEndpoint() - integerRange.lowerEndpoint());
            }
        }, stackPrinter);

        loadTweets();
    }


    private void loadTweets() {
        timelineTweetsInteractor.getLoadTweets().subscribe(new Consumer<Range<Integer>>() {
            @Override
            public void accept(@NonNull Range<Integer> integerRange) throws Exception {
                tweetsAdapter.notifyItemRangeInserted(integerRange.lowerEndpoint(), integerRange.upperEndpoint() - integerRange.lowerEndpoint());
                swipeContainer.setRefreshing(false);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                Log.i(TAG, "handling the error");
                swipeContainer.setRefreshing(false);
                Toast.makeText(getContext(), "Failed to refresh", Toast.LENGTH_SHORT)
                     .show();
                throwable.printStackTrace();
            }
        });
    }
}
