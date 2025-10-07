package com.vibez.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.content.ClipboardManager;
import android.content.ClipData;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.graphics.Canvas;
import androidx.core.content.ContextCompat;
import android.graphics.drawable.Drawable;
import android.view.View;


public class ChatActivity extends AppCompatActivity implements ChatAdapter.OnMessageInteractionListener {

    // Views
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private EditText messageEditText;
    private MaterialButton sendButton, nextButton, cancelMatchmakingButton;
    private TextView strangerNameTextView, strangerDetailsTextView, strangerFlagTextView;
    private FrameLayout connectingOverlay;
    private MaterialToolbar toolbar;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mUsersRef;
    private DatabaseReference mQueueRef;
    private DatabaseReference mChatRoomRef;

    // State
    private List<Message> messageList;
    private java.util.Map<String, Message> messageMap;
    private String chatRoomId;
    private String otherUserId;
    private boolean isMatchmaking = false;
    private ValueEventListener queueListener;

    // Typing Indicator
    private DatabaseReference mTypingRef;
    private ValueEventListener typingListener;
    private TextView typingIndicatorTextView;
    private final android.os.Handler typingHandler = new android.os.Handler();
    private Runnable typingRunnable;
    private Message selectedMessage;
    private Message messageToReply;

    // Reply Preview Views
    private View replyPreviewLayout;
    private TextView replyPreviewName;
    private TextView replyPreviewText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initializeFirebase();
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();

