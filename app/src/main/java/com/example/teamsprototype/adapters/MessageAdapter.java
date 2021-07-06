package com.example.teamsprototype.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.teamsprototype.R;
import com.example.teamsprototype.databinding.ItemReceiveBinding;
import com.example.teamsprototype.databinding.ItemSendBinding;
import com.example.teamsprototype.model.Message;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<Message> messages;

    final int ITEM_SENT = 1;
    final int ITEM_RECEIVED = 2;

    public MessageAdapter(Context context, ArrayList<Message> messages){
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SENT){
            View view = LayoutInflater.from(context).inflate(R.layout.item_send, parent, false);
            return new SendViewHolder(view);
        }
        View view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false);
        return new ReceiveViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if(FirebaseAuth.getInstance().getUid().equals(message.getSender_id())){
            return ITEM_SENT;
        }
        return ITEM_RECEIVED;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if(holder.getClass() == SendViewHolder.class){
            SendViewHolder viewHolder = (SendViewHolder) holder;
            if(message.getMessage().equals("Photo")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.localMsg.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl())
                        .placeholder(R.drawable.image)
                        .into(viewHolder.binding.image);
            }
            viewHolder.binding.localMsg.setText(message.getMessage());

        } else {
            ReceiveViewHolder viewHolder = (ReceiveViewHolder) holder;
            if(message.getMessage().equals("Photo")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.remoteMsg.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl())
                        .placeholder(R.drawable.image)
                        .into(viewHolder.binding.image);
            }
            viewHolder.binding.remoteMsg.setText(message.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SendViewHolder extends RecyclerView.ViewHolder {
        ItemSendBinding binding;

        public SendViewHolder(@NonNull View itemView) {
            super(itemView);
            this.binding = ItemSendBinding.bind(itemView);
        }
    }

    public class ReceiveViewHolder extends RecyclerView.ViewHolder {
        ItemReceiveBinding binding;

        public ReceiveViewHolder(@NonNull View itemView) {
            super(itemView);
            this.binding = ItemReceiveBinding.bind(itemView);
        }
    }

}
