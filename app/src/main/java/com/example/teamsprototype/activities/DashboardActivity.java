package com.example.teamsprototype.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teamsprototype.R;
import com.example.teamsprototype.utilities.AppConstants;
import com.example.teamsprototype.utilities.Preferences;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener {

    Button join, host;
    FloatingActionButton signOut;
    TextView greet;
    Preferences preferences;
    String msg;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

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
        signOut = findViewById(R.id.logout);

        msg = "Hello, " + preferences.getString(AppConstants.NAME);
        greet.setText(msg);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        if(token!=null)
                            sendFCMToken(token);
                    }
                });

        join.setOnClickListener(this);
        host.setOnClickListener(this);
        signOut.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.joinMeet:
                startActivity(new Intent(DashboardActivity.this, JoinActivity.class));
                break;
            case R.id.hostMeet:
                startActivity(new Intent(DashboardActivity.this, HostActivity.class));
                break;
            case R.id.logout:
                signOut();
                break;
        }
    }

    private void sendFCMToken(String token){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                db.collection(AppConstants.KEY_COLLECTION).document(preferences.getString(AppConstants.USER_ID));
        documentReference.update(AppConstants.FCM_TOKEN, token)
                .addOnFailureListener(e -> Toast.makeText(DashboardActivity.this, "Unable to send token: "+e.getLocalizedMessage(),  Toast.LENGTH_SHORT).show());
    }

    private void signOut(){
        Toast.makeText(DashboardActivity.this, "Signing Out...",  Toast.LENGTH_SHORT).show();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection(AppConstants.KEY_COLLECTION).document(preferences.getString(AppConstants.USER_ID));
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(AppConstants.FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates).addOnSuccessListener(unused -> {
            preferences.clearPreferences();
            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
            finish();
        }).addOnFailureListener(e -> Toast.makeText(DashboardActivity.this, "Unable to SignOut",  Toast.LENGTH_SHORT).show());
    }
}
