package com.example.teamsprototype.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.teamsprototype.R;
import com.example.teamsprototype.services.Tokens;
import com.example.teamsprototype.utilities.AppConstants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class CallActivity extends AppCompatActivity implements View.OnClickListener{

    private RtcEngine mRtcEngine;
    FrameLayout localContainer;
    RelativeLayout remoteContainer;
    SurfaceView localView, remoteView;
    FloatingActionButton mute_btn, video_btn, end_call_btn, switchCam_btn, speaker_btn;
    String channelName, token;

    boolean mute = false;
    boolean cam = false;
    boolean end_call = false;
    boolean speaker = true;

    private final IRtcEngineEventHandler mRtcHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
        }

        @Override
        public void onUserOffline(final int uid, int reason) {
            super.onUserOffline(uid, reason);
            runOnUiThread(() -> {
                removeRemoteVideo();
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

        end_call_btn = findViewById(R.id.hangUp);
        switchCam_btn = findViewById(R.id.switchCam);
        mute_btn = findViewById(R.id.mic);
        video_btn = findViewById(R.id.video);
        speaker_btn = findViewById(R.id.speaker);
        localContainer = findViewById(R.id.localVideo);
        remoteContainer = findViewById(R.id.remoteVideo);

        end_call_btn.setOnClickListener(this);
        switchCam_btn.setOnClickListener(this);
        mute_btn.setOnClickListener(this);
        video_btn.setOnClickListener(this);
        speaker_btn.setOnClickListener(this);

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

    private synchronized String getToken(String channelName){
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicReference<String> token_from_firebase = new AtomicReference<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(AppConstants.TOKENS).document(channelName).get()
                .addOnSuccessListener(documentSnapshot -> {
                    token_from_firebase.set(documentSnapshot.getString("token"));
                    })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Firebase Error", Toast.LENGTH_SHORT).show();
                });
        countDownLatch.countDown();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return token_from_firebase.get();
    }

    private void joinChannel() {
        Toast.makeText(getApplicationContext(), "Token from Call:" + channelName +" = "+token, Toast.LENGTH_LONG).show();
        mRtcEngine.enableVideo();
        mRtcEngine.enableLocalVideo(false);
        mRtcEngine.enableLocalAudio(false);
        mRtcEngine.muteLocalAudioStream(true);
        mRtcEngine.joinChannel(token, channelName, "", 0);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.speaker:
                speaker = !speaker;
                if(speaker){
                    speaker_btn.setImageResource(R.drawable.speaker_on);
                } else {
                    speaker_btn.setImageResource(R.drawable.speaker_off);
                }
                mRtcEngine.setEnableSpeakerphone(speaker);
                break;

            case R.id.mic:
                mute = !mute;
                if(mute){
                    mute_btn.setImageResource(R.drawable.mic_off);
                    mRtcEngine.enableLocalAudio(false);
                } else {
                    mute_btn.setImageResource(R.drawable.mic_on);
                    mRtcEngine.enableLocalAudio(true);
                }
                mRtcEngine.muteLocalAudioStream(mute);
                break;

            case R.id.video:
                cam = !cam;
                if(cam){
                    setupLocalVideo();
                    video_btn.setImageResource(R.drawable.video_on);
                }else{
                    removeLocalVideo();
                    video_btn.setImageResource(R.drawable.video_off);
                }
                mRtcEngine.muteLocalVideoStream(!cam);
                break;

            case R.id.switchCam:
                mRtcEngine.switchCamera();
                break;

            case R.id.hangUp:
                end_call = !end_call;
                endCall();
                break;
        }
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
