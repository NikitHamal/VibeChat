package com.vibez.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 0;
    private static final int VIEW_TYPE_RECEIVED = 1;
    private static final int VIEW_TYPE_SYSTEM = 2;

    private final List<Message> messages;
    private final String currentUserId;

    public ChatAdapter(List<Message> messages) {
        this.messages = messages;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.getSenderId() != null) {
            if (message.getSenderId().equals("system")) {
                return VIEW_TYPE_SYSTEM;
            } else if (message.getSenderId().equals(currentUserId)) {
                return VIEW_TYPE_SENT;
            }
        }
        return VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_SENT:
                View sentView = inflater.inflate(R.layout.item_message_sent, parent, false);
                return new SentMessageViewHolder(sentView);
            case VIEW_TYPE_RECEIVED:
                View receivedView = inflater.inflate(R.layout.item_message_received, parent, false);
                return new ReceivedMessageViewHolder(receivedView);
            case VIEW_TYPE_SYSTEM:
                View systemView = inflater.inflate(R.layout.item_message_system, parent, false);
                return new SystemMessageViewHolder(systemView);
            default:
                // This should not happen, but as a fallback, inflate a default view
                View defaultView = inflater.inflate(R.layout.item_message_system, parent, false);
                return new SystemMessageViewHolder(defaultView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_SENT:
                ((SentMessageViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_RECEIVED:
                ((ReceivedMessageViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_SYSTEM:
                ((SystemMessageViewHolder) holder).bind(message);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        SentMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
        }

        void bind(Message message) {
            messageText.setText(message.getText());
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
        }

        void bind(Message message) {
            messageText.setText(message.getText());
        }
    }

    static class SystemMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        SystemMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
        }

        void bind(Message message) {
            messageText.setText(message.getText());
        }
    }
}