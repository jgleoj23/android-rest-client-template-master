package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.interactor.TwitterInteractor;
import com.codepath.apps.restclienttemplate.model.Tweet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * @author Joseph Gardi
 */
public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder> {

    private final String TAG = getClass().getName();
    private TwitterInteractor twitterInteractor;

    @Inject
    public TweetAdapter(TwitterInteractor twitterInteractor) {
        this.twitterInteractor = twitterInteractor;

        twitterInteractor.getHomeTimeline().subscribe(new Consumer<List<Tweet>>() {
            @Override
            public void accept(@NonNull List<Tweet> tweets) throws Exception {
                notifyDataSetChanged();
            }
        });
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.item_tweet, parent, false);

        return new ViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(twitterInteractor.getTweets().get(position));
    }


    @Override
    public int getItemCount() {
        return twitterInteractor.getTweets().size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        Context context;
        @BindView(R.id.ivProfileImage)
        ImageView ivProfileImage;
        @BindView(R.id.tvUsername)
        TextView tvUsername;
        @BindView(R.id.tvBody)
        TextView tvBody;
        @BindView(R.id.tvTime)
        TextView tvTime;


        ViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            ButterKnife.bind(this, itemView);
        }


        public void bind(Tweet tweet) {
            tvUsername.setText(tweet.getUser().getName());
            tvBody.setText(tweet.getText());

            Glide.with(context)
                    .load(tweet.getUser().getProfileImageUrl())
                    .into(ivProfileImage);

            tvTime.setText(getRelativeTimeAgo(tweet.getCreatedAt()));
        }

        public String getRelativeTimeAgo(String rawJsonDate) {
            String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
            SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
            sf.setLenient(true);

            String relativeDate = "";
            try {
                long dateMillis = sf.parse(rawJsonDate).getTime();
                relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                        System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return relativeDate;
        }
    }



}
