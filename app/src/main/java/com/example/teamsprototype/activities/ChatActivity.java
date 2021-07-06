package com.example.teamsprototype.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamsprototype.R;
import com.example.teamsprototype.adapters.UsersAdapter;
import com.example.teamsprototype.databinding.ActivityChatBinding;
import com.example.teamsprototype.services.User;
import com.example.teamsprototype.utilities.AppConstants;
import com.example.teamsprototype.utilities.Preferences;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    FirebaseFirestore db;
    ArrayList<User> users;
    UsersAdapter usersAdapter;
    Preferences preferences;
    BottomNavigationView bottomNav;
    RecyclerView rclView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        db = FirebaseFirestore.getInstance();
        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(this, users);
        preferences = new Preferences(this);

        rclView = findViewById(R.id.recyclerView);
        rclView.setAdapter(usersAdapter);

        db.collection(AppConstants.KEY_COLLECTION).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                        String uid = preferences.getString(AppConstants.USER_ID);
                        if(task.isSuccessful() && task.getResult()!= null){
                            for(QueryDocumentSnapshot doc : task.getResult()){
                                if(uid.equals(doc.getId())){
                                    continue;
                                } else {
                                    String name = doc.getString(AppConstants.NAME);
                                    String email = doc.getString(AppConstants.EMAIL);
                                    String user_id = doc.getString(AppConstants.USER_ID);
                                    User user = new User(user_id, name, email);
                                    users.add(user);
                                    usersAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                });

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.chatActivity);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.dashboard:
                    startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                    finish();
                    overridePendingTransition(0,0);
                    return true;

                case R.id.chatActivity:
                    break;

                case R.id.logout:
                    preferences.clearPreferences();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                    overridePendingTransition(0,0);
                    return true;

                case R.id.schedule:
                    startActivity(new Intent(getApplicationContext(), CalendarActivity.class));
                    finish();
                    overridePendingTransition(0,0);
                    return true;
            }
            return false;
        });

    }

}