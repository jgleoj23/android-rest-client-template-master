package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.restclienttemplate.activity.ProfileActivity;
import com.codepath.apps.restclienttemplate.interactor.TimelineTweetsInteractor;
import com.codepath.apps.restclienttemplate.model.Tweet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Creates the rows for the timeline
 *
 * @author Joseph Gardi
 */
public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    private final String TAG = getClass().getName();
    private ObjectMapper objectMapper = new ObjectMapper();

    private TimelineTweetsInteractor timelineTweetsInteractor;
    private SimpleDateFormat twitterDateFormat;


    public TweetsAdapter(final TimelineTweetsInteractor timelineTweetsInteractor, SimpleDateFormat twitterDateFormat) {
        this.timelineTweetsInteractor = timelineTweetsInteractor;
        this.twitterDateFormat = twitterDateFormat;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tweet, parent, false));
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(timelineTweetsInteractor.getTweets().get(position));
    }


    @Override
    public int getItemCount() {

        return timelineTweetsInteractor.getTweets().size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private Context context;

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


        private void bind(final Tweet tweet) {
            Log.i(TAG, "binding " + tweet.getCreatedAt());

            tvUsername.setText(tweet.getUser().getName());
            tvBody.setText(tweet.getText());

            final String profileImageUrl = tweet.getUser().getProfileImageUrl();
            Picasso.with(context)
                    .load(profileImageUrl)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(ivProfileImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            System.out.println("error");
                            Picasso.with(context)
                                    .load(profileImageUrl)
                                    .into(ivProfileImage);
                        }
                    });

            tvTime.setText(getRelativeTimeAgo(tweet.getCreatedAt()));

            ivProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "clicked user: " + tweet.getUser().getScreenName());
                    Intent i = new Intent(context, ProfileActivity.class);
                    try {
                        i.putExtra("user", objectMapper.writeValueAsString(tweet.getUser()));
                        context.startActivity(i);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        private String getRelativeTimeAgo(String rawJsonDate) {
            twitterDateFormat.setLenient(true);

            String relativeDate = "";
            try {
                long dateMillis = twitterDateFormat.parse(rawJsonDate).getTime();
                relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                        System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return relativeDate;
        }
    }
}
