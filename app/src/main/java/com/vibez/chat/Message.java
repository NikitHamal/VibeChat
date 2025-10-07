package com.vibez.chat;

import java.util.Map;
import java.util.HashMap;

public class Message {

    private String messageId;
    private String text;
    private String senderId;
    private long timestamp;
    private String type = "text"; // "text" or "system"
    private String replyToMessageId;
    private Map<String, String> reactions = new HashMap<>();

    public Message() {
        // Default constructor required for calls to DataSnapshot.getValue(Message.class)
    }

    public Message(String messageId, String text, String senderId, long timestamp, String type) {
        this.messageId = messageId;
        this.text = text;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.type = type;
    }

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReplyToMessageId() {
        return replyToMessageId;
    }

    public void setReplyToMessageId(String replyToMessageId) {
        this.replyToMessageId = replyToMessageId;
    }

    public Map<String, String> getReactions() {
        return reactions;
    }

    public void setReactions(Map<String, String> reactions) {
        this.reactions = reactions;
    }
}