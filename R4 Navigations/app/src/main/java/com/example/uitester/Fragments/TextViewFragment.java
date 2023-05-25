package com.example.uitester.Fragments;

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

import com.example.uitester.MainActivity;
import com.example.uitester.R;

public class TextViewFragment extends Fragment {

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
        if (ma != null) {
            Integer themeId = ma.getThemeId();
            if (themeId != null) {
                final Context contextThemeWrapper = new ContextThemeWrapper(ma, themeId);
                LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
                View view = localInflater.inflate(fragmentId, container, false);
                TextView textLoading = view.findViewById(R.id.textviewer);
                String recivedString = getArguments().getString("KEY");
                textLoading.setText(recivedString);
                return view;
            } else {
                View view = inflater.inflate(fragmentId, container, false);
                TextView textLoading = view.findViewById(R.id.textviewer);
                String recivedString = getArguments().getString("KEY");
                textLoading.setText(recivedString);
                return view;
            }
        } else {
            Log.e(TAG, "could not get mainActivity, can't create fragment");
            return null;
        }
    }

}
