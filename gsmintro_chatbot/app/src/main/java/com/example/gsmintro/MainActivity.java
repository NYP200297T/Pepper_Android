package com.example.gsmintro;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.HolderBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.QiChatExecutor;
import com.aldebaran.qi.sdk.object.holder.AutonomousAbilitiesType;
import com.aldebaran.qi.sdk.object.holder.Holder;
import com.example.gsmintro.Executors.BookmarkExecutor;
import com.example.gsmintro.Executors.FragmentExecutor;
import com.example.gsmintro.Executors.TextExecutor;
import com.example.gsmintro.Fragments.LoadingFragment;
import com.example.gsmintro.Fragments.SplashFragment;
import com.example.gsmintro.Utils.ChatData;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    public QiContext qiContext;
    private static final String TAG = "gsmintro";
    private final List<String> topicNames = Arrays.asList("chatbot", "concepts");
    public Holder holder;
    public Animate animate;
    private FragmentManager fragmentManager;
    private String currentFragment;
    public ChatData currentChatBot, englishChatBot;
    private android.content.res.Configuration config;
    private Resources res;
    private Future<Void> chatFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        res = getApplicationContext().getResources();
        config = res.getConfiguration();
        this.fragmentManager = getSupportFragmentManager();
        QiSDK.register(this, this);
        updateLocale("en");
        setContentView(R.layout.activity_main);
    }

    private void updateLocale(String strLocale) {
        Locale locale = new Locale(strLocale);
        config.setLocale(locale);
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    @Override
    protected void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(@NotNull QiContext qiContext) {
        // The robot focus is gained.
        this.qiContext = qiContext;

        // Build the holder for the abilities.
        this.holder = HolderBuilder.with(qiContext)
                .withAutonomousAbilities(
                        AutonomousAbilitiesType.BASIC_AWARENESS,
                        AutonomousAbilitiesType.BACKGROUND_MOVEMENT
                )
                .build();

        Animation myAnimation = AnimationBuilder.with(qiContext)
                .withResources(R.raw.bowing_b001)
                .build();

        this.animate = AnimateBuilder.with(qiContext)
                .withAnimation(myAnimation)
                .build();

        Log.d("test", "listening");

        englishChatBot = new ChatData(this, qiContext, new Locale("en"), topicNames, true);
        Map<String, QiChatExecutor> executors = new HashMap<>();
        executors.put("FragmentExecutor", new FragmentExecutor(qiContext, this));
        executors.put("TextExecutor", new TextExecutor(qiContext, this));
        executors.put("BookmarkExecutor", new BookmarkExecutor(qiContext,this));
        englishChatBot.setupExecutors(executors);
        currentChatBot = englishChatBot;
        currentChatBot.chat.async().addOnStartedListener(() -> runOnUiThread(() -> {
            setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.ALWAYS); // Disable overlay mode for the rest of the app.
            setFragment(new SplashFragment());
        }));
        chatFuture = currentChatBot.chat.async().run();
        chatFuture.thenConsume(future -> {
            if (future.hasError()) {
                Log.e(TAG, "Discussion finished with error.", future.getError());
            }
        });
        this.currentChatBot.goToBookmarkNewTopic("init","chatbot");
        runOnUiThread(() -> setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.ALWAYS));
    }

    @Override
    public void onRobotFocusLost() {
        // The robot focus is lost.
        this.qiContext = null;
        if (currentChatBot != null) {
            currentChatBot.chat.async().removeAllOnStartedListeners();
        }
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
        currentFragment = fragment.getClass().getSimpleName();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_fade_in_right, R.anim.exit_fade_out_left,
                R.anim.enter_fade_in_left, R.anim.exit_fade_out_right);
        transaction.replace(R.id.placeholder, fragment, "currentFragment");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public static boolean isNumeric(String string) {
        if (string == null || string.equals("")) {
            return false;
        }

        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            System.out.println("Input String cannot be parsed to Integer.");
        }
        return false;
    }
}