package com.codepath.apps.restclienttemplate.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.EndlessRecyclerViewScrollListener;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TweetsAdapter;
import com.codepath.apps.restclienttemplate.TwitterApplication;
import com.codepath.apps.restclienttemplate.interactor.TimelineTweetsInteractor;
import com.google.common.base.Function;
import com.google.common.collect.Range;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

import static com.codepath.apps.restclienttemplate.StackPrinter.stackPrinter;

/**
 * @author Joseph Gardi
 */
public class TimelineFragment extends Fragment {

    private String TAG = getClass().getName();

    private TimelineTweetsInteractor timelineTweetsInteractor;
    private TweetsAdapter tweetsAdapter;

    @Inject
    Function<TimelineTweetsInteractor, TweetsAdapter> tweetsAdapterFactory;

    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;
    @BindView(R.id.rvTweets)
    RecyclerView rvTweets;


    public static TimelineFragment newInstance(TimelineTweetsInteractor timelineTweetsInteractor) {
        TimelineFragment fragment = new TimelineFragment();
        fragment.timelineTweetsInteractor = timelineTweetsInteractor;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((TwitterApplication) getActivity().getApplication()).getAppComponent().inject(this);
        View view = inflater.inflate(R.layout.timeline, container, false);
        ButterKnife.bind(this, view);

        tweetsAdapter = tweetsAdapterFactory.apply(timelineTweetsInteractor);

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
                Log.i(TAG, "loading more: " + page);
                timelineTweetsInteractor.getLoadOlderTweets().subscribe(new Consumer<Range<Integer>>() {
                    @Override
                    public void accept(@NonNull final Range<Integer> integerRange) throws Exception {
                        rvTweets.post(new Runnable() {
                            @Override
                            public void run() {
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

        timelineTweetsInteractor.getPostedTweet().subscribe(new Consumer<Integer>() {
            @Override
            public void accept(@NonNull Integer position) throws Exception {
                Log.i(TAG, "accepted");
                tweetsAdapter.notifyItemInserted(0);
            }
        });

        loadTweets();

        return view;
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
