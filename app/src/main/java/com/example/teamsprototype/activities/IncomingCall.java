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

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncomingCall extends AppCompatActivity {

    String remote_name, local_name, uid, caller_token;
    TextView caller_id, caller_name;
    FloatingActionButton acceptCall, rejectCall;
    String channelName, token;
    Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        preferences = new Preferences(getApplicationContext());
        uid = preferences.getString(AppConstants.USER_ID);
        local_name = preferences.getString(AppConstants.NAME);

        remote_name = getIntent().getStringExtra("name");
        channelName = getIntent().getStringExtra("channelName");
        token = getIntent().getStringExtra("token");
        caller_token = getIntent().getStringExtra(AppConstants.REMOTE_MSG_INVITER_TOKEN);

        caller_id = findViewById(R.id.caller_id);
        caller_name = findViewById(R.id.caller_name);

        caller_name.setText(remote_name);
        caller_id.setText(remote_name.substring(0,1));

        acceptCall = findViewById(R.id.accept_call);
        rejectCall = findViewById(R.id.reject_call);

        acceptCall.setOnClickListener(v -> {
            sendInvitationResponse(caller_token, AppConstants.ACCEPT);
            Intent intent = new Intent(getBaseContext(), CallActivity.class);
            intent.putExtra("channelName", channelName);
            intent.putExtra("token", token);
            intent.putExtra("uid", uid);
            intent.putExtra("name", local_name);
            startActivity(intent);
            finish();
        });

        rejectCall.setOnClickListener(v -> {
            sendInvitationResponse(caller_token, AppConstants.REJECT);
            finish();
        });
    }

    private void sendInvitationResponse(String receiverToken, String type){
        try{
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(AppConstants.TYPE, AppConstants.REMOTE_MSG_RESPONSE);
            data.put(AppConstants.REMOTE_MSG_RESPONSE, type);

            body.put(AppConstants.REMOTE_MSG_DATA, data);
            body.put(AppConstants.REMOTE_MSG_REGISTRATION_ID, tokens);

            sendRemoteMessage(body.toString(), type);

        }catch (Exception e){
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private final BroadcastReceiver responseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(AppConstants.REMOTE_MSG_RESPONSE);
            if(type.equals(AppConstants.CANCEL)){
                    finish();
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
                    if(type.equals(AppConstants.ACCEPT)){
                        Toast.makeText(IncomingCall.this, "Joining Call", Toast.LENGTH_SHORT).show();
                    } else if(type.equals(AppConstants.REJECT)){
                        Toast.makeText(IncomingCall.this, "Call Rejected", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(IncomingCall.this, response.message(), Toast.LENGTH_SHORT).show();
                }
                finish();
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(IncomingCall.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}