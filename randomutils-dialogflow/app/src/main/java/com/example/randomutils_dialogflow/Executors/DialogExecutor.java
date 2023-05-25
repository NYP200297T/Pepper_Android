package com.example.randomutils_dialogflow.Executors;

import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;
import com.example.randomutils_dialogflow.DialogRequest;
import com.example.randomutils_dialogflow.MainActivity;

import java.util.List;


/**
 * FragmentExecutor sets the fragment to be displayed in the placeholder of the main activity
 * This executor is added to the Chat(see main activity)
 * Triggered in qiChat as follow : ^execute( FragmentExecutor, frag_XXXX )
 */

public class DialogExecutor extends BaseQiChatExecutor {
    private final MainActivity ma;

    public DialogExecutor(QiContext qiContext, MainActivity mainActivity) {
        super(qiContext);
        this.ma = mainActivity;
    }

    @Override
    public void runWith(List<String> params) {
        String str = this.ma.currentChatBot.getQiChatVariable("sayer");
        Log.d("heard:",str);
        try {
            DialogRequest.run(this.ma,str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {

    }
}