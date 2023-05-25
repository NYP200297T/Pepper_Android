package org.ishoot.dynaflow.Fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.conversation.Say;

import org.ishoot.dynaflow.MainActivity;
import org.ishoot.dynaflow.R;

import java.util.Map;

public class NavigateFragment extends Fragment {

    private static final String TAG = "MSI_NavigateFragment";
    private MainActivity ma;
    private Say sayer = null;
    private Map fragmentInfo;
    private String nextFragmenter;
    private Future<Void> sayerfuture;

    /**
     * inflates the layout associated with this fragment
     * if an application theme is set it will be applied to this fragment.
     */

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
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

            TextView textLoading = view.findViewById(R.id.textviewer2);

            if (fragmentInfo.containsKey("title")) {
                String text = (String) this.fragmentInfo.get("title");
                ma.runOnUiThread(() -> textLoading.setText(text));
            }
            if (this.fragmentInfo.containsKey("textsettings")) {
                // 1: Bold, 2: Italic, 3: Bold_Italic
                int textModify = (int) this.fragmentInfo.get("textsettings");
                ma.runOnUiThread(() -> textLoading.setTypeface(textLoading.getTypeface(), textModify));
            }
            if (fragmentInfo.containsKey("image")) {
                ma.runOnUiThread(() -> textLoading.setBackground(
                        Drawable.createFromPath(ma.getImagePath((String) fragmentInfo.get("image"))
                        )
                ));
            }

            return view;

        } else {
            Log.e(TAG, "could not get mainActivity, can't create fragment");
            return null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (this.sayer != null) {
            // define sayerfuture to cancel later
            sayerfuture = sayer.async().run();

            sayerfuture.andThenConsume((useless) -> {
                // define future for finished movement
                ma.robotHelper.goToHelper.addOnFinishedMovingListener(useless2 -> {
                    ma.robotHelper.goToHelper.removeOnFinishedMovingListeners();
                    ma.nextFragment(nextFragmenter);
                });
                // create movement, if navsettings are available
                if (this.fragmentInfo.containsKey("navsettings")) {
                    Map moveModify = (Map) this.fragmentInfo.get("navsettings");
                    ma.moveToLocation(
                            (String) this.fragmentInfo.get("location"),
                            (boolean) moveModify.get("bgMove"),
                            (boolean) moveModify.get("straight"),
                            (boolean) moveModify.get("maxSpd")
                    );
                } else {
                    // use default moveToLocation settings
                    ma.moveToLocation((String) this.fragmentInfo.get("location"));
                }
            });
        }
    }

    public void setInfo(QiContext qiContext, Map fragmentInfo) {
        this.fragmentInfo = fragmentInfo;
        if (this.fragmentInfo.containsKey("chat")) {
            this.sayer = SayBuilder.with(qiContext)
                    .withText((String) fragmentInfo.get("chat"))
                    .build();
        }
        this.nextFragmenter = (String) this.fragmentInfo.get("next");
    }

    public void killListen() {
        if (sayerfuture != null) {
            sayerfuture.cancel(true);
        }
        ma.robotHelper.goToHelper.removeOnFinishedMovingListeners();
        ma.robotHelper.goToHelper.checkAndCancelCurrentGoto();
    }

}
