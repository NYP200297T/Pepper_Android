package org.ishoot.dynaflow.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.ishoot.dynaflow.MainActivity;
import org.ishoot.dynaflow.R;

public class AdminFragment extends Fragment {

    private static final String TAG = "MSI_AdminFragment";
    private MainActivity ma;

    /**
     * inflates the layout associated with this fragment
     * if an application theme is set it will be applied to this fragment.
     */

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {

        int fragmentId = R.layout.fragment_admin;
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

            Button admin_restart = view.findViewById(R.id.admin_restart);
            admin_restart.setOnClickListener(v -> ma.nextFragment("home"));

            Button admin_choose = view.findViewById(R.id.admin_choose);
            admin_choose.setOnClickListener(v -> ma.showFileChooser());

            return view;

        } else {
            Log.e(TAG, "could not get mainActivity, can't create fragment");
            return null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        CharSequence showText = ma.getBatteryLevel() + "%";
        TextView battery_level = view.findViewById(R.id.battery_level);
        battery_level.setText(showText);
    }

}
