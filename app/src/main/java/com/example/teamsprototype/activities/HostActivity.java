package com.example.teamsprototype.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teamsprototype.R;
import com.example.teamsprototype.services.ChannelNameGenerator;
import com.example.teamsprototype.services.Tokens;
import com.example.teamsprototype.utilities.AppConstants;
import com.example.teamsprototype.utilities.Preferences;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static android.content.ContentValues.TAG;


public class HostActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    EditText agenda, time, date;
    TextView code;
    FloatingActionButton share;
    Button meet_now, schedule_meet;
    Calendar c = Calendar.getInstance();
    String room, token;
    Preferences preferences;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        room = ChannelNameGenerator.randomString();

        preferences = new Preferences(getApplicationContext());
        ProgressDialog dialog = new ProgressDialog(HostActivity.this);
        dialog.setMessage("Getting the meeting ready...");

        agenda = findViewById(R.id.agenda);
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        code = findViewById(R.id.code);
        share = findViewById(R.id.share);
        meet_now = findViewById(R.id.meet_now);
        schedule_meet = findViewById(R.id.meet_later);

        code.setText(room);

        date.setInputType(InputType.TYPE_NULL);
        time.setInputType(InputType.TYPE_NULL);

        date.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN)
                    showDatePicker();
            return false;
        });

        time.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN)
                showTimePicker();
            return false;
        });

//        Share meeting details
        share.setOnClickListener(v -> {
            String msg = "Join the Teams Meeting using code: " + code.getText().toString()
                    + "\nAgenda: " +  agenda.getText().toString()
                    + "\nDate: " + date.getText().toString()
                    + "\nTime: " + time.getText().toString();
            Intent send = new Intent();
            send.setAction(Intent.ACTION_SEND);
            send.putExtra(Intent.EXTRA_TEXT, msg);
            send.setType("text/plain");
            Intent shareIntent = Intent.createChooser(send, "Teams Meeting");
            startActivity(shareIntent);
        });

//        Starts the meeting
        meet_now.setOnClickListener(v -> {
            dialog.show();
            token = Tokens.createToken(room, 0);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            HashMap<String, Object> token_instance = new HashMap<>();
            token_instance.put("token", token);

            db.collection(AppConstants.TOKENS)
                    .document(room)
                    .set(token_instance)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));

            if(token != null){
                dialog.dismiss();
                Intent intent = new Intent(getApplicationContext(), CallActivity.class);
                intent.putExtra("channelName", room);
                intent.putExtra("token", token);
                intent.putExtra("uid", preferences.getString(AppConstants.USER_ID));
                intent.putExtra("name", preferences.getString(AppConstants.NAME));
                startActivity(intent);
                finish();
            } else {
                dialog.dismiss();
                AlertDialog alert = new AlertDialog.Builder(HostActivity.this)
                        .setTitle("Unable to create meeting")
                        .setMessage("Make sure you are connected to the internet and retry")
                        .setPositiveButton("Dismiss", null)
                        .setIcon(R.drawable.alert)
                        .show();

                meet_now.setVisibility(View.VISIBLE);
            }
        });

//        Adds the meeting details to the Schedule
        schedule_meet.setOnClickListener(v -> {
            if(date.getText().toString().isEmpty()){
                date.setError("Field can't be empty");
            } else {
                Intent intent = new Intent(getApplicationContext(), ScheduleActivity.class);
                intent.putExtra("agenda", agenda.getText().toString());
                intent.putExtra("date", date.getText().toString());
                intent.putExtra("time", time.getText().toString());
                intent.putExtra("code", code.getText().toString());
                startActivity(intent);
                finish();
            }
        });
    }

    private void showTimePicker() {
        int hr = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, this, hr, min, false);
        timePickerDialog.show();
    }

    private void showDatePicker(){
        DatePickerDialog datePickerDialog = new DatePickerDialog( this, this,
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String meetDate = dayOfMonth+"/"+month+"/"+year;
        date.setText(meetDate);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
        String meetTime = format.format(c.getTime());
        time.setText(meetTime);
    }
}