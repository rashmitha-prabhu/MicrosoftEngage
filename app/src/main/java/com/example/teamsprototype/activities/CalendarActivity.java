package com.example.teamsprototype.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamsprototype.R;
import com.example.teamsprototype.model.MeetingModel;
import com.example.teamsprototype.adapters.ScheduleAdapter;
import com.example.teamsprototype.utilities.DatabaseHandler;
import com.example.teamsprototype.utilities.Preferences;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    String agenda, date, time, code;
    private RecyclerView tasks;
    private ScheduleAdapter scheduleAdapter;
    private List<MeetingModel> meetingList;
    private DatabaseHandler db;
    BottomNavigationView bottomNav;
    Preferences preferences;

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

        if(code!=null){
            MeetingModel meeting = new MeetingModel();
            meeting.setAgenda(agenda);
            meeting.setDate(date);
            meeting.setTime(time);
            meeting.setCode(code);
            db.insertTask(meeting);
        }

        tasks = findViewById(R.id.list);
        tasks.setLayoutManager(new LinearLayoutManager(this));
        meetingList = db.getTask();

        scheduleAdapter = new ScheduleAdapter(db,CalendarActivity.this);
        tasks.setAdapter(scheduleAdapter);
        scheduleAdapter.setMeetings(meetingList);

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
                    return true;

                case R.id.logout:
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