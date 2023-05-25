package com.example.gsmintro.Executors;

import android.util.Log;
import android.widget.TextView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;
import com.example.gsmintro.MainActivity;
import com.example.gsmintro.R;

import java.util.List;

/**
 * FragmentExecutor sets the fragment to be displayed in the placeholder of the main activity
 * This executor is added to the Chat(see main activity)
 * Triggered in qiChat as follow : ^execute( FragmentExecutor, frag_XXXX )
 */

public class TextExecutor extends BaseQiChatExecutor {
    private final MainActivity ma;
    private String TAG = "MSI_FragmentExecutor";
    private String[] splited;

    public TextExecutor(QiContext qiContext, MainActivity mainActivity) {
        super(qiContext);
        this.ma = mainActivity;
        this.splited = ma.getString(R.string.gsm_script).split("\n");
    }

    @Override
    public void runWith(List<String> params) {
        String num = params.get(0);
        String text = "";
        if (params == null || params.isEmpty()) {
            return;
        } else if (ma.isNumeric(num)) {
            text = splited[Integer.valueOf(num)-1];
        }
        Log.d(TAG,"new Text :" + text);
        String finalText = text;
        ma.runOnUiThread(() -> {
            TextView textloader = this.ma.findViewById(R.id.textviewer);
            textloader.setText(finalText);
        });

    }

    @Override
    public void stop() {

    }
}