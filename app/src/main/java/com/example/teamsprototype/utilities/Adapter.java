package com.example.teamsprototype.utilities;

import android.content.Context;
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
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private List<MeetingModel> meetingList;
    private final CalendarActivity activity;
    private final DatabaseHandler db;
    private String token;

    public Adapter(DatabaseHandler db, CalendarActivity activity){
        this.db = db;
        this.activity = activity;
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
                int position = getAbsoluteAdapterPosition();
                deleteItem(position);
            });

            meet.setOnClickListener(v13 -> {
                int position = getAbsoluteAdapterPosition();
                MeetingModel item = meetingList.get(position);
                String code = item.getCode();
                boolean done = Tokens.createToken(code, 0);
                if(done) {
                    FirebaseFirestore fdb = FirebaseFirestore.getInstance();
                    fdb.collection(AppConstants.TOKENS).document(code).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                Toast.makeText(v13.getContext(), "Getting the meeting ready...", Toast.LENGTH_SHORT).show();
                                token = documentSnapshot.getString("token");
                                Intent intent = new Intent(v13.getContext(), CallActivity.class);
                                intent.putExtra("channelName", code);
                                intent.putExtra("token", token);
                                v13.getContext().startActivity(intent);
                            })
                            .addOnFailureListener(e -> {
                                token = null;
                                Toast.makeText(v13.getContext(), "Retry...", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(v13.getContext(), "Error in creating room. Retry", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
