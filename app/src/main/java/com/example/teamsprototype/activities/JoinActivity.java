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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class JoinActivity extends AppCompatActivity implements View.OnClickListener {
    EditText code;
    Button join;
    static String token, room;
    CollectionReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        db = FirebaseFirestore.getInstance().collection(AppConstants.TOKENS);

        code = findViewById(R.id.joinCode);
        join = findViewById(R.id.joinBtn);

        join.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.joinBtn) {
            room = code.getText().toString();
            if (room.trim().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Enter room code", Toast.LENGTH_SHORT).show();
            } else {
                join.setVisibility(View.GONE);
                db.document(room).get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot doc = task.getResult();
                                if (doc.exists()) {
                                    token = doc.getString("token");
                                    Intent intent = new Intent(JoinActivity.this.getApplicationContext(), CallActivity.class);
                                    intent.putExtra("channelName", room);
                                    intent.putExtra("token", token);
                                    JoinActivity.this.startActivity(intent);
                                    finish();
                                } else {
                                    token = null;
                                    join.setVisibility(View.VISIBLE);
                                    Toast.makeText(JoinActivity.this.getApplicationContext(), "Room doesn't exist. Host meeting", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                join.setVisibility(View.VISIBLE);
                                Toast.makeText(JoinActivity.this.getApplicationContext(), "Retry...", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

        }
    }
}