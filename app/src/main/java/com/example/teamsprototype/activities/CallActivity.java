package com.example.teamsprototype.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.teamsprototype.R;
import com.example.teamsprototype.utilities.AppConstants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.models.UserInfo;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class CallActivity extends AppCompatActivity{

    private RtcEngine mRtcEngine;
    RelativeLayout remoteContainer;
    FrameLayout localContainer;
    ImageView localUser;
    TextView localName, remoteName;
    SurfaceView localView, remoteView;
    FloatingActionButton mute_btn, video_btn, end_call_btn, switchCam_btn, chat_btn;
    String channelName, token, local_Id, remote_Id, remote_name, name;

    boolean mute = true;
    boolean cam = false;
    boolean end_call = false;

    private final IRtcEngineEventHandler mRtcHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
        }

        @Override
        public void onLocalUserRegistered(int uid, String userAccount) {
            super.onLocalUserRegistered(uid, userAccount);
        }

        @Override
        public void onRejoinChannelSuccess(String channel, final int uid, int elapsed){
            super.onRejoinChannelSuccess(channel, uid, elapsed);
        }

        @Override
        public void onUserJoined(final int uid, int elapsed){
            super.onUserJoined(uid, elapsed);
        }

        @Override
        public void onUserInfoUpdated(int uid, UserInfo userInfo) {
            super.onUserInfoUpdated(uid, userInfo);
            runOnUiThread(() -> {
                remote_Id = userInfo.userAccount;
                remoteUserJoined();
            });
        }

        @Override
        public void onUserOffline(final int uid, int reason) {
            super.onUserOffline(uid, reason);
            runOnUiThread(() -> {
                removeRemoteVideo();
                remoteUserLeft();
            });
        }

        @Override
        public void onRemoteVideoStateChanged(final int uid, int state, int reason, int elapsed) {
            super.onRemoteVideoStateChanged(uid, state, reason, elapsed);

            if(state == Constants.REMOTE_VIDEO_STATE_STOPPED){
                runOnUiThread(() -> removeRemoteVideo());
            }
            else if (state == Constants.REMOTE_VIDEO_STATE_DECODING || state == Constants.REMOTE_VIDEO_STATE_STARTING){
                runOnUiThread(() -> setupRemoteVideo(uid));
            }
        }

        @Override
        public void onTokenPrivilegeWillExpire(String token){
            super.onTokenPrivilegeWillExpire(token);
            mRtcEngine.renewToken(token);
        }
    };

    private void getAllPermissions() {
        String[] requiredPermissions = new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        ArrayList<String> permissionsToAskFor = new ArrayList<>();
        for (String permission : requiredPermissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToAskFor.add(permission);
            }
        }
        if (!permissionsToAskFor.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToAskFor.toArray(new String[0]), 1);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        Intent intent = getIntent();
        channelName = intent.getStringExtra("channelName");
        token = intent.getStringExtra("token");
        local_Id = intent.getStringExtra("uid");
        name = intent.getStringExtra("name");

        chat_btn = findViewById(R.id.chat);
        mute_btn = findViewById(R.id.mic);
        video_btn = findViewById(R.id.video);
        switchCam_btn = findViewById(R.id.switchCam);
        end_call_btn = findViewById(R.id.hangUp);

        localContainer = findViewById(R.id.localVideo);
        localUser = findViewById(R.id.local_user);
        localName = findViewById(R.id.localUsername);
        localName.setText(name);

        remoteContainer = findViewById(R.id.remoteVideo);
        remoteName = findViewById(R.id.remoteUsername);

        chat_btn.setOnClickListener(v -> chat_view());
        mute_btn.setOnClickListener(v -> audio_toggle());
        video_btn.setOnClickListener(v -> video_toggle());
        switchCam_btn.setOnClickListener(v -> mRtcEngine.switchCamera());
        end_call_btn.setOnClickListener(v -> endCall());

        getAllPermissions();
        initializeRtcEngine();
        setVideoConfig();
        joinChannel();
    }

    private void initializeRtcEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setVideoConfig(){
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_24,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
        ));
        mRtcEngine.setDefaultAudioRoutetoSpeakerphone(true);
    }

    private void joinChannel() {
        mRtcEngine.enableVideo();
        mRtcEngine.enableLocalVideo(false);
        mRtcEngine.enableLocalAudio(false);
        mRtcEngine.muteLocalAudioStream(true);
        mRtcEngine.joinChannelWithUserAccount(token, channelName, local_Id);
    }

    private void chat_view() {
        if(remote_Id == null){
            Toast.makeText(getApplicationContext(), "No chat channel available", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(CallActivity.this, ConversationActivity.class);
            intent.putExtra("name", remote_name);
            intent.putExtra("uid", remote_Id);
            intent.putExtra("prevAct", "call");
            startActivity(intent);
        }
    }

    private void audio_toggle() {
        mute = !mute;
        if(mute){
            mute_btn.setImageResource(R.drawable.mic_off);
            mRtcEngine.enableLocalAudio(false);
        } else {
            mute_btn.setImageResource(R.drawable.mic_on);
            mRtcEngine.enableLocalAudio(true);
        }
        mRtcEngine.muteLocalAudioStream(mute);
    }

    private void video_toggle() {
        cam = !cam;
        if(cam){
            setupLocalVideo();
            video_btn.setImageResource(R.drawable.video_on);
        }else{
            removeLocalVideo();
            video_btn.setImageResource(R.drawable.video_off);
        }
        mRtcEngine.muteLocalVideoStream(!cam);
    }

    private void remoteUserJoined() {
        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        ViewGroup.LayoutParams params = localContainer.getLayoutParams();
        params.height = (int) (150 * scale + 0.5f);
        params.width = (int) (150 * scale + 0.5f);
        localContainer.setLayoutParams(params);
        localContainer.requestLayout();
        params = localUser.getLayoutParams();
        params.height = (int) (50 * scale + 0.5f);
        params.width = (int) (50 * scale + 0.5f);
        localUser.setLayoutParams(params);
        localUser.requestLayout();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(AppConstants.KEY_COLLECTION).document(remote_Id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    remote_name = documentSnapshot.getString(AppConstants.NAME);
                    remoteName.setText(remote_name);
                })
                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
    }

    private void remoteUserLeft(){
        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        ViewGroup.LayoutParams params = localContainer.getLayoutParams();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        localContainer.setLayoutParams(params);
        localContainer.requestLayout();
        params = localUser.getLayoutParams();
        params.height = (int) (70 * scale + 0.5f);
        params.width = (int) (70 * scale + 0.5f);
        localUser.setLayoutParams(params);
        localUser.requestLayout();
    }

    private void setupLocalVideo() {
        mRtcEngine.enableLocalVideo(true);
        localView = RtcEngine.CreateRendererView(getBaseContext());
        localView.setZOrderMediaOverlay(true);
        localContainer.addView(localView);

        VideoCanvas canvas = new VideoCanvas(localView, VideoCanvas.RENDER_MODE_HIDDEN, 0);
        mRtcEngine.setupLocalVideo(canvas);
    }

    private void removeLocalVideo() {
        mRtcEngine.enableLocalVideo(false);
        if(localView!=null){
            localContainer.removeView(localView);
        }
        localView = null;
    }

    private void setupRemoteVideo(int uid) {
        int count = remoteContainer.getChildCount();
        View view = null;
        for(int i=0; i<count; i++){
            View v = remoteContainer.getChildAt(i);
            if(v.getTag() instanceof Integer && ((int) v.getTag())==uid){
                view = v;
            }
        }
        if(view != null){
            return;
        }

        remoteView = RtcEngine.CreateRendererView(getBaseContext());
        remoteContainer.addView(remoteView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(remoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        remoteView.setTag(uid);
        mRtcEngine.setRemoteSubscribeFallbackOption(Constants.STREAM_FALLBACK_OPTION_AUDIO_ONLY);
    }

    private void removeRemoteVideo() {
        if(remoteView!=null){
            remoteContainer.removeView(remoteView);
        }
        remoteView = null;
    }

    private void endCall() {
        end_call = !end_call;
        removeLocalVideo();
        removeRemoteVideo();
        leaveChannel();
        finish();
    }

    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    protected void onDestroy(){
        super.onDestroy();
        if(!end_call){
            leaveChannel();
        }
        RtcEngine.destroy();
        finish();
    }

}
