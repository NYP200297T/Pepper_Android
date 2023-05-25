package com.example.gsmintro.Executors;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;
import com.example.gsmintro.MainActivity;

import java.util.List;

/**
 * FragmentExecutor sets the fragment to be displayed in the placeholder of the main activity
 * This executor is added to the Chat(see main activity)
 * Triggered in qiChat as follow : ^execute( FragmentExecutor, frag_XXXX )
 */

public class BookmarkExecutor extends BaseQiChatExecutor {
    private final MainActivity ma;
    private String TAG = "MSI_FragmentExecutor";

    public BookmarkExecutor(QiContext qiContext, MainActivity mainActivity) {
        super(qiContext);
        this.ma = mainActivity;
    }

    @Override
    public void runWith(List<String> params) {
        String bookmark = params.get(0);
        this.ma.currentChatBot.goToBookmarkSameTopic(bookmark);
    }

    @Override
    public void stop() {

    }
}