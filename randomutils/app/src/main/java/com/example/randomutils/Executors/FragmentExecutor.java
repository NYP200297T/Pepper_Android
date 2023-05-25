package com.example.randomutils.Executors;

import android.util.Log;

import androidx.fragment.app.Fragment;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;
import com.example.randomutils.Fragments.HelloFragment;
import com.example.randomutils.Fragments.RadioFragment;
import com.example.randomutils.Fragments.SplashFragment;
import com.example.randomutils.Fragments.TimeFragment;
import com.example.randomutils.Fragments.WeatherFragment;
import com.example.randomutils.MainActivity;

import java.util.List;

/**
 * FragmentExecutor sets the fragment to be displayed in the placeholder of the main activity
 * This executor is added to the Chat(see main activity)
 * Triggered in qiChat as follow : ^execute( FragmentExecutor, frag_XXXX )
 */

public class FragmentExecutor extends BaseQiChatExecutor {
    private final MainActivity ma;
    private String TAG = "MSI_FragmentExecutor";

    public FragmentExecutor(QiContext qiContext, MainActivity mainActivity) {
        super(qiContext);
        this.ma = mainActivity;
    }

    @Override
    public void runWith(List<String> params) {
        String fragmentName;
        String optionalData; //use this if you need to pass on data when setting the fragment.
        if (params == null || params.isEmpty()) {
            return;
        } else {
            fragmentName = params.get(0);
            if(params.size() == 2){
                optionalData = params.get(1);
            }
        }
        Fragment fragment;
        Log.d(TAG,"fragmentName :" + fragmentName);
        switch (fragmentName){
            case ("frag_hello"):
                fragment = new HelloFragment();
                break;
            case ("frag_splash"):
                fragment = new SplashFragment();
                break;
            case ("frag_time"):
                fragment = new TimeFragment();
                break;
            case ("frag_radio"):
                fragment = new RadioFragment();
                break;
            case ("frag_weather"):
                fragment = new WeatherFragment();
                break;
            default:
                fragment = new SplashFragment();
                break;
        }
        ma.setFragment(fragment);
    }

    @Override
    public void stop() {

    }
}