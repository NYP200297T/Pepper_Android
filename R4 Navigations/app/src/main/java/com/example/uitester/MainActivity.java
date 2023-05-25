package com.example.uitester;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.actuation.AttachedFrame;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.OrientationPolicy;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.QiChatExecutor;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.aldebaran.qi.sdk.object.power.FlapSensor;
import com.aldebaran.qi.sdk.object.power.Power;
import com.aldebaran.qi.sdk.object.streamablebuffer.StreamableBuffer;
import com.example.uitester.Executors.BookmarkExecutor;
import com.example.uitester.Executors.CancelExecutor;
import com.example.uitester.Executors.FragmentExecutor;
import com.example.uitester.Executors.MoveExecutor;
import com.example.uitester.Fragments.LoadingFragment;
import com.example.uitester.Fragments.SplashFragment;
import com.example.uitester.Utils.ChatData;
import com.example.uitester.Utils.LocalizeAndMapHelper;
import com.example.uitester.Utils.RobotHelper;
import com.example.uitester.Utils.SaveFileHelper;
import com.example.uitester.Utils.Vector2theta;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "test";
    public SaveFileHelper saveFileHelper;
    public TreeMap<String, AttachedFrame> savedLocations = new TreeMap<>();
    private final AtomicBoolean load_location_success = new AtomicBoolean(false);
    public static final String filesDirectoryPath = Environment.getExternalStorageDirectory().getPath() + "/Maps/";
    public static final String mapFileName = "mapData.txt";
    public static final String LocationsFileName = "points.json";
    private static final int MULTIPLE_PERMISSIONS = 1;
    public QiContext qiContext;
    public RobotHelper robotHelper;
    private FragmentManager fragmentManager;
    private final LoadingFragment loadingFragment = new LoadingFragment();
    public ChatData currentChatBot, englishChatBot;
    private android.content.res.Configuration config;
    private Resources res;
    public List<String> routepoints;
    public Animate animate;
    private Future<Void> chatFuture;
    private final List<String> topicNames = Arrays.asList("chatbot", "concepts");
    public boolean amStuck = false;
    private AlertDialog chargeDialog;
    public String currentTargetLoc;

    private String currentFragment = "";

    public static final String map_missing = "Map missing, please map out area first.";
    public static final String open_charge = "Please close my charging flap";
    public static final String localise_error = "Retrying localise";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        robotHelper = new RobotHelper();
        saveFileHelper = new SaveFileHelper();
        res = getApplicationContext().getResources();
        config = res.getConfiguration();
        // Register the RobotLifecycleCallbacks to this Activity.

        if (this.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MULTIPLE_PERMISSIONS);
        }
        this.fragmentManager = getSupportFragmentManager();

        QiSDK.register(this, this);
        updateLocale("en");
        setContentView(R.layout.activity_main);
    }

    private void updateLocale(String strLocale) {
        java.util.Locale locale = new java.util.Locale(strLocale);
        config.setLocale(locale);
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    @Override
    protected void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        // The robot focus is gained.
        this.qiContext = qiContext;
        robotHelper.onRobotFocusGained(qiContext);

        runOnUiThread(() -> {
            setFragment(loadingFragment);
            loadingFragment.updateTextValue("Loading chatbot...");
        });

        Power power = qiContext.getPower();
        FlapSensor chargingFlap = power.getChargingFlap();

        if (chargingFlap.getState().getOpen()) {
            robotHelper.say("Please close my charging flap");
        }

        runOnUiThread(() -> loadingFragment.updateTextValue("Loading mapData..."));

        Log.d(TAG, "load mapData");
        StreamableBuffer mapData = saveFileHelper.readStreamableBufferFromFile(filesDirectoryPath, mapFileName);
        Log.d(TAG, "done loading mapData");

        // Build a new ExplorationMap.
        Log.d(TAG, "building map");

        runOnUiThread(() -> loadingFragment.updateTextValue("Building map from data..."));

        robotHelper.localizeAndMapHelper.setStreamableMap(mapData);
        robotHelper.localizeAndMapHelper.preBuildStreamableExplorationMap();
        //topicNames needs to be updated wih the topics names of the topics in the raw resource dir

        Log.d(TAG, "done building map");

        runOnUiThread(() -> loadingFragment.updateTextValue("Building other functions..."));

        Animation myAnimation = AnimationBuilder.with(qiContext)
                .withResources(R.raw.bowing_b001)
                .build();

        this.animate = AnimateBuilder.with(qiContext)
                .withAnimation(myAnimation)
                .build();

        File file = new File(filesDirectoryPath, LocationsFileName);
        List<Phrase> phraseList = new ArrayList<>();

        if (file.exists()) {
            Map<String, Vector2theta> vectors = saveFileHelper.getLocationsFromFile(filesDirectoryPath, LocationsFileName);

            // Clear current savedLocations.
            savedLocations = new TreeMap<>();
            Frame mapFrame = robotHelper.getMapFrame();

            // Build frames from the vectors.
            for (Map.Entry<String, Vector2theta> entry : vectors.entrySet()) {
                // Create a transform from the vector2theta.
                Transform t = entry.getValue().createTransform();

                // Create an AttachedFrame representing the current robot frame relatively to the MapFrame.
                AttachedFrame attachedFrame = mapFrame.async().makeAttachedFrame(t).getValue();

                // Store the FreeFrame.
                savedLocations.put(entry.getKey(), attachedFrame);
                load_location_success.set(true);

                phraseList.add(new Phrase(String.valueOf(entry.getKey())));
            }
            Log.d(TAG, "load all points success");
        } else {
            Log.d(TAG, "points.json does not exist");
        }

        routepoints = Arrays.asList("Point Charge", "Back Door", "R417", "R416", "Corridor Start", "Trophy 1", "Trophy 2", "R405", "R412", "Robotics Room", "Front Door", "Point Charge", "Charge");

        englishChatBot = new ChatData(this, qiContext, new Locale("en"), topicNames, true);
        Map<String, QiChatExecutor> executors = new HashMap<>();
        executors.put("FragmentExecutor", new FragmentExecutor(qiContext, this));
        executors.put("BookmarkExecutor", new BookmarkExecutor(qiContext, this));
        executors.put("MoveExecutor", new MoveExecutor(qiContext, this));
        executors.put("CancelExecutor", new CancelExecutor(qiContext, this));
        englishChatBot.setupExecutors(executors);
        currentChatBot = englishChatBot;
        currentChatBot.disableAnimations();
        currentChatBot.setDynamicConcept("locations", phraseList);

        // Hold the abilities asynchronously.
        Log.d(TAG, "holding autonomous functions");

        runOnUiThread(() -> {
            loadingFragment.updateTextValue("Localising...");
            AlertDialog.Builder chargeBuilder = buildDialog(open_charge, false);
            chargeDialog = chargeBuilder.create();
        });

        if (chargingFlap.getState().getOpen()) {
            runOnUiThread(chargeDialog::show);
            robotHelper.say(open_charge);
            while (true) {
                if (!chargingFlap.getState().getOpen()) {
                    runOnUiThread(chargeDialog::cancel);
                    break;
                }
            }
        }

        // Add an on status changed listener on the Localize action to know when the robot is localized in the map.
        robotHelper.localizeAndMapHelper.addOnFinishedLocalizingListener(status -> {

            robotHelper.releaseAbilities();
            initChat();
            AlertDialog.Builder dialogBuilder = null;

            if (status.equals(LocalizeAndMapHelper.LocalizationStatus.FAILED)) {

                currentChatBot.setQiChatVariable("anysayer",localise_error);
                dialogBuilder = buildDialog(localise_error, false);
                dialogBuilder.setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    localise();
                });

            } else if (status.equals(LocalizeAndMapHelper.LocalizationStatus.MAP_MISSING)) {

                currentChatBot.setQiChatVariable("anysayer",map_missing);
                dialogBuilder = buildDialog(map_missing, false);
                dialogBuilder.setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    this.finishAffinity();
                });

            }

            if (dialogBuilder != null) {

                AlertDialog.Builder finalDialogBuilder = dialogBuilder;
                runOnUiThread(() -> {
                    AlertDialog dialoger = finalDialogBuilder.create();
                    dialoger.show();
                });

                currentChatBot.goToBookmarkSameTopic("anysay");

            } else {

                chargingFlap.addOnStateChangedListener(flapState -> {
                    if (flapState.getOpen() & amStuck) {
                        runOnUiThread(chargeDialog::show);
                        currentChatBot.goToBookmarkSameTopic("charge");
                        while (true) {
                            if (!chargingFlap.getState().getOpen()) {
                                runOnUiThread(chargeDialog::cancel);
                            }
                        }
                    }
                });
            }
        });

        // Localise and end of setup
        localise();
        Log.i(TAG, "Localizing...");
    }

    @Override
    public void onRobotFocusLost() {
        // The robot focus is lost.
        this.qiContext = null;
        robotHelper.localizeAndMapHelper.removeOnFinishedLocalizingListeners();
        robotHelper.goToHelper.removeOnFinishedMovingListeners();
        chatFuture.requestCancellation();
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissionsList, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissionsList, grantResults);
        if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            QiSDK.register(this, this);
        } else finishAndRemoveTask();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.OVERLAY); // We don't want to see the speech bar while loading
        this.setFragment(loadingFragment);
    }

    public Integer getThemeId() {
        try {
            return getPackageManager().getActivityInfo(getComponentName(), 0).getThemeResource();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initChat() {
        chatFuture = currentChatBot.chat.async().run();
        chatFuture.thenConsume(future -> {
            if (future.hasError()) {
                Log.e(TAG, "Discussion finished with error.", future.getError());
            }
        });

        runOnUiThread(() -> {
            setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.ALWAYS);
            setFragment(new SplashFragment());
        });

        currentChatBot.goToBookmarkNewTopic("init","chatbot");
    }

    public void moveToLocation(String locate) {
        Log.d(TAG, "moving to " + locate);

        AttachedFrame goToFrame = savedLocations.get(locate);
        assert goToFrame != null;
        robotHelper.holdAbilities(false).andThenConsume(useless ->
                robotHelper.goToHelper.goTo(goToFrame, false, true, OrientationPolicy.ALIGN_X).andThenConsume(useless2 ->
                        robotHelper.releaseAbilities()
                ));
    }

    public void cancelMoveToLocation() {
        robotHelper.goToHelper.checkAndCancelCurrentGoto();
    }

    public void localise() {
        robotHelper.holdAbilities(false).andThenConsume((useless) ->
                robotHelper.localizeAndMapHelper.animationToLookInFront().andThenConsume(aVoider ->
                        robotHelper.say("Please stay away from me while I localise myself").andThenConsume(aVoid ->
                                robotHelper.localizeAndMapHelper.localize())));
    }

    // provide default values for title
    public AlertDialog.Builder buildDialog(String text, boolean cancelable) {
        return buildDialog("Notice",text,cancelable);
    }

    // main buildDialog function
    public AlertDialog.Builder buildDialog(String title, String text, boolean cancelable) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(text);
        builder.setCancelable(cancelable);

        // create and show the alert dialog
        return builder;
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