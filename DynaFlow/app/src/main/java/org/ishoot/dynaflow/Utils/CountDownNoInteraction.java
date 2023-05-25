package org.ishoot.dynaflow.Utils;

import android.os.CountDownTimer;
import android.util.Log;

import org.ishoot.dynaflow.MainActivity;

public class CountDownNoInteraction extends CountDownTimer {

    private String TAG = "MSI_NoInteraction";
    private String nextFragment;
    private MainActivity mainActivity;

    public CountDownNoInteraction(MainActivity mainActivity, String nextFragment, long millisUtilEnd, long countDownInterval) {
        super(millisUtilEnd, countDownInterval);
        this.nextFragment = nextFragment;
        this.mainActivity = mainActivity;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        //Log.d(TAG,"Millis until end : " + millisUntilFinished);
    }

    @Override
    public void onFinish() {
        Log.d(TAG, "Timer Finished");
        mainActivity.nextFragment(nextFragment);
    }

    public void reset() {
        Log.d(TAG, "Timer Reset");
        super.cancel();
        super.start();
    }
}
