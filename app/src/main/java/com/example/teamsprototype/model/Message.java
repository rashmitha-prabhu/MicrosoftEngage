package com.example.teamsprototype.model;

import com.google.firebase.database.DataSnapshot;

public class Message {
    private String message, sender_id, imageUrl, messageId;
    private long time;

    public Message() {
    }

    public Message(String message, String sender_id, long time) {
        this.message = message;
        this.sender_id = sender_id;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender_id() {
        return sender_id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
