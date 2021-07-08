package com.example.teamsprototype.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamsprototype.R;
import com.example.teamsprototype.utilities.AppConstants;
import com.example.teamsprototype.utilities.Preferences;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

public class DashboardActivity extends AppCompatActivity{

    Button join, host;
    TextView greet;
    Preferences preferences;
    String msg;
    boolean doubleBackToExitPressedOnce = false;
    private BottomNavigationView bottomNav;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 1000);
    }

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

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<String> task) {
                if(task.isSuccessful() && task.getResult()!=null){
                    sendFCMToken(task.getResult());
                }
            }
        });

        join.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, JoinActivity.class)));
        host.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, HostActivity.class)));

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.dashboard);
        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.dashboard:
                    break;

                case R.id.chatActivity:
                    startActivity(new Intent(getApplicationContext(), ChatActivity.class));
                    overridePendingTransition(0,0);
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
                    startActivity(new Intent(getApplicationContext(), CalendarActivity.class));
                    overridePendingTransition(0,0);
                    return true;
            }
            return false;
        });
    }

    private void sendFCMToken(String token){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        preferences.putString(AppConstants.FCM_TOKEN, token);
        db.collection(AppConstants.KEY_COLLECTION).document(preferences.getString(AppConstants.USER_ID))
                .update(AppConstants.FCM_TOKEN, token);
    }
}
