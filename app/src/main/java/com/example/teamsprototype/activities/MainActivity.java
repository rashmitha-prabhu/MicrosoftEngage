package com.example.teamsprototype.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.teamsprototype.R;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    ImageView teams, ms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }
}