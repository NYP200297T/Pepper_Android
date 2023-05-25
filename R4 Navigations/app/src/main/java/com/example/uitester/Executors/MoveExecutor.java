package com.example.uitester.Executors;

import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;
import com.example.uitester.Fragments.SplashFragment;
import com.example.uitester.MainActivity;
import com.example.uitester.R;
import com.example.uitester.Utils.GoToHelper;

import java.util.List;

/**
 * FragmentExecutor sets the fragment to be displayed in the placeholder of the main activity
 * This executor is added to the Chat(see main activity)
 * Triggered in qiChat as follow : ^execute( FragmentExecutor, frag_XXXX )
 */

public class MoveExecutor extends BaseQiChatExecutor {
    private final MainActivity ma;
    private String TAG = "MSI_FragmentExecutor";
    private boolean running;
    private boolean done;
    private int routeCount = 0;

    public MoveExecutor(QiContext qiContext, MainActivity mainActivity) {
        super(qiContext);
        this.ma = mainActivity;
    }

    @Override
    public void runWith(List<String> params) {
        Log.d("test", String.valueOf(params));
        if (params.get(0).equals("route")) {
            Route();
        } else if (!params.get(0).equals("") && params.get(0) != null) {
            Move(params.get(0));
            this.ma.runOnUiThread(() -> this.ma.setFragment(new SplashFragment(),true));
        } else {
            this.ma.currentChatBot.goToBookmarkSameTopic("nolocation");
        }
    }

    @Override
    public void stop() {
        ma.robotHelper.releaseAbilities();
        ma.robotHelper.goToHelper.removeOnFinishedMovingListeners();
    }

    private void Move(String routeloc) {
        done = false;

        this.ma.currentTargetLoc = routeloc;
        this.ma.runOnUiThread(() -> {
            TextView textEdit = this.ma.findViewById(R.id.mapview);
            textEdit.setText("Going to " + routeloc);
        });
        this.ma.currentChatBot.setQiChatVariable("routeloc", routeloc);
        this.ma.currentChatBot.goToBookmarkSameTopic("routesay");

        this.ma.moveToLocation(routeloc);
        ma.robotHelper.goToHelper.addOnFinishedMovingListener((goToStatus) -> {
            if (goToStatus == GoToHelper.GoToStatus.FINISHED) {
                routeCount++;
                done = true;
            } else if (goToStatus == GoToHelper.GoToStatus.CANCELLED) {
                running = false;
                done = true;
            } else if (goToStatus == GoToHelper.GoToStatus.FAILED) {
                this.ma.runOnUiThread(this::showStuckAlert);
            }
            this.ma.robotHelper.goToHelper.removeOnFinishedMovingListeners();
        });

        while (true) {
            if (done) {
                break;
            }
        }
    }

    private void Route() {
        running = true;
        routeCount = 0;
        while (running) {
            String routeloc = this.ma.routepoints.get(routeCount);
            Move(routeloc);

            if (routeCount >= this.ma.routepoints.size()) {
                break;
            }
        }
        this.ma.runOnUiThread(() -> this.ma.setFragment(new SplashFragment(),true));
    }

    private void showStuckAlert() {
        // setup the alert builder
        AlertDialog.Builder builder = ma.buildDialog("I am stuck, please help me!",false);

        builder.setNegativeButton("Cancel Move", (dialog, which) -> {
            done = true;
            ma.amStuck = false;
            dialog.cancel();
            ma.cancelMoveToLocation();
        });

        // add the buttons
        builder.setPositiveButton("Done", (dialog, which) -> {
            done = true;
            ma.amStuck = false;
            dialog.dismiss();
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        ma.currentChatBot.goToBookmarkSameTopic("stuck");
        ma.amStuck = true;
        dialog.show();
    }

}