package com.example.randomutils.Executors;

import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;
import com.example.randomutils.MainActivity;
import com.example.randomutils.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;

import java.util.List;

/**
 * FragmentExecutor sets the fragment to be displayed in the placeholder of the main activity
 * This executor is added to the Chat(see main activity)
 * Triggered in qiChat as follow : ^execute( FragmentExecutor, frag_XXXX )
 */

public class RadioExecutor extends BaseQiChatExecutor {
    private final MainActivity ma;
    private String TAG = "MSI_RadioExecutor";
    private ExoPlayer mp = null;
    private Handler mainHandler = null;

    public RadioExecutor(QiContext qiContext, MainActivity mainActivity) {
        super(qiContext);
        ma = mainActivity;
    }

    @Override
    public void runWith(List<String> params) {

        String vara = params.get(0);

        if (mainHandler == null) {
            mainHandler = new Handler(ma.getApplicationContext().getMainLooper());
        }

        Runnable stopper = () -> {
            if (this.mp != null) {
                if (this.mp.isPlaying()) {
                    this.mp.setPlayWhenReady(false);
                    this.mp.stop();
                    this.mp.release();
                }
            }
            this.mp = null;
        };

        switch (vara) {
            case ("start"):

                String radioStr = ma.currentChatBot.getQiChatVariable("radiostation");
                String radioUri = TextUtils.join("", radioStr.split("\\s+"));
                String audioUrl = ma.radioMap.get(radioUri);

                mainHandler.post(stopper);

                this.mp = new ExoPlayer.Builder(ma.getApplicationContext())
                        .setHandleAudioBecomingNoisy(false)
                        .build();

                // below line is use to set our
                // url to our media player.

                // This is your code
                mainHandler.post(() -> {
                    MediaItem mediaItem = MediaItem.fromUri(Uri.parse(audioUrl));
                    this.mp.setMediaItem(mediaItem);
                    this.mp.prepare();
                    this.mp.play();
                });

                ma.runOnUiThread(() -> {
                    TextView textLoading = ma.findViewById(R.id.textviewer2);
                    textLoading.setText("Now Playing: " + radioStr);
                });
                break;

            case ("stop"):
                mainHandler.post(stopper);
                break;

            case ("frag"):

                String radioStr2 = ma.currentChatBot.getQiChatVariable("radiostation");
                String radioUri2 = "@drawable/" + TextUtils.join("", radioStr2.split("\\s+")).toLowerCase();

                ma.runOnUiThread(() -> {
                    int imageResource = ma.getResources().getIdentifier(radioUri2, null, ma.getPackageName());
                    ImageView imageView = ma.findViewById(R.id.imageView7);
                    imageView.setImageResource(imageResource);
                    TextView textLoading = ma.findViewById(R.id.textviewer2);
                    textLoading.setText("Loading: " + radioStr2);
                });

                ma.currentChatBot.goToBookmarkSameTopic("radiosay");
                break;

            default:
                break;
        }
    }

    @Override
    public void stop() {

    }
}