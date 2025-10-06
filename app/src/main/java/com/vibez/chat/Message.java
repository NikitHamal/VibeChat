package com.vibez.chat;

public class Message {
    private String text;
    private boolean isSentByUser;
    private boolean isSystemMessage;

    public Message(String text, boolean isSentByUser, boolean isSystemMessage) {
        this.text = text;
        this.isSentByUser = isSentByUser;
        this.isSystemMessage = isSystemMessage;
    }

    public String getText() {
        return text;
    }

    public boolean isSentByUser() {
        return isSentByUser;
    }

    public boolean isSystemMessage() {
        return isSystemMessage;
    }
}