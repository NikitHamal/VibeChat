package com.vibez.chat;

public class QueueUser {
    public String status;
    public String chatRoomId;

    public QueueUser() {
        // Default constructor required for calls to DataSnapshot.getValue(QueueUser.class)
    }

    public QueueUser(String status) {
        this.status = status;
        this.chatRoomId = null;
    }
}