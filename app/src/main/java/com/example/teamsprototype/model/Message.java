package com.example.teamsprototype.model;

public class Message {
    private String message;
    private String sender_id;
    private String imageUrl;
    private long time;

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
}
