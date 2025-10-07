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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messages;
    private EditText messageEditText;
    private MaterialButton sendButton, nextButton;
    private TextView strangerNameTextView, strangerDetailsTextView, strangerFlagTextView;
    private FrameLayout loadingOverlay;
    private MaterialToolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mChatRoomRef;
    private String chatRoomId;
    private ChildEventListener mMessagesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRoomId = getIntent().getStringExtra("CHAT_ROOM_ID");
        if (chatRoomId == null) {
            Toast.makeText(this, "Error: Chat room not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mChatRoomRef = FirebaseDatabase.getInstance().getReference("chats").child(chatRoomId);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();

        loadStrangerData();
        listenForMessages();
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
        loadingOverlay = findViewById(R.id.loading_overlay);
        loadingOverlay.setVisibility(View.GONE);
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
        nextButton.setOnClickListener(v -> {
            leaveChat();
            // Go back to HomeActivity to find a new match
            Intent intent = new Intent(ChatActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void loadStrangerData() {
        mChatRoomRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    if (userId != null && !userId.equals(currentUser.getUid())) {
                        fetchAndDisplayStrangerInfo(userId);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Failed to load stranger data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAndDisplayStrangerInfo(String userId) {
        DatabaseReference strangerRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        strangerRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                        // For now, no flag for real users. This can be a future feature.
                        strangerFlagTextView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                 Toast.makeText(ChatActivity.this, "Failed to load stranger info.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void listenForMessages() {
        messages.clear();
        mMessagesListener = mChatRoomRef.child("messages").addChildEventListener(new ChildEventListener() {
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
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Failed to load messages.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (!messageText.isEmpty()) {
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
            // Remove the entire chat room from the database
            mChatRoomRef.removeValue();
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mChatRoomRef != null && mMessagesListener != null) {
            mChatRoomRef.child("messages").removeEventListener(mMessagesListener);
        }
    }
}