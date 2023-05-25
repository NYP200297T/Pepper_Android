package com.example.gsmintro.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gsmintro.MainActivity;
import com.example.gsmintro.R;

public class IntroCourseFragment extends Fragment {

    private static final String TAG = "MSI_LoadingFragment";
    private MainActivity ma;

    /**
     * inflates the layout associated with this fragment
     * if an application theme is set it will be applied to this fragment.
     */

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        int fragmentId = R.layout.fragment_textview;
        this.ma = (MainActivity) getActivity();
        View view;
        if (ma != null) {
            Integer themeId = ma.getThemeId();
            if (themeId != null) {
                final Context contextThemeWrapper = new ContextThemeWrapper(ma, themeId);
                LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
                view = localInflater.inflate(fragmentId, container, false);
            } else {
                view = inflater.inflate(fragmentId, container, false);
            }
            return view;
        } else {
            Log.e(TAG, "could not get mainActivity, can't create fragment");
            return null;
        }
    }
//
//    @Override
//    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        this.ma = (MainActivity) getActivity();
//        ma.currentChatBot.goToBookmarkSameTopic("introcourse1");
//    }

}
