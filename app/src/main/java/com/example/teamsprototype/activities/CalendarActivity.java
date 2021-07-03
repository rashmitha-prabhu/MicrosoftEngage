package com.example.teamsprototype.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamsprototype.R;
import com.example.teamsprototype.model.MeetingModel;
import com.example.teamsprototype.utilities.Adapter;
import com.example.teamsprototype.utilities.DatabaseHandler;

import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    String agenda, date, time, code;
    private RecyclerView tasks;
    private Adapter adapter;
    private List<MeetingModel> meetingList;
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

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

        adapter = new Adapter(db,CalendarActivity.this);
        tasks.setAdapter(adapter);
        adapter.setMeetings(meetingList);
    }
}