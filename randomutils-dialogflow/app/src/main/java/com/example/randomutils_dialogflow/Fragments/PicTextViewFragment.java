package com.example.randomutils_dialogflow.Fragments;

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

import com.example.randomutils_dialogflow.MainActivity;
import com.example.randomutils_dialogflow.R;

public class PicTextViewFragment extends Fragment {

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
            View view;
            Integer themeId = ma.getThemeId();
            if (themeId != null) {
                final Context contextThemeWrapper = new ContextThemeWrapper(ma, themeId);
                LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
                view = localInflater.inflate(fragmentId, container, false);
            } else {
                view = inflater.inflate(fragmentId, container, false);
            }

            // Grab string from Bundle
            TextView textLoading = view.findViewById(R.id.textviewer2);
            String receivedString = getArguments().getString("KEY");
            textLoading.setText(receivedString); //load text

            // Grab Image name from bundle
            ImageView imageView = view.findViewById(R.id.imageView7);
            int receivedImg = getArguments().getInt("IMG");
            imageView.setImageResource(receivedImg);

            return view;

        } else {
            Log.e(TAG, "could not get mainActivity, can't create fragment");
            return null;
        }
    }

}
