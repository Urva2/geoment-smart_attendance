package com.example.attendance;

import com.google.firebase.Timestamp;

public class MessageModel {
    private String message, senderID, senderName;
    private Timestamp timestamp;

    public MessageModel() {} // Empty constructor for Firebase

    public MessageModel(String message, String senderID, String senderName, Timestamp timestamp) {
        this.message = message;
        this.senderID = senderID;
        this.senderName = senderName;
        this.timestamp = timestamp;
    }

    public String getMessage() { return message; }
    public String getSenderID() { return senderID; }
    public String getSenderName() { return senderName; }
    public Timestamp getTimestamp() { return timestamp;}
}
