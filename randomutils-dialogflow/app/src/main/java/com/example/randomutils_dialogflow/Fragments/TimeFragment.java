package com.example.randomutils_dialogflow.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.randomutils_dialogflow.MainActivity;
import com.example.randomutils_dialogflow.R;

import java.util.Calendar;
import java.util.Date;

public class TimeFragment extends Fragment {

    private static final String TAG = "MSI_LoadingFragment";
    private MainActivity ma;
    private TextView textLoading;
    /**
     * inflates the layout associated with this fragment
     * if an application theme is set it will be applied to this fragment.
     */

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        int fragmentId = R.layout.fragment_textview;
        this.ma = (MainActivity) getActivity();
        if (ma != null) {
            Integer themeId = ma.getThemeId();
            View view;
            if (themeId != null) {
                final Context contextThemeWrapper = new ContextThemeWrapper(ma, themeId);
                LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
                view = localInflater.inflate(fragmentId, container, false);
            } else {
                view = inflater.inflate(fragmentId, container, false);
            }

            // Grab string from Bundle
            this.textLoading = view.findViewById(R.id.textviewer_weather);

            return view;

        } else {
            Log.e(TAG, "could not get mainActivity, can't create fragment");
            return null;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Date currentTime = Calendar.getInstance().getTime();
        this.textLoading.setText("The time now is "+currentTime);
        this.ma.currentChatBot.goToBookmarkSameTopic("time");
    }
}
