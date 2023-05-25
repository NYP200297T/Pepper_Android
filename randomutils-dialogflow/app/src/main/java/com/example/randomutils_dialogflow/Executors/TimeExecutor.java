package com.example.randomutils_dialogflow.Executors;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;
import com.example.randomutils_dialogflow.MainActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * FragmentExecutor sets the fragment to be displayed in the placeholder of the main activity
 * This executor is added to the Chat(see main activity)
 * Triggered in qiChat as follow : ^execute( FragmentExecutor, frag_XXXX )
 */

public class TimeExecutor extends BaseQiChatExecutor {
    private final MainActivity ma;
    private String TAG = "MSI_FragmentExecutor";

    public TimeExecutor(QiContext qiContext, MainActivity mainActivity) {
        super(qiContext);
        this.ma = mainActivity;
    }

    @Override
    public void runWith(List<String> params) {
        Date currentTime = Calendar.getInstance().getTime();
        this.ma.currentChatBot.setQiChatVariable("timer",String.valueOf(currentTime));
    }

    @Override
    public void stop() {

    }
}