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
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import static android.content.ContentValues.TAG;

public class Tokens{
    static boolean success = false;
    static String token = null;

    public static boolean createToken(String channelName, int uid) {
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
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        HashMap<String, Object> token_instance = new HashMap<>();
                        token_instance.put("token", token);

                        db.collection(AppConstants.TOKENS)
                                .document(channelName)
                                .set(token_instance)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                                .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
                        success = true;
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
        return success;
    }
}
