package com.example.teamsprototype.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.teamsprototype.R;

public class FullscreenActivity extends AppCompatActivity {
//   Displays images in chat in fullscreen view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        String path = getIntent().getStringExtra("path");
        ImageView image = findViewById(R.id.fullScreenImage);

        Glide.with(getApplicationContext()).load(path)
                .into(image);

        ImageButton back = findViewById(R.id.backPress);
        back.setOnClickListener(v -> finish());
    }
}