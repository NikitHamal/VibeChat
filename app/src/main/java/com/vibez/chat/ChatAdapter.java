package com.vibez.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Message> messages;

    public ChatAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case Message.TYPE_SENT:
                View sentView = inflater.inflate(R.layout.item_message_sent, parent, false);
                return new SentMessageViewHolder(sentView);
            case Message.TYPE_RECEIVED:
                View receivedView = inflater.inflate(R.layout.item_message_received, parent, false);
                return new ReceivedMessageViewHolder(receivedView);
            case Message.TYPE_SYSTEM:
                View systemView = inflater.inflate(R.layout.item_message_system, parent, false);
                return new SystemMessageViewHolder(systemView);
            default:
                throw new IllegalArgumentException("Invalid view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        switch (holder.getItemViewType()) {
            case Message.TYPE_SENT:
                ((SentMessageViewHolder) holder).bind(message);
                break;
            case Message.TYPE_RECEIVED:
                ((ReceivedMessageViewHolder) holder).bind(message);
                break;
            case Message.TYPE_SYSTEM:
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