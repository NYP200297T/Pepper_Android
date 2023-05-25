package com.example.uitester.Executors;

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

public class RouteExecutor extends BaseQiChatExecutor {
    private final MainActivity ma;
    private String TAG = "MSI_FragmentExecutor";
    private boolean running;
    private boolean done;
    private int routeCount = 0;

    public RouteExecutor(QiContext qiContext, MainActivity mainActivity) {
        super(qiContext);
        this.ma = mainActivity;
    }

    @Override
    public void runWith(List<String> params) {
        running = true;
        while (running) {

            done = false;
            String routeloc = this.ma.routepoints.get(routeCount);
            this.ma.currentTargetLoc = routeloc;
            this.ma.runOnUiThread(() -> {
                TextView textEdit = this.ma.findViewById(R.id.mapview);
                textEdit.setText("Going to " + routeloc);
            });
            this.ma.currentChatBot.setQiChatVariable("routeloc",routeloc);
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

        this.ma.runOnUiThread(() -> this.ma.setFragment(new SplashFragment()));
    }

    @Override
    public void stop() {
        ma.robotHelper.releaseAbilities();
        ma.robotHelper.goToHelper.removeOnFinishedMovingListeners();
    }

    private void showStuckAlert() {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getQiContext());
        builder.setTitle("Notice");
        builder.setMessage("I am stuck, please help me!");
        builder.setCancelable(false);

        // add the buttons
        builder.setPositiveButton("Done", (dialog, which) -> {
            done = true;
            ma.amStuck = false;
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel Move", (dialog, which) -> {
            done = true;
            ma.amStuck = false;
            dialog.cancel();
            ma.cancelMoveToLocation();
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        ma.currentChatBot.goToBookmarkSameTopic("stuck");
        ma.amStuck = true;
        dialog.show();
    }

}