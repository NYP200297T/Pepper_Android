package com.example.gsmintro.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gsmintro.MainActivity;
import com.example.gsmintro.R;

public class HelloFragment extends Fragment {

    private static final String TAG = "MSI_LoadingFragment";
    private MainActivity ma;
    /**
     * inflates the layout associated with this fragment
     * if an application theme is set it will be applied to this fragment.
     */

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        int fragmentId = R.layout.fragment_pictextview;
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
            TextView textLoading = view.findViewById(R.id.textviewer2);
            textLoading.setText("Hello, nice to meet you.");

            // Grab Image name from bundle
            ImageView imageView = view.findViewById(R.id.imageView7);
            imageView.setImageResource(R.drawable.pepper_robot);

            return view;

        } else {
            Log.e(TAG, "could not get mainActivity, can't create fragment");
            return null;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.ma = (MainActivity) getActivity();
        this.ma.currentChatBot.goToBookmarkSameTopic("hello");
//        this.ma.holder.async().hold();
//        this.ma.animate.run();
//        this.ma.holder.async().release();
//          .andThenConsume(future ->
//            ma.setFragment(new SplashFragment())
//        );
    }

}
