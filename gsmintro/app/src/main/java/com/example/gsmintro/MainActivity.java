package com.example.gsmintro;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.HolderBuilder;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.holder.AutonomousAbilitiesType;
import com.aldebaran.qi.sdk.object.holder.Holder;
import com.aldebaran.qi.sdk.util.PhraseSetUtil;
import com.example.gsmintro.Fragments.LoadingFragment;
import com.example.gsmintro.Fragments.SplashFragment;
import com.example.gsmintro.Fragments.TextViewFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    public QiContext qiContext;
    private static final String TAG = "gsmintro";
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.fragmentManager = getSupportFragmentManager();
        QiSDK.register(this, this);
        setContentView(R.layout.activity_main);
        Log.d(TAG, TAG);
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        super.onDestroy();
        QiSDK.unregister(this, this);
    }

    @Override
    public void onRobotFocusGained(@NotNull QiContext qiContext) {
        // The robot focus is gained.
        this.qiContext = qiContext;
        runOnUiThread(() -> {
            setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.ALWAYS);
        });

        // Build the holder for the abilities.
        Holder holder = HolderBuilder.with(qiContext)
                .withAutonomousAbilities(
                        AutonomousAbilitiesType.BASIC_AWARENESS,
                        AutonomousAbilitiesType.BACKGROUND_MOVEMENT
                )
                .build();

        ArrayList<PhraseSet> phraseList = new ArrayList<>();

        PhraseSet hiPhrase = PhraseSetBuilder.with(qiContext)
                .withTexts("Hello", "Hi")
                .build();
        phraseList.add(hiPhrase);

        PhraseSet introPhrase = PhraseSetBuilder.with(qiContext)
                .withTexts("Introduce yourself", "Pepper, introduce yourself")
                .build();
        phraseList.add(introPhrase);

        PhraseSet coursePhrase = PhraseSetBuilder.with(qiContext)
                .withTexts("Introduce the course", "Pepper, introduce the course")
                .build();
        phraseList.add(coursePhrase);

        Listen listen = ListenBuilder.with(qiContext)
                .withPhraseSets(phraseList)
                .build();

        Animation animation = AnimationBuilder.with(qiContext)
                .withResources(R.raw.bowing_b001)
                .build();
        Animate animate = AnimateBuilder.with(qiContext)
                .withAnimation(animation)
                .build();

        while (true) {

            Log.d("test", "listening");
            setFragment(new SplashFragment());

            PhraseSet matchedPhraseSet = listen.run().getMatchedPhraseSet();

            if (PhraseSetUtil.equals(matchedPhraseSet, hiPhrase)) {

                Bundle bundle = new Bundle();
                bundle.putString("KEY", "Hello, nice to meet you.");

                TextViewFragment fragger = new TextViewFragment();
                fragger.setArguments(bundle);
                setFragment(fragger);

                sayFunc(qiContext,"Hello, nice to meet you.");

                Log.d("test", "bowing");

                holder.async().hold();
                animate.run();
                holder.async().release();

            } else if (PhraseSetUtil.equals(matchedPhraseSet, introPhrase)) {

                Bundle bundle = new Bundle();
                String introstr = getString(R.string.gsm_intro);
                bundle.putString("KEY", introstr);

                TextViewFragment fragger = new TextViewFragment();
                fragger.setArguments(bundle);
                setFragment(fragger);

                Log.d("test", "introself");
                sayFunc(qiContext, introstr);

            } else if (PhraseSetUtil.equals(matchedPhraseSet, coursePhrase)) {

                String[] splited = getString(R.string.gsm_script).split("\n");

                for (String str : splited) {
                    Bundle bundle = new Bundle();
                    bundle.putString("KEY", str);

                    TextViewFragment fragger = new TextViewFragment();
                    fragger.setArguments(bundle);
                    setFragment(fragger);

                    Log.d("test", "course");
                    sayFunc(qiContext, str);

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onRobotFocusLost() {
        // The robot focus is lost.
        this.qiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.OVERLAY); // We don't want to see the speech bar while loading
        this.setFragment(new LoadingFragment());
    }

    public Integer getThemeId() {
        try {
            return getPackageManager().getActivityInfo(getComponentName(), 0).getThemeResource();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setFragment(@NotNull Fragment fragment) {
        Log.d(TAG, "Transaction for fragment : " + fragment.getClass().getSimpleName());
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_fade_in_right, R.anim.exit_fade_out_left,
                R.anim.enter_fade_in_left, R.anim.exit_fade_out_right);
        transaction.replace(R.id.placeholder, fragment, "currentFragment");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void sayFunc (QiContext qiContext, String text) {

        // Build the action.
        Say say = SayBuilder.with(qiContext)
                .withText(text)
                .build();

        // Run the action synchronously.
        say.run();
    }

}