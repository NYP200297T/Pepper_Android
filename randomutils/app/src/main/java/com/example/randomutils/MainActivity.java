package com.example.randomutils;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.QiChatExecutor;
import com.aldebaran.qi.sdk.object.holder.AutonomousAbilitiesType;
import com.aldebaran.qi.sdk.object.holder.Holder;
import com.example.randomutils.Executors.BookmarkExecutor;
import com.example.randomutils.Executors.FragmentExecutor;
import com.example.randomutils.Executors.RadioExecutor;
import com.example.randomutils.Executors.TimeExecutor;
import com.example.randomutils.Executors.WeatherExecutor;
import com.example.randomutils.Fragments.LoadingFragment;
import com.example.randomutils.Fragments.SplashFragment;
import com.example.randomutils.Utils.ChatData;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    public QiContext qiContext;
    private static final String TAG = "randomutils";
    private final List<String> topicNames = Arrays.asList("chatbot", "concepts");
    public Holder holder;
    public Animate animate;
    private FragmentManager fragmentManager;
    private String currentFragment = "";
    public ChatData currentChatBot, englishChatBot;
    private android.content.res.Configuration config;
    private Resources res;
    private Future<Void> chatFuture;
    public Map<String, String> radioMap = new HashMap<>();
    public List<String> isoCountries = new ArrayList<>();
    private List<Phrase> radioPhrases = new ArrayList<>();
    private List<Phrase> isoCountriesPhrases = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        res = getApplicationContext().getResources();
        config = res.getConfiguration();
        this.fragmentManager = getSupportFragmentManager();
        QiSDK.register(this, this);
        updateLocale("en");
        setContentView(R.layout.activity_main);
        this.radioMap.put("Class95", "https://22393.live.streamtheworld.com/CLASS95_PREM.aac");
        this.radioMap.put("Gold905", "https://22393.live.streamtheworld.com/GOLD905_PREM.aac");
        this.radioMap.put("CNA938", "https://22393.live.streamtheworld.com/938NOW_PREM.aac");
        this.radioMap.put("Kiss92", "https://23743.live.streamtheworld.com/KISS_92AAC.aac");

        for (String radioStr : radioMap.keySet()) {
            String radioSplit = TextUtils.join(" ",radioStr.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)"));
            radioPhrases.add(new Phrase(radioSplit));
        }

        for (String iso : Locale.getISOCountries()) {
            Locale l = new Locale("", iso);
            String dispcountry = l.getDisplayCountry();
            isoCountries.add(dispcountry);
            isoCountriesPhrases.add(new Phrase(dispcountry));
        }
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
        runOnUiThread(() -> setFragment(new LoadingFragment()));

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
        executors.put("BookmarkExecutor", new BookmarkExecutor(qiContext,this));
        executors.put("TimeExecutor", new TimeExecutor(qiContext,this));
        executors.put("RadioExecutor", new RadioExecutor(qiContext,this));
        executors.put("WeatherExecutor", new WeatherExecutor(qiContext,this));
        englishChatBot.setupExecutors(executors);
        currentChatBot = englishChatBot;
        currentChatBot.setDynamicConcept("radiostations", radioPhrases);
        currentChatBot.setDynamicConcept("countryphrases", isoCountriesPhrases);
        currentChatBot.chat.async().addOnStartedListener(() -> runOnUiThread(() -> {
            currentChatBot.goToBookmarkNewTopic("init","chatbot");
            setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.ALWAYS);
            setFragment(new SplashFragment());
        }));
        chatFuture = currentChatBot.chat.async().run();
        chatFuture.thenConsume(future -> {
            if (future.hasError()) {
                Log.e(TAG, "Discussion finished with error.", future.getError());
            }
        });
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
        runOnUiThread(() -> setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.OVERLAY)); // We don't want to see the speech bar while loading
    }

    public Integer getThemeId() {
        try {
            return getPackageManager().getActivityInfo(getComponentName(), 0).getThemeResource();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    // provide default values for boolean
    public void setFragment(Fragment fragment) {
        setFragment(fragment,false);
    }

    // main setFragment function
    public void setFragment(Fragment fragment, boolean back) {

        if (!currentFragment.equals(String.valueOf(fragment.getClass().getSimpleName()))) {

            FragmentTransaction transaction = fragmentManager.beginTransaction();

            if (back) {
                transaction.setCustomAnimations(R.anim.enter_fade_in_left, R.anim.exit_fade_out_right, R.anim.enter_fade_in_right, R.anim.exit_fade_out_left);
            } else
                transaction.setCustomAnimations(R.anim.enter_fade_in_right, R.anim.exit_fade_out_left, R.anim.enter_fade_in_left, R.anim.exit_fade_out_right);

            transaction.replace(R.id.placeholder, fragment, "currentFragment");
            transaction.addToBackStack(null);
            transaction.commit();

        }
        currentFragment = fragment.getClass().getSimpleName();
    }
}