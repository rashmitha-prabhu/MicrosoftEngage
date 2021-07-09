package com.example.teamsprototype.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teamsprototype.R;
import com.example.teamsprototype.utilities.AppConstants;
import com.example.teamsprototype.utilities.Preferences;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class JoinActivity extends AppCompatActivity {
    EditText code;
    Button join;
    static String token, room;
    CollectionReference db;
    Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        db = FirebaseFirestore.getInstance().collection(AppConstants.TOKENS);
        preferences = new Preferences(getApplicationContext());

        code = findViewById(R.id.joinCode);
        join = findViewById(R.id.joinBtn);

//        Joins the channel for the video call
        join.setOnClickListener(v -> {
            room = code.getText().toString();
            if (room.trim().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Enter room code", Toast.LENGTH_SHORT).show();
            } else {
                join.setVisibility(View.GONE);
                String uid = preferences.getString(AppConstants.USER_ID);

                db.document(room).get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot doc = task.getResult();
                                assert doc != null;
                                if (doc.exists()) {
                                    token = doc.getString("token");
                                    Intent intent = new Intent(JoinActivity.this.getApplicationContext(), CallActivity.class);
                                    intent.putExtra("channelName", room);
                                    intent.putExtra("token", token);
                                    intent.putExtra("uid", uid);
                                    intent.putExtra("name", preferences.getString(AppConstants.NAME));
                                    startActivity(intent);
                                    finish();
                                } else {
                                    token = null;
                                    join.setVisibility(View.VISIBLE);
                                    Toast.makeText(JoinActivity.this.getApplicationContext(), "Room doesn't exist...", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                join.setVisibility(View.VISIBLE);
                                Toast.makeText(JoinActivity.this.getApplicationContext(), "Retry...", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}