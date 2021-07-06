package com.example.teamsprototype.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.Call;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamsprototype.R;
import com.example.teamsprototype.adapters.MessageAdapter;
import com.example.teamsprototype.model.Message;
import com.example.teamsprototype.services.ChannelNameGenerator;
import com.example.teamsprototype.services.Tokens;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ConversationActivity extends AppCompatActivity {

    MessageAdapter adapter;
    ArrayList<Message> messages;
    ImageView send;
    EditText msg_box;
    FirebaseDatabase database;
    FirebaseStorage storage;
    RecyclerView chat_view;
    String senderRoom, receiverRoom;
    ImageView attachment, start_call, back;
    String senderUid, receiverUid;
    ProgressDialog dialog;
    TextView textView, p_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Image");
        dialog.setCancelable(false);

        send = findViewById(R.id.send_btn);
        msg_box = findViewById(R.id.message_box);
        chat_view = findViewById(R.id.chat_view);
        attachment = findViewById(R.id.camera);

        String name = getIntent().getStringExtra("name");
        receiverUid = getIntent().getStringExtra("uid");

        textView = findViewById(R.id.chat_name);
        textView.setText(name);
        p_name = findViewById(R.id.p_name);
        p_name.setText(name.substring(0,1));

        messages = new ArrayList<>();
        adapter = new MessageAdapter(this, messages);
        chat_view.setLayoutManager(new LinearLayoutManager(this));
        chat_view.setAdapter(adapter);
        senderUid = FirebaseAuth.getInstance().getUid();

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        database = FirebaseDatabase.getInstance();
        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        messages.clear();
                        for(DataSnapshot snapshot1 : snapshot.getChildren()){
                            Message message = snapshot1.getValue(Message.class);
                            messages.add(message);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

        send.setOnClickListener(v -> {
            String msgTxt = msg_box.getText().toString();
            Date date = new Date();
            Message message = new Message(msgTxt, senderUid, date.getTime());
            msg_box.setText("");

            HashMap<String, Object> lastMsg = new HashMap<>();
            lastMsg.put("lastMsg", message.getMessage());
            lastMsg.put("lastMsgTime", date.getTime());
            lastMsg.put("userId", senderUid);

            database.getReference().child("chats").child(senderRoom).updateChildren(lastMsg);
            database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsg);

            database.getReference().child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .push()
                    .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    database.getReference().child("chats")
                            .child(receiverRoom)
                            .child("messages")
                            .push()
                            .setValue(message);
                }
            });
        });

        back = findViewById(R.id.back);
        back.setOnClickListener(v -> {
            finish();
        });

        storage = FirebaseStorage.getInstance();
        attachment.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            launcher.launch(intent);
        });

        start_call =  findViewById(R.id.start_call);
        start_call.setOnClickListener(v -> {
            String channelName = ChannelNameGenerator.randomString();
            String token = Tokens.createToken(channelName, 0);
            String uid = senderUid;
            
            if(token != null){
                Intent intent = new Intent(getApplicationContext(), CallActivity.class);
                intent.putExtra("channelName", channelName);
                intent.putExtra("token", token);
                intent.putExtra("uid", uid);
            } else {
                Toast.makeText(getBaseContext(), "Error in creating room. Retry...", Toast.LENGTH_SHORT).show();
            }
        });

    }

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if(data != null){
                            if(data.getData() != null){
                                Uri image = data.getData();
                                Calendar calendar = Calendar.getInstance();
                                StorageReference reference = storage.getReference().child("chats")
                                        .child(calendar.getTimeInMillis() + "");
                                dialog.show();
                                reference.putFile(image).addOnCompleteListener(task -> {
                                    dialog.dismiss();
                                    if(task.isSuccessful()){
                                        reference.getDownloadUrl().addOnSuccessListener(uri -> {
                                            String filePath = uri.toString();
                                            String msgTxt = msg_box.getText().toString();
                                            Date date = new Date();
                                            Message message = new Message(msgTxt, senderUid, date.getTime());
                                            message.setImageUrl(filePath);
                                            message.setMessage("Photo");

                                            HashMap<String, Object> lastMsg = new HashMap<>();
                                            lastMsg.put("lastMsg", message.getMessage());
                                            lastMsg.put("lastMsgTime", date.getTime());
                                            lastMsg.put("userId", senderUid);

                                            database.getReference().child("chats").child(senderRoom).updateChildren(lastMsg);
                                            database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsg);

                                            database.getReference().child("chats")
                                                    .child(senderRoom)
                                                    .child("messages")
                                                    .push()
                                                    .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    database.getReference().child("chats")
                                                            .child(receiverRoom)
                                                            .child("messages")
                                                            .push()
                                                            .setValue(message);
                                                }
                                            });
                                        });
                                    }
                                });
                            }
                        }
                    }
                }
            });
}