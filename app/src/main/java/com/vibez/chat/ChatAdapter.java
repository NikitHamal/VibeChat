package com.vibez.chat;

import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 0;
    private static final int VIEW_TYPE_RECEIVED = 1;
    private static final int VIEW_TYPE_SYSTEM = 2;

    private final List<Message> messageList;
    private final java.util.Map<String, Message> messageMap;
    private final String currentUserId;
    private final String strangerName;
    private final OnMessageInteractionListener listener;

    public interface OnMessageInteractionListener {
        void onMessageLongClicked(Message message);
        void onMessageDoubleTapped(Message message);
    }

    public ChatAdapter(List<Message> messageList, java.util.Map<String, Message> messageMap, String strangerName, OnMessageInteractionListener listener) {
        this.messageList = messageList;
        this.messageMap = messageMap;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.strangerName = strangerName;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
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
                return new SentMessageViewHolder(sentView, listener);
            case VIEW_TYPE_RECEIVED:
                View receivedView = inflater.inflate(R.layout.item_message_received, parent, false);
                return new ReceivedMessageViewHolder(receivedView, listener);
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
        Message message = messageList.get(position);
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
        return messageList.size();
    }

    abstract class BaseMessageViewHolder extends RecyclerView.ViewHolder {
        public BaseMessageViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        abstract void bind(Message message);
    }

    class SentMessageViewHolder extends BaseMessageViewHolder {
        TextView messageText, replyQuoteName, replyQuoteText;
        LinearLayout reactionContainer;
        View replyQuoteLayout;
        private final GestureDetector gestureDetector;

        SentMessageViewHolder(View itemView, OnMessageInteractionListener listener) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            reactionContainer = itemView.findViewById(R.id.reaction_container);
            replyQuoteLayout = itemView.findViewById(R.id.reply_quote_layout);
            replyQuoteName = itemView.findViewById(R.id.reply_quote_name);
            replyQuoteText = itemView.findViewById(R.id.reply_quote_text);

            gestureDetector = new GestureDetector(itemView.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(android.view.MotionEvent e) {
                    listener.onMessageDoubleTapped(messageList.get(getAdapterPosition()));
                    return true;
                }
            });

            itemView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

            itemView.setOnLongClickListener(v -> {
                listener.onMessageLongClicked(messageList.get(getAdapterPosition()));
                return true;
            });
        }

        void bind(Message message) {
            messageText.setText(message.getText());
            bindReactions(message);
            bindReply(message);
        }

        private void bindReply(Message message) {
            if (message.getReplyToMessageId() != null) {
                Message originalMessage = messageMap.get(message.getReplyToMessageId());
                if (originalMessage != null) {
                    replyQuoteLayout.setVisibility(View.VISIBLE);
                    boolean isReplyToSelf = originalMessage.getSenderId().equals(currentUserId);
                    replyQuoteName.setText(isReplyToSelf ? "You" : strangerName);
                    replyQuoteText.setText(originalMessage.getText());
                } else {
                    replyQuoteLayout.setVisibility(View.GONE);
                }
            } else {
                replyQuoteLayout.setVisibility(View.GONE);
            }
        }

        private void bindReactions(Message message) {
            reactionContainer.removeAllViews();
            if (message.getReactions() != null && !message.getReactions().isEmpty()) {
                reactionContainer.setVisibility(View.VISIBLE);
                // Using a Map to count unique emojis
                Map<String, Integer> emojiCounts = new HashMap<>();
                for (String emoji : message.getReactions().values()) {
                    emojiCounts.put(emoji, emojiCounts.getOrDefault(emoji, 0) + 1);
                }

                for (Map.Entry<String, Integer> entry : emojiCounts.entrySet()) {
                    TextView emojiView = new TextView(itemView.getContext());
                    String text = entry.getKey() + (entry.getValue() > 1 ? " " + entry.getValue() : "");
                    emojiView.setText(text);
                    emojiView.setTextSize(12);
                    emojiView.setPadding(8, 2, 8, 2);
                    reactionContainer.addView(emojiView);
                }
            } else {
                reactionContainer.setVisibility(View.GONE);
            }
        }
    }

    class ReceivedMessageViewHolder extends BaseMessageViewHolder {
        TextView messageText, replyQuoteName, replyQuoteText;
        LinearLayout reactionContainer;
        View replyQuoteLayout;
        private final GestureDetector gestureDetector;

        ReceivedMessageViewHolder(View itemView, OnMessageInteractionListener listener) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            reactionContainer = itemView.findViewById(R.id.reaction_container);
            replyQuoteLayout = itemView.findViewById(R.id.reply_quote_layout);
            replyQuoteName = itemView.findViewById(R.id.reply_quote_name);
            replyQuoteText = itemView.findViewById(R.id.reply_quote_text);

            gestureDetector = new GestureDetector(itemView.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(android.view.MotionEvent e) {
                    listener.onMessageDoubleTapped(messageList.get(getAdapterPosition()));
                    return true;
                }
            });

            itemView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

            itemView.setOnLongClickListener(v -> {
                listener.onMessageLongClicked(messageList.get(getAdapterPosition()));
                return true;
            });
        }

        void bind(Message message) {
            messageText.setText(message.getText());
            bindReactions(message);
            bindReply(message);
        }

        private void bindReply(Message message) {
            if (message.getReplyToMessageId() != null) {
                Message originalMessage = messageMap.get(message.getReplyToMessageId());
                if (originalMessage != null) {
                    replyQuoteLayout.setVisibility(View.VISIBLE);
                    boolean isReplyToSelf = originalMessage.getSenderId().equals(currentUserId);
                    replyQuoteName.setText(isReplyToSelf ? "You" : strangerName);
                    replyQuoteText.setText(originalMessage.getText());
                } else {
                    replyQuoteLayout.setVisibility(View.GONE);
                }
            } else {
                replyQuoteLayout.setVisibility(View.GONE);
            }
        }

        private void bindReactions(Message message) {
            reactionContainer.removeAllViews();
            if (message.getReactions() != null && !message.getReactions().isEmpty()) {
                reactionContainer.setVisibility(View.VISIBLE);
                Map<String, Integer> emojiCounts = new HashMap<>();
                for (String emoji : message.getReactions().values()) {
                    emojiCounts.put(emoji, emojiCounts.getOrDefault(emoji, 0) + 1);
                }

                for (Map.Entry<String, Integer> entry : emojiCounts.entrySet()) {
                    TextView emojiView = new TextView(itemView.getContext());
                    String text = entry.getKey() + (entry.getValue() > 1 ? " " + entry.getValue() : "");
                    emojiView.setText(text);
                    emojiView.setTextSize(12);
                    emojiView.setPadding(8, 2, 8, 2);
                    reactionContainer.addView(emojiView);
                }
            } else {
                reactionContainer.setVisibility(View.GONE);
            }
        }
    }

    class SystemMessageViewHolder extends RecyclerView.ViewHolder {
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