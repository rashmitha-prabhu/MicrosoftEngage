package com.example.teamsprototype.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teamsprototype.R;
import com.example.teamsprototype.utilities.AppConstants;
import com.example.teamsprototype.utilities.Preferences;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener {

    Button join, host;
    FloatingActionButton signOut;
    TextView greet;
    Preferences preferences;
    String msg;
    ProgressBar progressBar;
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
        progressBar = findViewById(R.id.signOut);

        msg = "Hello, " + preferences.getString(AppConstants.NAME);
        greet.setText(msg);

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
                signOut.setVisibility(View.VISIBLE);
                signOut();
                break;
        }
    }

    private void signOut(){
            preferences.clearPreferences();
            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
            finish();
    }
}
