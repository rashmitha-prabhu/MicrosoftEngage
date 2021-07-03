package com.example.teamsprototype.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teamsprototype.R;
import com.example.teamsprototype.utilities.AppConstants;
import com.example.teamsprototype.utilities.Preferences;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Timer;
import java.util.TimerTask;

public class DashboardActivity extends AppCompatActivity{

    Button join, host, schedule;
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
        signOut = findViewById(R.id.logout);
        schedule = findViewById(R.id.view_sched);

        msg = "Hello, " + preferences.getString(AppConstants.NAME);
        greet.setText(msg);

        join.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, JoinActivity.class)));
        host.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, HostActivity.class)));
        schedule.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, CalendarActivity.class)));
        signOut.setOnClickListener(v -> {
            preferences.clearPreferences();
            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
            finish();
        });

    }
}
