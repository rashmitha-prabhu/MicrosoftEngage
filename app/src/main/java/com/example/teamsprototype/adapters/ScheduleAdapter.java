package com.example.teamsprototype.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamsprototype.R;
import com.example.teamsprototype.activities.CalendarActivity;
import com.example.teamsprototype.activities.CallActivity;
import com.example.teamsprototype.model.MeetingModel;
import com.example.teamsprototype.services.Tokens;
import com.example.teamsprototype.utilities.AppConstants;
import com.example.teamsprototype.utilities.DatabaseHandler;
import com.example.teamsprototype.utilities.Preferences;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private List<MeetingModel> meetingList;
    private final CalendarActivity activity;
    private final DatabaseHandler db;
    private String token;
    Preferences preferences;

    public ScheduleAdapter(DatabaseHandler db, CalendarActivity activity){
        this.db = db;
        this.activity = activity;
        this.preferences = new Preferences(activity.getApplicationContext());
    }

    public @NotNull ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.meeting_layout, parent, false);
        return new ViewHolder(itemView);
    }

    public void setMeetings(List<MeetingModel> meetingList){
        this.meetingList = meetingList;
        notifyDataSetChanged();
    }

    public void deleteItem(int position){
        MeetingModel item = meetingList.get(position);
        db.deleteTask(item.getId());
        meetingList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        db.openDatabase();
        MeetingModel item = meetingList.get(position);
        holder.agenda.setText(item.getAgenda());
        holder.date.setText(item.getDate());
        holder.time.setText(item.getTime());
        holder.code.setText(item.getCode());
    }

    @Override
    public int getItemCount() {
        return meetingList.size();
    }

    public Context getContext() {
        return activity;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView agenda, date, time, code;
        ImageView share, meet, delete;

        ViewHolder(View v){
            super(v);
            agenda = v.findViewById(R.id.meet_agenda);
            date = v.findViewById(R.id.meet_date);
            time = v.findViewById(R.id.meet_time);
            code = v.findViewById(R.id.meet_code);

            share = v.findViewById(R.id.share_meeting);
            delete = v.findViewById(R.id.delete_meeting);
            meet = v.findViewById(R.id.start_meeting);

            share.setOnClickListener(v1 -> {
                int position = getAbsoluteAdapterPosition();
                MeetingModel item = meetingList.get(position);
                String msg = "Join the Teams Meeting using code: " + item.getCode()
                        + "\nAgenda: " +  item.getAgenda()
                        + "\nDate: " + item.getDate()
                        + "\nTime: " + item.getTime();
                Intent send = new Intent();
                send.setAction(Intent.ACTION_SEND);
                send.putExtra(Intent.EXTRA_TEXT, msg);
                send.setType("text/plain");
                Intent shareIntent = Intent.createChooser(send, "Teams Meeting");
                v1.getContext().startActivity(shareIntent);
            });

            delete.setOnClickListener(v12 -> {
                AlertDialog alert = new AlertDialog.Builder(getContext())
                        .setTitle("Delete Meeting")
                        .setMessage("Are you sure you want to delete?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int position = getAbsoluteAdapterPosition();
                                deleteItem(position);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .setIcon(R.drawable.alert)
                        .show();
            });

            meet.setOnClickListener(v13 -> {
                int position = getAbsoluteAdapterPosition();
                MeetingModel item = meetingList.get(position);
                String code = item.getCode();
                String uid = preferences.getString(AppConstants.USER_ID);
                String token = Tokens.createToken(code, 0);

                if(token!=null) {
                    Intent intent = new Intent(v13.getContext(), CallActivity.class);
                    intent.putExtra("channelName", code);
                    intent.putExtra("token", token);
                    intent.putExtra("uid", uid);
                    v13.getContext().startActivity(intent);
                } else {
                    Toast.makeText(v13.getContext(), "Error in creating room. Retry", Toast.LENGTH_SHORT).show();
                }

//                if(done) {
//                    FirebaseFirestore fdb = FirebaseFirestore.getInstance();
//                    fdb.collection(AppConstants.TOKENS).document(code).get()
//                            .addOnSuccessListener(documentSnapshot -> {
//                                Toast.makeText(v13.getContext(), "Getting the meeting ready...", Toast.LENGTH_SHORT).show();
//                                token = documentSnapshot.getString("token");
//                                Intent intent = new Intent(v13.getContext(), CallActivity.class);
//                                intent.putExtra("channelName", code);
//                                intent.putExtra("token", token);
//                                intent.putExtra("uid", uid);
//                                v13.getContext().startActivity(intent);
//                            })
//                            .addOnFailureListener(e -> {
//                                token = null;
//                                Toast.makeText(v13.getContext(), "Retry...", Toast.LENGTH_SHORT).show();
//                            });
//                } else {
//                    Toast.makeText(v13.getContext(), "Error in creating room. Retry", Toast.LENGTH_SHORT).show();
//                }
            });
        }
    }
}
