package com.example.randomutils_dialogflow;

import android.util.Log;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DialogRequest {

    private static AccessToken token;
    private static final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static GoogleCredentials credentials;

    public static void auth(InputStream json) throws IOException {
        credentials = GoogleCredentials.fromStream(json).createScoped("https://www.googleapis.com/auth/dialogflow");
    }

    public static String request(String inputStr) throws Exception {
        credentials.refreshIfExpired();
        token = credentials.getAccessToken();

        /* {
              "query_input": {
                "text": {
                  "text": "I know french",
                  "language_code": "en-US"
                }
             }
           }
        */

        String requestJson = jsonBuild(inputStr);
        RequestBody body = RequestBody.create(requestJson,JSON);

        Request request = new Request.Builder()
                .url("https://dialogflow.googleapis.com/v2/projects/pepper-faq-wa9q/agent/sessions/12341:detectIntent")
                .header("Content-Type", "application/json; charset=utf-8")
                .addHeader("Authorization", "Bearer " + token.getTokenValue())
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            Log.d("Request code",Integer.toString(response.code()));
            if (response.code() == 200) {
                JsonObject jsonObject = new Gson().fromJson(Objects.requireNonNull(response.body()).string(), JsonObject.class);
                return jsonObject.get("queryResult").getAsJsonObject().get("fulfillmentText").getAsString();
            } else {
                Log.d("JSON Request:",response.body().string());
                return "error";
            }
        }
    }

    public static void run(MainActivity ma, String inputStr) throws Exception {

        credentials.refreshIfExpired();
        token = credentials.getAccessToken();

        /* example JSON request
        {
            "query_input": {
                "text": {
                  "text": "I know french",
                  "language_code": "en-US"
                }
            }
        }
        */

        String requestJson = jsonBuild(inputStr);
        RequestBody body = RequestBody.create(requestJson,JSON);

        Request request = new Request.Builder()
                .url("https://dialogflow.googleapis.com/v2/projects/pepper-faq-wa9q/agent/sessions/12341:detectIntent")
                .header("Content-Type", "application/json; charset=utf-8")
                .addHeader("Authorization", "Bearer " + token.getTokenValue())
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            Log.d("Request code",Integer.toString(response.code()));
            if (response.code() == 200) {
                JsonObject jsonObject = new Gson().fromJson(Objects.requireNonNull(response.body()).string(), JsonObject.class);
                String chatResp = jsonObject.get("queryResult").getAsJsonObject().get("fulfillmentText").getAsString();

                Log.d("chatresp",chatResp);

                List<String> values = ma.radioMap.keySet().stream()
                        .filter(key -> key.contains(chatResp))
                        .map(ma.radioMap::get)
                        .collect(Collectors.toList());

                if (values.isEmpty()) {
                    if (chatResp.equals("radiostop")) {
                        ma.currentChatBot.goToBookmarkSameTopic("radiostop");
                    } else {
                        ma.currentChatBot.setQiChatVariable("anysayer", chatResp);
                    }
                } else {
                    ma.currentChatBot.setQiChatVariable("radiostation", chatResp);
                    ma.currentChatBot.goToBookmarkSameTopic("radiostart");
                }
            } else {
                Log.d("JSON Request:",response.body().string());
            }
        }
    }

    private static String jsonBuild(String inputStr) {
        return "{'query_input': {" +
                "'text': {" +
                "'text': '"+inputStr+"', "+
                "'language_code': 'en-GB'"+
                "}"+
                "}"+
                "}";
    }

}
