package com.example.teamsprototype.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teamsprototype.R;
import com.example.teamsprototype.services.Tokens;
import com.example.teamsprototype.utilities.AppConstants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;


public class HostActivity extends AppCompatActivity
        implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    EditText agenda, time, date;
    TextView code;
    FloatingActionButton share;
    Button meet_now;
    Calendar c = Calendar.getInstance();
    String room;
    String token;
    FirebaseFirestore db;

    String randomString(){
        StringBuilder sb = new StringBuilder(10);
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        for(int i=0; i<10; i++){
            int n = (int)(alphabet.length()*Math.random());
            sb.append(alphabet.charAt(n));
        }
        return sb.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        room = randomString();

        agenda = findViewById(R.id.agenda);
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        code = findViewById(R.id.code);
        share = findViewById(R.id.share);
        meet_now = findViewById(R.id.meet_now);

        code.setText(room);
        token = Tokens.createToken(room, 0);
//        if(Tokens.createToken(room, 0)){
//            db = FirebaseFirestore.getInstance();
//            db.collection(AppConstants.TOKENS).document(room).get()
//                    .addOnSuccessListener(documentSnapshot -> {
//                        Toast.makeText(getApplicationContext(), "Firebase retrieval success in HOST", Toast.LENGTH_SHORT).show();
//                    })
//                    .addOnFailureListener(e -> {
//                        Toast.makeText(getApplicationContext(), "Firebase retrieval failed", Toast.LENGTH_SHORT).show();
//                    });
//        }

        date.setOnClickListener(this);
        time.setOnClickListener(this);
        share.setOnClickListener(this);
        meet_now.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.date:
                showDatePicker();
                break;

            case R.id.time:
                showTimePicker();
                break;

            case R.id.share:
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
                break;

            case R.id.meet_now:
                Intent intent = new Intent(getApplicationContext(), CallActivity.class);
                intent.putExtra("channelName", room);
                intent.putExtra("token", token);
                startActivity(intent);
                finish();
        }
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
        String meetTime = hourOfDay+":"+minute+":00";
        time.setText(meetTime);
    }
}