package com.example.randomutils.Executors;

import android.util.Log;
import android.widget.TextView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;
import com.example.randomutils.MainActivity;
import com.example.randomutils.R;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * FragmentExecutor sets the fragment to be displayed in the placeholder of the main activity
 * This executor is added to the Chat(see main activity)
 * Triggered in qiChat as follow : ^execute( FragmentExecutor, frag_XXXX )
 */

public class WeatherExecutor extends BaseQiChatExecutor {
    private final MainActivity ma;
    private String TAG = "MSI_WeatherExecutor";
    private final String apiKey = null;

    public WeatherExecutor(QiContext qiContext, MainActivity mainActivity) {
        super(qiContext);
        this.ma = mainActivity;
    }

    @Override
    public void runWith(List<String> params) {

        String country = this.ma.currentChatBot.getQiChatVariable("country");
        Log.d(TAG,country);
        String urlQueryGeo = "http://api.openweathermap.org/geo/1.0/direct?limit=1&q=" + country + "&appid=" + apiKey;
        String urlQueryWeather = "https://api.openweathermap.org/data/2.5/weather?appid=" + apiKey + "&units=metric";

        HttpURLConnection conn = null;
        HttpURLConnection conn2 = null;
        Map weatherMap = null;
        URL urlWeather = null;
        String line;
        StringBuffer responseContent = new StringBuffer();

        try {
            URL urlGeo = new URL(urlQueryGeo);
            conn = (HttpURLConnection) urlGeo.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);// 5000 milliseconds = 5 seconds
            conn.setReadTimeout(5000);

            // Test if the response from the server is successful
            int status = conn.getResponseCode();
            Log.i(TAG, "response code1: " + status);

            if (status >= 300) {
                Log.e(TAG, "Oops HTML Code 300+");
            }
            else {
                Log.d(TAG,"Running request");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();

                List localList = new Gson().fromJson(responseContent.toString(), List.class);
                Map localMap = (Map) localList.get(0);

                String lat = String.valueOf(localMap.get("lat"));
                String lon = String.valueOf(localMap.get("lon"));

                Log.d(TAG,lat);
                Log.d(TAG,lon);

                urlWeather = new URL(urlQueryWeather+"&lat="+lat+"&lon="+lon);
            }

            conn2 = (HttpURLConnection) urlWeather.openConnection();
            conn2.setRequestMethod("GET");

            // Test if the response from the server is successful
            int status2 = conn2.getResponseCode();
            Log.i(TAG, "response code2: " + status2);

            if (status2 >= 300) {
                Log.e(TAG, "Oops HTML Code 300+");
            } else {
                Log.d(TAG,"Running request");

                responseContent = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();
                weatherMap = new Gson().fromJson(responseContent.toString(), Map.class);
            }

        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
        } finally {
            Log.d(TAG,"done");
            conn.disconnect();
            if (conn2 != null) {
                conn2.disconnect();
            }
        }

        Log.d(TAG,String.valueOf(weatherMap));

        if (weatherMap != null) {

            List w1 = (List) weatherMap.get("weather");
            Map w2 = (Map) w1.get(0);
            String weather = String.valueOf(w2.get("description"));

            Map t1 = (Map) weatherMap.get("main");
            String temp = String.valueOf(t1.get("temp"));

            this.ma.runOnUiThread(() -> {
                TextView textWeather = this.ma.findViewById(R.id.textviewer_weather);
                textWeather.setText("Temperature: "+temp+"°C"+"\nWeather: "+weather+"\nCountry: "+country);
            });

            this.ma.currentChatBot.setQiChatVariable("temperature", temp);
            this.ma.currentChatBot.setQiChatVariable("weather", weather);
            this.ma.currentChatBot.goToBookmarkSameTopic("countrysay");
        }
    }

    @Override
    public void stop() {

    }
}