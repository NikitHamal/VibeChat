package com.vibez.chat;

public class Message {
    public static final int TYPE_SENT = 0;
    public static final int TYPE_RECEIVED = 1;
    public static final int TYPE_SYSTEM = 2;

    private String text;
    private int type;

    public Message(String text, int type) {
        this.text = text;
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public int getType() {
        return type;
    }
}