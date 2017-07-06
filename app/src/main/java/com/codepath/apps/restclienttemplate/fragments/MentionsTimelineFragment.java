package com.codepath.apps.restclienttemplate.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.restclienttemplate.TimelineView;
import com.codepath.apps.restclienttemplate.TwitterApplication;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Joseph Gardi
 */
public class MentionsTimelineFragment extends Fragment {

    private String TAG = getClass().getName();

    @Inject
    @Named("mentionsTimelineSource")
    TimelineView timelineView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((TwitterApplication) getActivity().getApplication()).getAppComponent().inject(this);
        return timelineView;
    }
}
