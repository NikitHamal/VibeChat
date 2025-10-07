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

public class ChatActivity extends AppCompatActivity {

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
    private List<Message> messages;
    private String chatRoomId;
    private boolean isMatchmaking = false;
    private ValueEventListener queueListener;

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
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                leaveChat();
            }
        });
    }

    private void setupRecyclerView() {
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);
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
                if (snapshot.exists() && snapshot.hasChild("matchedWith")) {
                    isMatchmaking = false;
                    chatRoomId = snapshot.child("chatRoomId").getValue(String.class);
                    String otherUserId = snapshot.child("matchedWith").getValue(String.class);

                    if (chatRoomId != null && otherUserId != null) {
                        mQueueRef.child(currentUser.getUid()).removeEventListener(queueListener);
                        mChatRoomRef = FirebaseDatabase.getInstance().getReference("chats").child(chatRoomId);

                        connectingOverlay.setVisibility(View.GONE);
                        toolbar.setVisibility(View.VISIBLE);

                        fetchAndDisplayStrangerInfo(otherUserId);
                        listenForMessages();
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
                    mQueueRef.child(currentUser.getUid()).child("matchedWith").setValue(otherUserId);
                    mQueueRef.child(currentUser.getUid()).child("chatRoomId").setValue(matchedChatRoomId);
                } else {
                    // Failed to claim, look for another match after a short delay
                    new android.os.Handler().postDelayed(ChatActivity.this::findAndClaimMatch, 1000);
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
                    strangerNameTextView.setText(stranger.getName());
                    if ("Anonymous".equals(stranger.getName())) {
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
        messages.clear();
        mChatRoomRef.child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                if (message != null) {
                    messages.add(message);
                    chatAdapter.notifyItemInserted(messages.size() - 1);
                    chatRecyclerView.scrollToPosition(messages.size() - 1);
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
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
            Message message = new Message(messageText, currentUser.getUid(), System.currentTimeMillis());
            if (messageId != null) {
                mChatRoomRef.child("messages").child(messageId).setValue(message);
                messageEditText.setText("");
            }
        }
    }

    private void leaveChat() {
        if (mChatRoomRef != null) {
            mChatRoomRef.removeValue();
        }
        finish();
    }

    private void findNewMatch() {
        if (mChatRoomRef != null) {
            mChatRoomRef.removeValue();
        }
        recreate(); // Restart the activity to find a new match
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isMatchmaking) {
            mQueueRef.child(currentUser.getUid()).removeValue();
        }
        if (queueListener != null) {
            mQueueRef.child(currentUser.getUid()).removeEventListener(queueListener);
        }
    }
}