package com.codepath.apps.restclienttemplate;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.codepath.apps.restclienttemplate.fragments.MentionsTimelineFragment;
import com.codepath.apps.restclienttemplate.fragments.TimelineFragment;

import java.util.Arrays;
import java.util.List;

/**
 * @author Joseph Gardi
 */
public class TweetsPagerAdapter extends FragmentPagerAdapter {

    private List<String> tabTitles = Arrays.asList("Home", "Mentions");

    public TweetsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                TimelineFragment fragment = new TimelineFragment();

            case 1:
                return new MentionsTimelineFragment();
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
}
