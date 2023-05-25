package org.ishoot.dynaflow;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.actuation.AttachedFrame;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.OrientationPolicy;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;
import com.aldebaran.qi.sdk.object.power.FlapSensor;
import com.aldebaran.qi.sdk.object.power.Power;
import com.aldebaran.qi.sdk.object.streamablebuffer.StreamableBuffer;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.ishoot.dynaflow.Fragments.AdminFragment;
import org.ishoot.dynaflow.Fragments.LoadingFragment;
import org.ishoot.dynaflow.Fragments.MenuFragment;
import org.ishoot.dynaflow.Fragments.NavigateFragment;
import org.ishoot.dynaflow.Fragments.PicTextViewFragment;
import org.ishoot.dynaflow.Utils.AdminConsoleButton;
import org.ishoot.dynaflow.Utils.CountDownRestartButton;
import org.ishoot.dynaflow.Utils.FileUtils;
import org.ishoot.dynaflow.Utils.HumanAwareManager;
import org.ishoot.dynaflow.Utils.LocalizeAndMapHelper;
import org.ishoot.dynaflow.Utils.RobotHelper;
import org.ishoot.dynaflow.Utils.SaveFileHelper;
import org.ishoot.dynaflow.Utils.Vector2theta;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    // initialise class wide variables
    public QiContext qiContext;
    private static final String TAG = "DynaFlow";
    private FragmentManager fragmentManager;
    public String currentFragment = "";
    private android.content.res.Configuration config;
    private Resources res;
    public static final String sdCardPath = Environment.getExternalStorageDirectory().getPath();
    public Map<String, Fragment> fragmentMap = new HashMap<>();
    public FlapSensor chargingFlap;
    private final LoadingFragment loadingFragment = new LoadingFragment();

    public static String mapsDirectoryPath = sdCardPath + "/Maps/";
    public static String flowJsonPath;
    public static String imagesPath = sdCardPath + "/DynaFlow/images";
    public static String mapFileName = "mapData.txt";
    public static String LocationsFileName = "points.json";
    public Map json;
    public Map<String,Map> fragmentJson;

    private static final int MULTIPLE_PERMISSIONS = 2;
    public TreeMap<String, AttachedFrame> savedLocations = new TreeMap<>();
    public RobotHelper robotHelper;
    public SaveFileHelper saveFileHelper;
    public boolean amStuck = false;
    private AlertDialog chargeDialog;
    public String currentTargetLoc;

    private static final int FILE_SELECT_CODE = 0;
    private static final String map_missing = "Map missing, please map out area first.";
    private static final String open_charge = "Please close my charging flap";
    private static final String localise_error = "Retrying localise";

    private final CountDownRestartButton countDownRestartButton = new CountDownRestartButton(this,1000,100);
    private final AdminConsoleButton adminConsolebutton = new AdminConsoleButton(this,1000,100);

    public HumanAwareness humanAwareness;
    public boolean hoomanAware;
    public HumanAwareManager humanAwareManager;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check for read & write permission
        if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED & this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MULTIPLE_PERMISSIONS);
        }

        // setup robot helpers and methods
        robotHelper = new RobotHelper();
        saveFileHelper = new SaveFileHelper();
        res = getApplicationContext().getResources();
        config = res.getConfiguration();
        this.fragmentManager = getSupportFragmentManager();
        setContentView(R.layout.activity_main);

        // setup QiSDK dependencies
        QiSDK.register(this, this);
        updateLocale("en");
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
    public void onRobotFocusGained(@NonNull QiContext qiContext) {
        // The robot focus is gained.
        this.qiContext = qiContext;
        robotHelper.onRobotFocusGained(qiContext);

        // get last saved location of flow.json
        sharedPreferences = getSharedPreferences("flowjsonlocation", MODE_PRIVATE);
        flowJsonPath = sharedPreferences.getString("location",null);
        if (flowJsonPath == null) {
            editJsonLocation(sdCardPath + "/DynaFlow/flow.json");
        }

        //check if json exists
        File file = new File(flowJsonPath);
        if (file.exists()) {
            json = readJson(flowJsonPath);
            fragmentJson = (Map) json.get("fragments");
        } else {
            runOnUiThread(() -> {
                Toast.makeText(this, "Please choose json file", Toast.LENGTH_SHORT).show();
                showFileChooser();
            });
        }

        // set LoadingFragment to load fragments/other essentials
        runOnUiThread(() -> {
            setFragment(loadingFragment);
            setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        });

        // run setup
        setupFlows();
    }

    @Override
    public void onRobotFocusLost() {
        // The robot focus is lost.
        robotHelper.localizeAndMapHelper.removeOnFinishedLocalizingListeners();
        robotHelper.goToHelper.checkAndCancelCurrentGoto();
        if (humanAwareness != null) {
            humanAwareness.async().removeAllOnEngagedHumanChangedListeners();
        }
        this.qiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }

    @Override
    public void onUserInteraction() {
        if (humanAwareManager != null) {
            if (currentFragment.equals("SplashFragment")) {
                runOnUiThread(() -> nextFragment("home"));
                humanAwareManager.start();
            } else {
                humanAwareManager.reset();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        runOnUiThread(() -> setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.OVERLAY)); // We don't want to see the speech bar while loading
        this.setFragment(new LoadingFragment());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissionsList, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissionsList, grantResults);
        if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED & this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            QiSDK.register(this, this);
        } else finishAndRemoveTask();
    }

    public Integer getThemeId() {
        try {
            return getPackageManager().getActivityInfo(getComponentName(), 0).getThemeResource();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public Fragment getFragment() {
        return fragmentManager.findFragmentByTag("currentFragment");
    }

    public Map readJson(String filename) {
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assert reader != null;
        Map map = new Gson().fromJson(reader, Map.class);
        return map;
    }

    // provide default values for boolean
    public void setFragment(Fragment fragment) {
        setFragment(fragment,false,false);
    }

    // main setFragment function
    public void setFragment(Fragment fragment, boolean animation, boolean back) {

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (animation) {
            if (back) {
                transaction.setCustomAnimations(R.anim.enter_fade_in_left, R.anim.exit_fade_out_right, R.anim.enter_fade_in_right, R.anim.exit_fade_out_left);
            } else
                transaction.setCustomAnimations(R.anim.enter_fade_in_right, R.anim.exit_fade_out_left, R.anim.enter_fade_in_left, R.anim.exit_fade_out_right);
        }

        transaction.replace(R.id.placeholder, fragment, "currentFragment");
        transaction.addToBackStack(null);
        transaction.commit();
        currentFragment = fragment.getClass().getSimpleName();

    }

    public void nextFragment(String next) {
        Log.d(TAG,next);

        if (currentFragment.equals("MenuFragment")) {
            MenuFragment cancelFrag = (MenuFragment) getFragment();
            runOnUiThread(cancelFrag::killListen);
        }

        if (currentFragment.equals("NavigateFragment")) {
            NavigateFragment cancelFrag = (NavigateFragment) getFragment();
            runOnUiThread(cancelFrag::killListen);
        }

        if (currentFragment.equals("PicTextViewFragment")) {
            PicTextViewFragment cancelFrag = (PicTextViewFragment) getFragment();
            runOnUiThread(cancelFrag::killListen);
        }

        if (humanAwareness != null) {
            if (humanAwareness.getEngagedHuman() != null) {
                humanAwareManager.reset();
            }
        }

        if (next.equals("home")) {
            if (currentFragment.equals("NavigateFragment")) cancelMoveToLocation();
            runOnUiThread(() -> setFragment(fragmentMap.get(json.get("start"))));
        } else {
            fragmentMap.get(currentFragment);
            runOnUiThread(() -> setFragment(fragmentMap.get(next)));
        }
    }

    public void setupFlows() {

        // unused due to uncertainty
        if (json.containsKey("humanAware")) {
            hoomanAware = (boolean) json.get("humanAware");
        } else {
            hoomanAware = false;
        }
        humanAwareManager = new HumanAwareManager(this, hoomanAware);

        // load Fragments
        for (String key : fragmentJson.keySet()) {

            Map obj = fragmentJson.get(key);
            String kind = (String) obj.get("kind");

            switch (kind) {
                case "pictextview": {
                    PicTextViewFragment fragment = new PicTextViewFragment();

                    fragment.setInfo(qiContext,obj);
                    fragmentMap.put(key,fragment);
                    break;
                }
                case "menuview": {
                    MenuFragment fragment = new MenuFragment();

                    Map buttonjson = (Map) obj.get("buttons");
                    int menusize = buttonjson.size();

                    // load layout containing n number of buttons
                    String menukind = "fragment_menu" + menusize;
                    int menuID = getResId(menukind, R.layout.class);
                    fragment.setInfo(qiContext,obj,menuID);

                    fragmentMap.put(key,fragment);
                    break;
                }
                case "navigateview": {
                    NavigateFragment fragment = new NavigateFragment();

                    fragment.setInfo(qiContext,obj);
                    fragmentMap.put(key,fragment);
                    break;
                }
            }
        }

        fragmentMap.put("admin", new AdminFragment());

        if (json.containsKey("mapSettings")) {

            Map mapJson = (Map) json.get("mapSettings");
            mapFileName = (String) mapJson.get("map");
            LocationsFileName = (String) mapJson.get("points");
            mapsDirectoryPath = sdCardPath + mapJson.get("directory");

            File f = new File(mapsDirectoryPath, mapFileName);
            if (f.exists()) {
                StreamableBuffer mapData = saveFileHelper.readStreamableBufferFromFile(mapsDirectoryPath, mapFileName);
                if(robotHelper.localizeAndMapHelper.getStreamableMap()==null)
                {
                    robotHelper.localizeAndMapHelper.setStreamableMap(mapData);
                    robotHelper.localizeAndMapHelper.preBuildStreamableExplorationMap();
//                    robotHelper.localizeAndMapHelper.getExplorationMapBitmap().andThenConsume(bitmap -> {
//                        MapTopGraphicalRepresentation mapGraphicalRepresentation = robotHelper.localizeAndMapHelper.explorationMap.getTopGraphicalRepresentation();
//                        float scale = mapGraphicalRepresentation.getScale();
//                        float theta = mapGraphicalRepresentation.getTheta();
//                        float x = mapGraphicalRepresentation.getX();
//                        float y = mapGraphicalRepresentation.getY();
//
//                        Map<String,Float> mapInfo = new HashMap<>();
//                        mapInfo.put("scale",scale);
//                        mapInfo.put("theta",theta);
//                        mapInfo.put("x",x);
//                        mapInfo.put("y",y);
//                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//                        String jsonwriter = new File(mapsDirectoryPath, "mapinfo.json").getPath();
//                        try (Writer writer = new FileWriter(jsonwriter)) {
//                            gson.toJson(mapInfo, writer);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//
//                        File saver = new File(mapsDirectoryPath, mapImageFileName);
//                        try (FileOutputStream out = new FileOutputStream(saver)) {
//                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//
//                    });
                }
                setupMap();
                localise();
            }

        } else {
            initDone();
        }
    }

    public void initDone() {
        runOnUiThread(() -> setFragment(fragmentMap.get(json.get("start"))));
        this.countDownRestartButton.setupButton();
        this.adminConsolebutton.setupButton();
    }

    public QiContext getQiContext() {
        return qiContext;
    }

    private void setupMap() {
        Map<String, Vector2theta> vectors = saveFileHelper.getLocationsFromFile(mapsDirectoryPath, LocationsFileName);

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
        }
    }

    // default parameter values
    public void moveToLocation(String locate) {
        moveToLocation(locate,false,false,true);
    }

    public void moveToLocation(String locate, boolean bgMove, boolean straight, boolean maxSpd) {
        Log.d(TAG, "moving to " + locate);

        AttachedFrame goToFrame = savedLocations.get(locate);
        assert goToFrame != null;
        robotHelper.holdAbilities(bgMove).andThenConsume(useless ->
                robotHelper.goToHelper.goTo(goToFrame, straight, maxSpd, OrientationPolicy.ALIGN_X).andThenConsume(useless2 ->
                        robotHelper.releaseAbilities()
                ));
    }

    public void cancelMoveToLocation() {
        robotHelper.goToHelper.checkAndCancelCurrentGoto();
        robotHelper.goToHelper.removeOnFinishedMovingListeners();
    }

    public void localise() {

        Power power = qiContext.getPower();
        chargingFlap = power.getChargingFlap();

        if (chargingFlap.getState().getOpen()) {
            runOnUiThread(chargeDialog::show);
            while (true) {
                if (!chargingFlap.getState().getOpen()) {
                    runOnUiThread(chargeDialog::cancel);
                    break;
                }
            }
        }

        runOnUiThread(() -> loadingFragment.updateTextValue("Localising"));

        robotHelper.localizeAndMapHelper.addOnFinishedLocalizingListener(status -> {

            robotHelper.releaseAbilities();
            initDone();

            AlertDialog.Builder dialogBuilder = null;
            if (status.equals(LocalizeAndMapHelper.LocalizationStatus.FAILED)) {

                dialogBuilder = buildDialog(localise_error, false);
                dialogBuilder.setPositiveButton("Try again", (dialog, which) -> {
                    dialog.dismiss();
                });
                dialogBuilder.setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                    this.finishAffinity();
                });

            } else if (status.equals(LocalizeAndMapHelper.LocalizationStatus.MAP_MISSING)) {

                dialogBuilder = buildDialog(map_missing,false);
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

            } else {

                chargingFlap.addOnStateChangedListener(flapState -> {
                    if (flapState.getOpen() & amStuck) {
                        runOnUiThread(chargeDialog::show);
                        while (true) {
                            if (!chargingFlap.getState().getOpen()) {
                                runOnUiThread(chargeDialog::cancel);
                            }
                        }
                    }
                });
            }
        });

        robotHelper.holdAbilities(false).andThenConsume((useless) ->
                robotHelper.localizeAndMapHelper.animationToLookInFront().andThenConsume(aVoid ->
                                robotHelper.localizeAndMapHelper.localize()));
    }

    public void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/octet-stream");

        try {
            startActivityForResult(Intent.createChooser(intent, "Choose json file"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
            this.finishAffinity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_SELECT_CODE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                editJsonLocation(FileUtils.getPath(uri,this));
                this.json = readJson(flowJsonPath);
                this.fragmentJson = (Map) json.get("fragments");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void editJsonLocation(String location) {
        flowJsonPath = location;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("location", location);
        editor.apply();
    }

    public String getImagePath(String path) {
        return sdCardPath + json.get("imagepath") + path;
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

    public int getBatteryLevel() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getBaseContext().registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        return level;
    }

    // do nothing for back button press
    // override return to previous fragment
    @Override
    public void onBackPressed() { }

}