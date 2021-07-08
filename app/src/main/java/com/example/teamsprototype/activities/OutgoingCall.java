package com.example.teamsprototype.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.teamsprototype.R;
import com.example.teamsprototype.services.ApiClient;
import com.example.teamsprototype.services.ApiService;
import com.example.teamsprototype.utilities.AppConstants;
import com.example.teamsprototype.utilities.Preferences;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutgoingCall extends AppCompatActivity {

    Preferences preferences;
    String local_token, receiver_token, channelName, token, name, receiverUid, senderUid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_call);

        preferences = new Preferences(getApplicationContext());
        local_token = preferences.getString(AppConstants.FCM_TOKEN);

        name = getIntent().getStringExtra("name");
        senderUid = getIntent().getStringExtra("uid");
        receiverUid = getIntent().getStringExtra("receiver_uid");
        channelName = getIntent().getStringExtra("channelName");
        token = getIntent().getStringExtra("token");

        FirebaseFirestore.getInstance().collection(AppConstants.KEY_COLLECTION)
                .document(receiverUid).get().addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult()!=null){
                        receiver_token = task.getResult().getString(AppConstants.FCM_TOKEN);
                        if(receiver_token!=null) {
                            initiateMeeting(receiver_token);
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "User unavailable", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "User unavailable", Toast.LENGTH_SHORT).show();
                    }
                });

        TextView remote_id = findViewById(R.id.remote_id);
        remote_id.setText(name.substring(0, 1));

        TextView remote_name = findViewById(R.id.remote_name);
        remote_name.setText(name);

        FloatingActionButton end_call = findViewById(R.id.stop_call);
        end_call.setOnClickListener(v -> cancelMeeting(receiver_token));
    }

    private void initiateMeeting(String receiverToken){
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(AppConstants.TYPE, AppConstants.INVITE);
            data.put(AppConstants.NAME, preferences.getString(AppConstants.NAME));
            data.put(AppConstants.REMOTE_MSG_INVITER_TOKEN, local_token);
            data.put("token", token);
            data.put("channelName", channelName);

            body.put(AppConstants.REMOTE_MSG_DATA, data);
            body.put(AppConstants.REMOTE_MSG_REGISTRATION_ID, tokens);

            sendRemoteMessage(body.toString(), AppConstants.INVITE);
        } catch (Exception e){
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void cancelMeeting(String receiverToken){
        try{
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(AppConstants.TYPE, AppConstants.REMOTE_MSG_RESPONSE);
            data.put(AppConstants.REMOTE_MSG_RESPONSE, AppConstants.CANCEL);

            body.put(AppConstants.REMOTE_MSG_DATA, data);
            body.put(AppConstants.REMOTE_MSG_REGISTRATION_ID, tokens);

            sendRemoteMessage(body.toString(), AppConstants.REMOTE_MSG_RESPONSE);

        }catch (Exception e){
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private final BroadcastReceiver responseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(AppConstants.REMOTE_MSG_RESPONSE);
            switch (type){
                case AppConstants.ACCEPT:
                    Intent i = new Intent(getApplicationContext(), CallActivity.class);
                    i.putExtra("channelName", channelName);
                    i.putExtra("token", token);
                    i.putExtra("uid", preferences.getString(AppConstants.USER_ID));
                    i.putExtra("name", preferences.getString(AppConstants.NAME));
                    startActivity(i);
                    finish();
                    break;

                case AppConstants.REJECT:
                    Toast.makeText(getApplicationContext(), "Call Ended", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                responseReceiver,
                new IntentFilter(AppConstants.REMOTE_MSG_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                responseReceiver
        );
    }

    private void sendRemoteMessage(String remoteMessageBody, String type){
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                AppConstants.getRemoteMessageHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if(response.isSuccessful()){
                    if (type.equals(AppConstants.REMOTE_MSG_RESPONSE)) {
                        finish();
                    }
                } else {
                    Toast.makeText(OutgoingCall.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(OutgoingCall.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}