package com.vibez.chat;

public class Participant {

    private String status; // "active", "left"
    private long lastActive;

    public Participant() {
        // Default constructor required for calls to DataSnapshot.getValue(Participant.class)
    }

    public Participant(String status, long lastActive) {
        this.status = status;
        this.lastActive = lastActive;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getLastActive() {
        return lastActive;
    }

    public void setLastActive(long lastActive) {
        this.lastActive = lastActive;
    }
}