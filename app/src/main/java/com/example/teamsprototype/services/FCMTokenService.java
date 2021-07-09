package com.example.teamsprototype.services;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.teamsprototype.activities.IncomingCall;
import com.example.teamsprototype.utilities.AppConstants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMTokenService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Intent intent;
        String type = remoteMessage.getData().get(AppConstants.TYPE);

        if(type != null) {
            if (type.equals(AppConstants.INVITE)) {
                String name = remoteMessage.getData().get(AppConstants.NAME);
                String inviter_token = remoteMessage.getData().get(AppConstants.REMOTE_MSG_INVITER_TOKEN);
                String channelName = remoteMessage.getData().get("channelName");
                String token = remoteMessage.getData().get("token");

                intent = new Intent(getApplicationContext(), IncomingCall.class);
                intent.putExtra("name", name);
                intent.putExtra("token", token);
                intent.putExtra("channelName", channelName);
                intent.putExtra(AppConstants.REMOTE_MSG_INVITER_TOKEN, inviter_token);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            else if (type.equals(AppConstants.REMOTE_MSG_RESPONSE)){
                String response = remoteMessage.getData().get(AppConstants.REMOTE_MSG_RESPONSE);
                intent = new Intent(AppConstants.REMOTE_MSG_RESPONSE);
                intent.putExtra(AppConstants.REMOTE_MSG_RESPONSE, response);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        }
    }
}
