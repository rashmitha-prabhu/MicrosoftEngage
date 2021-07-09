package com.example.teamsprototype.services;

import android.util.Log;

import com.example.teamsprototype.utilities.AppConstants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import static android.content.ContentValues.TAG;

public class Tokens{
    static String token = null;

    public static String createToken(String channelName, int uid) {
//        Generates the agora user token from the web service deployed on azure and returns the token
        
        String url = "https://agoratokens.azurewebsites.net/access_token?channelName=" + channelName + "&uid=" + uid;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        CountDownLatch countDownLatch = new CountDownLatch(1);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if(response.isSuccessful()){
                    String t = response.body().string();
                    try {
                        JSONObject object = new JSONObject(t);
                        token = object.getString("token");
                        Log.d("Tokens", token);
                        countDownLatch.countDown();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return token;
    }
}
