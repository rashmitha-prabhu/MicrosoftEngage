package com.example.teamsprototype.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teamsprototype.R;
import com.example.teamsprototype.utilities.AppConstants;
import com.example.teamsprototype.utilities.Preferences;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class DashboardActivity extends AppCompatActivity{

    Button join, host;
    TextView greet;
    Preferences preferences;
    String msg;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        preferences = new Preferences(getApplicationContext());

        join = findViewById(R.id.joinMeet);
        host = findViewById(R.id.hostMeet);
        greet = findViewById(R.id.greet);

        msg = "Hello, " + preferences.getString(AppConstants.NAME);
        greet.setText(msg);

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult()!=null){
                sendFCMToken(task.getResult());
            }
        });

        join.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, JoinActivity.class)));
        host.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, HostActivity.class)));

//        Bottom navigation Bar
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.dashboard);
        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.dashboard:
                    break;

                case R.id.chatActivity:
                    startActivity(new Intent(getApplicationContext(), ChatActivity.class));
                    overridePendingTransition(0,0);
                    finish();
                    return true;

                case R.id.logout:
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    FirebaseAuth.getInstance().signOut();
                    FirebaseFirestore.getInstance().collection(AppConstants.KEY_COLLECTION)
                            .document(preferences.getString(AppConstants.USER_ID))
                            .update(AppConstants.FCM_TOKEN, null);
                    preferences.clearPreferences();
                    finish();
                    overridePendingTransition(0,0);
                    return true;

                case R.id.schedule:
                    startActivity(new Intent(getApplicationContext(), ScheduleActivity.class));
                    overridePendingTransition(0,0);
                    finish();
                    return true;
            }
            return false;
        });
    }

//    Update Firebase with the FCM token
    private void sendFCMToken(String token){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        preferences.putString(AppConstants.FCM_TOKEN, token);
        db.collection(AppConstants.KEY_COLLECTION).document(preferences.getString(AppConstants.USER_ID))
                .update(AppConstants.FCM_TOKEN, token);
    }
}
