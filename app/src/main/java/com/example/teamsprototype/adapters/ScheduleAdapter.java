package com.example.teamsprototype.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamsprototype.R;
import com.example.teamsprototype.activities.CallActivity;
import com.example.teamsprototype.activities.HostActivity;
import com.example.teamsprototype.activities.ScheduleActivity;
import com.example.teamsprototype.model.MeetingModel;
import com.example.teamsprototype.services.Tokens;
import com.example.teamsprototype.utilities.AppConstants;
import com.example.teamsprototype.utilities.DatabaseHandler;
import com.example.teamsprototype.utilities.Preferences;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

import static android.content.ContentValues.TAG;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {
//    Adapter for scheduled meetings

    private List<MeetingModel> meetingList;
    private final ScheduleActivity activity;
    private final DatabaseHandler db;
    Preferences preferences;

    public ScheduleAdapter(DatabaseHandler db, ScheduleActivity activity){
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
                        .setPositiveButton("Confirm", (dialog, which) -> {
                            int position = getAbsoluteAdapterPosition();
                            deleteItem(position);
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
                String name = preferences.getString(AppConstants.NAME);

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                HashMap<String, Object> token_instance = new HashMap<>();
                token_instance.put("token", token);

                db.collection(AppConstants.TOKENS)
                        .document(code)
                        .set(token_instance)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                        .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));

                if(token!=null) {
                    Intent intent = new Intent(v13.getContext(), CallActivity.class);
                    intent.putExtra("channelName", code);
                    intent.putExtra("token", token);
                    intent.putExtra("uid", uid);
                    intent.putExtra("name", name);
                    v13.getContext().startActivity(intent);
                } else {
                    AlertDialog alert = new AlertDialog.Builder(getContext())
                            .setTitle("Unable to create meeting")
                            .setMessage("Make sure you are connected to the internet and retry")
                            .setPositiveButton("Dismiss", null)
                            .setIcon(R.drawable.alert)
                            .show();
                }
            });
        }
    }
}
