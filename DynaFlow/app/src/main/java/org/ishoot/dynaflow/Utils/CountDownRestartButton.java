package org.ishoot.dynaflow.Utils;

import android.os.CountDownTimer;
import android.util.Log;

import org.ishoot.dynaflow.MainActivity;
import org.ishoot.dynaflow.R;

public class CountDownRestartButton extends CountDownTimer {

    private String TAG = "MSI_NoInteraction";
    private MainActivity ma;
    private int restartpress = 0;
    private final int countdownPresses = 5;

    public CountDownRestartButton(MainActivity ma, long millisUtilEnd, long countDownInterval) {
        super(millisUtilEnd, countDownInterval);
        this.ma = ma;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        //Log.d(TAG,"Millis until end : " + millisUntilFinished);
    }

    @Override
    public void onFinish() {
        Log.d(TAG, "Timer Finished");
        this.restartpress = 0;
    }

    public void reset() {
        Log.d(TAG, "Timer Reset");
        super.cancel();
        super.start();
    }

    public void setupButton() {
        ma.findViewById(R.id.homefloatbutton).setOnClickListener(v -> {
            if (v.equals(ma.findViewById(R.id.homefloatbutton))) {
                if (this.restartpress == this.countdownPresses-1) {
                    ma.runOnUiThread(() -> ma.nextFragment("home"));
                    this.cancel();
                    this.restartpress = 0;
                } else if (this.restartpress == 0) {
                    this.start();
                    this.restartpress += 1;
                } else {
                    this.reset();
                    this.restartpress += 1;
                }
            }
        });
    }

}
