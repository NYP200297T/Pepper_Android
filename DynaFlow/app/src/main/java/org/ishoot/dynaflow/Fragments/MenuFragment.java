package org.ishoot.dynaflow.Fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.conversation.Say;

import org.ishoot.dynaflow.MainActivity;
import org.ishoot.dynaflow.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "MSI_LoadingFragment";
    private MainActivity ma;
    private Say sayer;
    private Future<Void> sayerr;
    public Listen listener = null;
    private int fragmentId;
    private Map fragmentInfo;
    private Map responseMap;
    private Map buttonsMap;
    private Future<ListenResult> listennervroom;
    private Map<String,PhraseSet> buttonMapResp = new HashMap();

    /**
     * inflates the layout associated with this fragment
     * if an application theme is set it will be applied to this fragment.
     */

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {

        this.ma = (MainActivity) getActivity();

        if (ma != null) {
            View view;
            Integer themeId = ma.getThemeId();
            if (themeId != null) {
                final Context contextThemeWrapper = new ContextThemeWrapper(ma, themeId);
                LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
                view = localInflater.inflate(fragmentId, container, false);
            } else {
                view = inflater.inflate(fragmentId, container, false);
            }

            if (fragmentInfo.containsKey("image")) {
                ConstraintLayout constraintLayout = view.findViewById(R.id.menu_layout);
                String imgPath = (String) fragmentInfo.get("image");
                Drawable d = new BitmapDrawable(getResources(), createBitmap(imgPath));
                constraintLayout.setBackground(d);
            }

            for (Object buttonName : buttonsMap.keySet()) {
                String buttonNamer = (String) buttonName;
                int buttonintID;
                Map buttonMap = (Map) this.buttonsMap.get(buttonNamer);
                switch (buttonNamer) {
                    case "button1":
                        buttonintID = R.id.button1;
                        break;
                    case "button2":
                        buttonintID = R.id.button2;
                        break;
                    case "button3":
                        buttonintID = R.id.button3;
                        break;
                    case "button4":
                        buttonintID = R.id.button4;
                        break;
                    case "button5":
                        buttonintID = R.id.button5;
                        break;
                    case "button6":
                        buttonintID = R.id.button6;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + buttonNamer);
                }

                if (buttonMap.containsKey("image")) {
                    ma.runOnUiThread(() -> {
                        AppCompatImageButton b = view.findViewById(buttonintID);
                        b.setImageBitmap(createBitmap((String) buttonMap.get("image")));
                        b.setClickable(false);
                    });
                }
            }

            return view;

        } else {
            Log.e(TAG, "could not get mainActivity, can't create fragment");
            return null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (this.sayer != null) {
            if (listener != null) {
                this.sayerr = sayer.async().run();
                ma.runOnUiThread(() -> this.sayerr.andThenConsume(useless -> {
                    listenUp();
                    setupButtons(view);
                }));
            }
        } else {
            if (listener != null) {
                ma.runOnUiThread(() -> {
                    listenUp();
                    setupButtons(view);
                });
            }
        }
    }

    public Bitmap createBitmap(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(ma.getImagePath(path), new BitmapFactory.Options());
        return Bitmap.createScaledBitmap(bitmap, 200, 200,true);
    }

    public void listenUp() {
        listennervroom = listener.async().run();
        listennervroom.andThenConsume(listenResult -> {
            PhraseSet phraseSetHeard = listenResult.getMatchedPhraseSet();
            for (String button : this.buttonMapResp.keySet()) {
                if (phraseSetHeard.equals(this.buttonMapResp.get(button))) {
                    Map buttonInfo = (Map) this.buttonsMap.get(button);
                    ma.nextFragment((String) buttonInfo.get("fragment"));
                    break;
                }
            }
        });
    }

    public void setInfo(QiContext qiContext, Map fragmentInfo, int menuId) {

        this.fragmentInfo = fragmentInfo;

        if (fragmentInfo.containsKey("chat")) {
            this.sayer = SayBuilder.with(qiContext)
                    .withText((String) fragmentInfo.get("chat"))
                    .build();
        }
        if (fragmentInfo.containsKey("responses")) {
            this.responseMap = (Map) fragmentInfo.get("responses");
            List<PhraseSet> phraseList = new ArrayList<>();
            for (Object resp : this.responseMap.keySet()) {
                List respL = (List) this.responseMap.get(resp);
                String[] stringArr = (String[]) respL.toArray(new String[0]);
                PhraseSet addPhraser = PhraseSetBuilder.with(qiContext).withTexts(stringArr).build();
                this.buttonMapResp.put((String) resp, addPhraser);
                phraseList.add(addPhraser);
            }
            this.listener = ListenBuilder.with(qiContext)
                    .withPhraseSets(phraseList)
                    .build();
        }
        if (fragmentInfo.containsKey("buttons")) {
            this.buttonsMap = (Map) fragmentInfo.get("buttons");
        }
        this.fragmentId = menuId;
    }

    @Override
    public void onClick(View v) {
        //do what you want to do when button is clicked
        String buttonPressed = getResources().getResourceEntryName(v.getId());
        Map buttonInfo = (Map) this.buttonsMap.get(buttonPressed);
        ma.runOnUiThread(() -> ma.nextFragment((String) buttonInfo.get("fragment")));
    }

    public void setupButtons(View view) {
        for (Object buttonName : buttonsMap.keySet()) {
            String buttonNamer = (String) buttonName;
            int buttonintID;
            switch (buttonNamer) {
                case "button1":
                    buttonintID = R.id.button1;
                    break;
                case "button2":
                    buttonintID = R.id.button2;
                    break;
                case "button3":
                    buttonintID = R.id.button3;
                    break;
                case "button4":
                    buttonintID = R.id.button4;
                    break;
                case "button5":
                    buttonintID = R.id.button5;
                    break;
                case "button6":
                    buttonintID = R.id.button6;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + buttonNamer);
            }
            ma.runOnUiThread(() -> {
                AppCompatImageButton b = view.findViewById(buttonintID);
                b.setOnClickListener(this);
            });
        }
    }

    public void killListen() {
        if (this.listennervroom != null) {
            this.listennervroom.cancel(true);
            this.listener.removeAllOnStartedListeners();
        }
        if (this.sayerr != null) {
            this.sayerr.cancel(true);
        }
    }

}