        startMatchmaking();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        mUsersRef = FirebaseDatabase.getInstance().getReference("users");
        mQueueRef = FirebaseDatabase.getInstance().getReference("queue");
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        strangerNameTextView = findViewById(R.id.stranger_name);
        strangerDetailsTextView = findViewById(R.id.stranger_details);
        strangerFlagTextView = findViewById(R.id.stranger_flag);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);
        nextButton = findViewById(R.id.next_button);
        connectingOverlay = findViewById(R.id.connecting_overlay);
        cancelMatchmakingButton = findViewById(R.id.cancel_matchmaking_button);
        typingIndicatorTextView = findViewById(R.id.typing_indicator);
        replyPreviewLayout = findViewById(R.id.reply_preview_layout);
        replyPreviewName = findViewById(R.id.reply_preview_name);
        replyPreviewText = findViewById(R.id.reply_preview_text);
        findViewById(R.id.cancel_reply_button).setOnClickListener(v -> hideReplyPreview());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                leaveChat(true); // Pass true for back-pressed
            }
        });
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        messageMap = new java.util.HashMap<>();
        // Initial setup with a placeholder name. It will be updated when the match is made.
        chatAdapter = new ChatAdapter(messageList, messageMap, "", this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToReplyCallback());
        itemTouchHelper.attachToRecyclerView(chatRecyclerView);
    }

    private void setupListeners() {
        sendButton.setOnClickListener(v -> sendMessage());
        nextButton.setOnClickListener(v -> findNewMatch());
        cancelMatchmakingButton.setOnClickListener(v -> cancelMatchmaking());
    }

    private void startMatchmaking() {
        isMatchmaking = true;
        connectingOverlay.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.GONE);

        mQueueRef.child(currentUser.getUid()).setValue(true).addOnSuccessListener(aVoid -> {
            listenForMatch();
            findAndClaimMatch();
        });
    }

    private void cancelMatchmaking() {
        isMatchmaking = false;
        mQueueRef.child(currentUser.getUid()).removeValue();
        finish();
    }

    private void listenForMatch() {
        queueListener = mQueueRef.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isMatchmaking && snapshot.exists() && snapshot.hasChild("matchedWith")) {
                    isMatchmaking = false;
                    chatRoomId = snapshot.child("chatRoomId").getValue(String.class);
                    ChatActivity.this.otherUserId = snapshot.child("matchedWith").getValue(String.class);

                    if (chatRoomId != null && ChatActivity.this.otherUserId != null) {
                        mQueueRef.child(currentUser.getUid()).removeEventListener(queueListener);
                        mChatRoomRef = FirebaseDatabase.getInstance().getReference("chats").child(chatRoomId);
                        mTypingRef = mChatRoomRef.child("typing");

                        connectingOverlay.setVisibility(View.GONE);
                        toolbar.setVisibility(View.VISIBLE);

                        fetchAndDisplayStrangerInfo(ChatActivity.this.otherUserId);
                        listenForMessages();
                        setupTypingIndicator();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void findAndClaimMatch() {
        mQueueRef.orderByValue().equalTo(true).limitToFirst(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isMatchmaking) return; // Stop if matchmaking was cancelled
                boolean matchFound = false;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String otherUserId = userSnapshot.getKey();
                    if (otherUserId != null && !otherUserId.equals(currentUser.getUid())) {
                        attemptToClaim(otherUserId);
                        matchFound = true;
                        break;
                    }
                }
                if (!matchFound) {
                    // Could add a timeout here if desired
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void attemptToClaim(String otherUserId) {
        DatabaseReference otherUserRef = mQueueRef.child(otherUserId);
        otherUserRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                if (mutableData.getValue() instanceof Boolean && (Boolean) mutableData.getValue()) {
                    String newChatRoomId = mUsersRef.push().getKey();
                    mutableData.child("matchedWith").setValue(currentUser.getUid());
                    mutableData.child("chatRoomId").setValue(newChatRoomId);
                    return Transaction.success(mutableData);
                }
                return Transaction.abort();
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (committed && error == null) {
                    String matchedChatRoomId = currentData.child("chatRoomId").getValue(String.class);
                    // Set match info for the current user
                    mQueueRef.child(currentUser.getUid()).child("matchedWith").setValue(otherUserId);
                    mQueueRef.child(currentUser.getUid()).child("chatRoomId").setValue(matchedChatRoomId);

                    // Create the participants node in the new chat room
                    DatabaseReference newChatRoomRef = FirebaseDatabase.getInstance().getReference("chats").child(matchedChatRoomId);
                    long timestamp = System.currentTimeMillis();
                    newChatRoomRef.child("participants").child(currentUser.getUid()).setValue(new Participant("active", timestamp));
                    newChatRoomRef.child("participants").child(otherUserId).setValue(new Participant("active", timestamp));

                } else {
                    // Failed to claim, look for another match after a short delay
                    if (isMatchmaking) { // Only retry if we are still in matchmaking mode
                        new android.os.Handler().postDelayed(ChatActivity.this::findAndClaimMatch, 1000);
                    }
                }
            }
        });
    }

    private void fetchAndDisplayStrangerInfo(String userId) {
        mUsersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User stranger = snapshot.getValue(User.class);
                if (stranger != null) {
                    String name = stranger.getName();
                    strangerNameTextView.setText(name);

                    // Re-create the adapter with the stranger's name for reply quotes
                    chatAdapter = new ChatAdapter(messageList, messageMap, name, ChatActivity.this);
                    chatRecyclerView.setAdapter(chatAdapter);

                    if ("Anonymous".equals(name)) {
                        strangerDetailsTextView.setVisibility(View.GONE);
                        strangerFlagTextView.setText("🤫");
                    } else {
                        strangerDetailsTextView.setText(stranger.getGender() + ", " + stranger.getAge());
                        strangerDetailsTextView.setVisibility(View.VISIBLE);
                        strangerFlagTextView.setText(getFlagFromCountry(stranger.getCountry()));
                        strangerFlagTextView.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private String getFlagFromCountry(String country) {
        // This is a simplified mapping. A real app would use a library.
        if (country == null) return "🏳️";
        switch (country) {
            case "United States": return "🇺🇸";
            case "Canada": return "🇨🇦";
            case "United Kingdom": return "🇬🇧";
            case "Australia": return "🇦🇺";
            case "India": return "🇮🇳";
            default: return "🏳️";
        }
    }

    private void listenForMessages() {
        messageList.clear();
        messageMap.clear();
        mChatRoomRef.child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                if (message != null && message.getMessageId() != null) {
                    messageList.add(message);
                    messageMap.put(message.getMessageId(), message);
                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                    chatRecyclerView.scrollToPosition(messageList.size() - 1);
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message updatedMessage = snapshot.getValue(Message.class);
                if (updatedMessage == null || updatedMessage.getMessageId() == null) return;

                messageMap.put(updatedMessage.getMessageId(), updatedMessage);
                for (int i = 0; i < messageList.size(); i++) {
                    if (messageList.get(i).getMessageId().equals(updatedMessage.getMessageId())) {
                        messageList.set(i, updatedMessage);
                        chatAdapter.notifyItemChanged(i);
                        break;
                    }
                }
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                 Message removedMessage = snapshot.getValue(Message.class);
                 if (removedMessage == null || removedMessage.getMessageId() == null) return;

                 messageMap.remove(removedMessage.getMessageId());
                 for (int i = 0; i < messageList.size(); i++) {
                     if (messageList.get(i).getMessageId().equals(removedMessage.getMessageId())) {
                         messageList.remove(i);
                         chatAdapter.notifyItemRemoved(i);
                         break;
                     }
                 }
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (!messageText.isEmpty() && mChatRoomRef != null) {
            String messageId = mChatRoomRef.child("messages").push().getKey();
            if (messageId != null) {
                Message message = new Message(messageId, messageText, currentUser.getUid(), System.currentTimeMillis(), "text");
                if (messageToReply != null) {
                    message.setReplyToMessageId(messageToReply.getMessageId());
                }
                mChatRoomRef.child("messages").child(messageId).setValue(message);
                messageEditText.setText("");
                hideReplyPreview();
            }
        }
    }

    private void leaveChat(boolean isBackPressed) {
        if (mChatRoomRef != null && currentUser != null) {
            // 1. Send "User left" system message
            String messageId = mChatRoomRef.child("messages").push().getKey();
            if (messageId != null) {
                Message systemMessage = new Message(messageId, "User left the chat", "system", System.currentTimeMillis(), "system");
                mChatRoomRef.child("messages").child(messageId).setValue(systemMessage);
            }

            // 2. Update participant status to "left"
            DatabaseReference participantRef = mChatRoomRef.child("participants").child(currentUser.getUid());
            participantRef.setValue(new Participant("left", System.currentTimeMillis()));

            // 3. Check if both participants have left to delete the chat
            checkAndDeleteChatRoom();

            // 4. Handle exit behavior
            if (isBackPressed) {
                // Delayed exit for back press
                new android.os.Handler().postDelayed(this::finish, 1000);
            } else {
                // Immediate exit for "Next" button
                finish();
            }
        } else {
            finish();
        }
    }

    private void findNewMatch() {
        // This is now similar to leaveChat, but it will trigger a restart.
        if (mChatRoomRef != null && currentUser != null) {
            DatabaseReference participantRef = mChatRoomRef.child("participants").child(currentUser.getUid());
            participantRef.setValue(new Participant("left", System.currentTimeMillis()));
            checkAndDeleteChatRoom();
        }
        recreate(); // Restart the activity to find a new match
    }

    private void setupTypingIndicator() {
        // Listener for the other user's typing status
        typingListener = mTypingRef.child(otherUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue(Boolean.class)) {
                    typingIndicatorTextView.setText(R.string.typing_indicator_text);
                    typingIndicatorTextView.setVisibility(View.VISIBLE);
                } else {
                    typingIndicatorTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Runnable to set typing status to false after a delay
        typingRunnable = () -> mTypingRef.child(currentUser.getUid()).setValue(false);

        // TextWatcher to detect user input
        messageEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mTypingRef != null) {
                    typingHandler.removeCallbacks(typingRunnable);
                    mTypingRef.child(currentUser.getUid()).setValue(true);
                    typingHandler.postDelayed(typingRunnable, 2000); // 2-second delay
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void checkAndDeleteChatRoom() {
        if (mChatRoomRef == null) return;
        mChatRoomRef.child("participants").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                boolean allLeft = true;
                int participantCount = 0;
                for (DataSnapshot participantSnapshot : snapshot.getChildren()) {
                    participantCount++;
                    Participant p = participantSnapshot.getValue(Participant.class);
                    if (p != null && !"left".equals(p.getStatus())) {
                        allLeft = false;
                        break;
                    }
                }

                // Delete only if there were two participants and both have left
                if (participantCount > 1 && allLeft) {
                    mChatRoomRef.removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Log error or handle
            }
        });
    }

    @Override
    public void onMessageLongClicked(Message message) {
        this.selectedMessage = message;
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottom_sheet_reactions, findViewById(R.id.emoji_reactions_layout));

        // Emoji click listeners
        bottomSheetView.findViewById(R.id.reaction_love).setOnClickListener(v -> { addReaction("❤️"); bottomSheetDialog.dismiss(); });
        bottomSheetView.findViewById(R.id.reaction_laugh).setOnClickListener(v -> { addReaction("😂"); bottomSheetDialog.dismiss(); });
        bottomSheetView.findViewById(R.id.reaction_wow).setOnClickListener(v -> { addReaction("😮"); bottomSheetDialog.dismiss(); });
        bottomSheetView.findViewById(R.id.reaction_sad).setOnClickListener(v -> { addReaction("😢"); bottomSheetDialog.dismiss(); });
        bottomSheetView.findViewById(R.id.reaction_angry).setOnClickListener(v -> { addReaction("😠"); bottomSheetDialog.dismiss(); });

        // Action click listeners
        bottomSheetView.findViewById(R.id.action_copy).setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("message", selectedMessage.getText());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Message copied", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.action_reply).setOnClickListener(v -> {
            showReplyPreview(selectedMessage);
            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.action_report).setOnClickListener(v -> {
            Toast.makeText(this, "Report functionality is not yet implemented.", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    @Override
    public void onMessageDoubleTapped(Message message) {
        this.selectedMessage = message;
        addReaction("❤️");
    }

    private void addReaction(String emoji) {
        if (selectedMessage == null || mChatRoomRef == null || currentUser == null) return;

        DatabaseReference messageRef = mChatRoomRef.child("messages").child(selectedMessage.getMessageId()).child("reactions");
        messageRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // If the user has already reacted with the same emoji, remove the reaction (toggle off)
                if (snapshot.exists() && emoji.equals(snapshot.getValue(String.class))) {
                    messageRef.child(currentUser.getUid()).removeValue();
                } else {
                    // Otherwise, add or update the reaction
                    messageRef.child(currentUser.getUid()).setValue(emoji);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showReplyPreview(Message message) {
        messageToReply = message;
        replyPreviewLayout.setVisibility(View.VISIBLE);

        String name = message.getSenderId().equals(currentUser.getUid()) ? "You" : strangerNameTextView.getText().toString();
        replyPreviewName.setText(name);
        replyPreviewText.setText(message.getText());

        // Request focus and show keyboard
        messageEditText.requestFocus();
    }

    private void hideReplyPreview() {
        messageToReply = null;
        replyPreviewLayout.setVisibility(View.GONE);
    }

    // --- Swipe to Reply Inner Class ---
    public class SwipeToReplyCallback extends ItemTouchHelper.SimpleCallback {

        private final Drawable icon;
        private final Drawable background;

        SwipeToReplyCallback() {
            super(0, ItemTouchHelper.LEFT);
            icon = ContextCompat.getDrawable(ChatActivity.this, R.drawable.ic_reply);
            background = new android.graphics.drawable.ColorDrawable(ContextCompat.getColor(ChatActivity.this, R.color.primaryContainer));
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                showReplyPreview(messageList.get(position));
                chatAdapter.notifyItemChanged(position); // To reset the swipe view
            }
        }

        @Override
        public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            // Disable swipe for system messages
            if (viewHolder instanceof ChatAdapter.SystemMessageViewHolder) {
                return 0;
            }
            return super.getSwipeDirs(recyclerView, viewHolder);
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            View itemView = viewHolder.itemView;

            int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + iconMargin;
            int iconBottom = iconTop + icon.getIntrinsicHeight();

            if (dX < 0) { // Swiping to the left
                int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                int iconRight = itemView.getRight() - iconMargin;
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                background.setBounds(itemView.getRight() + ((int) dX), itemView.getTop(), itemView.getRight(), itemView.getBottom());
            } else {
                background.setBounds(0, 0, 0, 0);
            }
            background.draw(c);
            icon.draw(c);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up matchmaking listeners
        if (isMatchmaking) {
            mQueueRef.child(currentUser.getUid()).removeValue();
        }
        if (queueListener != null && currentUser != null) {
            mQueueRef.child(currentUser.getUid()).removeEventListener(queueListener);
        }

        // Clean up typing indicator resources
        if (mTypingRef != null && currentUser != null) {
            mTypingRef.child(currentUser.getUid().toString()).setValue(false); // Clear own typing status
            if (typingListener != null && otherUserId != null) {
                mTypingRef.child(otherUserId).removeEventListener(typingListener);
            }
        }
        if (typingHandler != null && typingRunnable != null) {
            typingHandler.removeCallbacks(typingRunnable);
        }
    }
}