package com.example.randomutils_dialogflow.Executors;

import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;
import com.example.randomutils_dialogflow.MainActivity;
import com.example.randomutils_dialogflow.R;

import java.io.IOException;
import java.util.List;

/**
 * FragmentExecutor sets the fragment to be displayed in the placeholder of the main activity
 * This executor is added to the Chat(see main activity)
 * Triggered in qiChat as follow : ^execute( FragmentExecutor, frag_XXXX )
 */

public class RadioExecutor extends BaseQiChatExecutor {
    private final MainActivity ma;
    private String TAG = "MSI_RadioExecutor";

    public RadioExecutor(QiContext qiContext, MainActivity mainActivity) {
        super(qiContext);
        this.ma = mainActivity;
    }

    @Override
    public void runWith(List<String> params) {

        String vara = params.get(0);

        switch (vara) {
            case ("start"):

                String radioStr = this.ma.currentChatBot.getQiChatVariable("radiostation");
                String audioUrl = this.ma.radioMap.get(radioStr);

                if (this.ma.mp != null) {
                    if (this.ma.mp.isPlaying()) {
                        this.ma.mp.stop();
                        this.ma.mp.reset();
                    }
                }

                // below line is use to set our
                // url to our media player.
                try {
                    this.ma.mp.setDataSource(audioUrl);
                    this.ma.mp.prepare();
                    this.ma.mp.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }

                this.ma.runOnUiThread(() -> {
                    TextView textLoading = this.ma.findViewById(R.id.textviewer2);
                    String setTexter = "Now Playing: " + radioStr;
                    textLoading.setText(setTexter);
                });

                break;

            case ("stop"):

                this.ma.mp.stop();
                this.ma.mp.reset();
                break;

            case ("frag"):

                String radioStr2 = this.ma.currentChatBot.getQiChatVariable("radiostation");
                String radioUri2 = "@drawable/" + TextUtils.join("", radioStr2.split("\\s+")).toLowerCase();

                this.ma.runOnUiThread(() -> {
                    int imageResource = this.ma.getResources().getIdentifier(radioUri2, null, this.ma.getPackageName());
                    ImageView imageView = this.ma.findViewById(R.id.imageView7);
                    imageView.setImageResource(imageResource);
                    TextView textLoading = this.ma.findViewById(R.id.textviewer2);
                    textLoading.setText("Loading: " + radioStr2);
                });

                this.ma.currentChatBot.goToBookmarkSameTopic("radiosay");
                break;

            default:
                break;
        }
    }

    @Override
    public void stop() {

    }
}