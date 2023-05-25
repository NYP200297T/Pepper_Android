package org.ishoot.dynaflow.Utils;

import android.os.CountDownTimer;
import android.util.Log;

import org.ishoot.dynaflow.MainActivity;
import org.ishoot.dynaflow.R;

public class AdminConsoleButton extends CountDownTimer {

    private String TAG = "MSI_NoInteraction";
    private MainActivity ma;
    private int adminpress = 0;
    private final int adminPresses = 5;

    public AdminConsoleButton(MainActivity ma, long millisUtilEnd, long countDownInterval) {
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
        this.adminpress = 0;
    }

    public void reset() {
        Log.d(TAG, "Timer Reset");
        super.cancel();
        super.start();
    }

    public void setupButton() {
        ma.findViewById(R.id.adminconsolebutton).setOnClickListener(v -> {
            if (v.equals(ma.findViewById(R.id.adminconsolebutton))) {
                if (this.adminpress == this.adminPresses-1) {
                    ma.runOnUiThread(() -> ma.nextFragment("admin"));
                    this.cancel();
                    this.adminpress = 0;
                } else if (this.adminpress == 0) {
                    this.start();
                    this.adminpress += 1;
                } else {
                    this.reset();
                    this.adminpress += 1;
                }
            }
        });
    }

}
