package com.example.teamsprototype.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamsprototype.R;
import com.example.teamsprototype.adapters.ScheduleAdapter;
import com.example.teamsprototype.model.MeetingModel;
import com.example.teamsprototype.utilities.AppConstants;
import com.example.teamsprototype.utilities.DatabaseHandler;
import com.example.teamsprototype.utilities.Preferences;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ScheduleActivity extends AppCompatActivity {

    String agenda, date, time, code;
    private RecyclerView tasks;
    private ScheduleAdapter scheduleAdapter;
    private List<MeetingModel> meetingList;
    private DatabaseHandler db;
    BottomNavigationView bottomNav;
    Preferences preferences;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        preferences = new Preferences(getApplicationContext());

        db = new DatabaseHandler(this);
        db.openDatabase();

        Intent intent = getIntent();
        agenda = intent.getStringExtra("agenda");
        date = intent.getStringExtra("date");
        time = intent.getStringExtra("time");
        code = intent.getStringExtra("code");

//        Insert new task
        if(code!=null){
            MeetingModel meeting = new MeetingModel();
            meeting.setAgenda(agenda);
            meeting.setDate(date);
            meeting.setTime(time);
            meeting.setCode(code);
            db.insertTask(meeting);
        }

//        Sets the view for tasks
        tasks = findViewById(R.id.list);
        tasks.setLayoutManager(new LinearLayoutManager(this));
        meetingList = db.getTask();

        scheduleAdapter = new ScheduleAdapter(db, ScheduleActivity.this);
        tasks.setAdapter(scheduleAdapter);
        scheduleAdapter.setMeetings(meetingList);

//        Bottom Navigation Bar
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.schedule);
        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.dashboard:
                    startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                    finish();
                    overridePendingTransition(0,0);
                    return true;

                case R.id.chatActivity:
                    startActivity(new Intent(getApplicationContext(), ChatActivity.class));
                    overridePendingTransition(0,0);
                    finish();
                    return true;

                case R.id.logout:
                    FirebaseAuth.getInstance().signOut();
                    FirebaseFirestore.getInstance().collection(AppConstants.KEY_COLLECTION)
                            .document(preferences.getString(AppConstants.USER_ID))
                            .update(AppConstants.FCM_TOKEN, null);
                    preferences.clearPreferences();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                    overridePendingTransition(0,0);
                    return true;

                case R.id.schedule:
                    break;
            }
            return false;
        });
    }
}