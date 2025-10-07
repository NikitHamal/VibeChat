package com.vibez.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private EditText usernameEditText, ageEditText;
    private TextView genderTextView;
    private MaterialButton startChatButton, cancelChatButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mUsersRef;
    private DatabaseReference mQueueRef;
    private FirebaseUser currentUser;

    private ValueEventListener queueListener;
    private boolean isSearching = false;

    private final String[] genders = {"Male", "Female", "Other"};
    private int selectedGenderIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeFirebase();
        initializeViews();
        setSupportActionBar(toolbar);
        loadUserData();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if the user was searching and restore the UI state
        mQueueRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isSearching = snapshot.exists();
                updateUiForSearch(isSearching);
                if (isSearching) {
                    listenForMatch();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }
        mUsersRef = FirebaseDatabase.getInstance().getReference("users");
        mQueueRef = FirebaseDatabase.getInstance().getReference("queue");
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        usernameEditText = findViewById(R.id.username_edit_text);
        ageEditText = findViewById(R.id.age_edit_text);
        genderTextView = findViewById(R.id.gender_text_view);
        startChatButton = findViewById(R.id.start_chat_button);
        cancelChatButton = findViewById(R.id.cancel_chat_button);
    }

    private void setupListeners() {
        genderTextView.setOnClickListener(v -> showGenderSelectionDialog());
        startChatButton.setOnClickListener(v -> startSearch());
        cancelChatButton.setOnClickListener(v -> cancelSearch());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadUserData() {
        mUsersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    usernameEditText.setText(user.getName());
                    if (user.getAge() > 0) {
                        ageEditText.setText(String.valueOf(user.getAge()));
                    }
                    if (user.getGender() != null && !user.getGender().isEmpty()) {
                        genderTextView.setText(user.getGender());
                        genderTextView.setTextColor(MaterialColors.getColor(HomeActivity.this, com.google.android.material.R.attr.colorOnSurface, 0));
                        for (int i = 0; i < genders.length; i++) {
                            if (genders[i].equals(user.getGender())) {
                                selectedGenderIndex = i;
                                break;
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showGenderSelectionDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.gender)
                .setSingleChoiceItems(genders, selectedGenderIndex, (dialog, which) -> {
                    selectedGenderIndex = which;
                    genderTextView.setText(genders[which]);
                    genderTextView.setTextColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface, 0));
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void startSearch() {
        String username = usernameEditText.getText().toString().trim();
        String ageStr = ageEditText.getText().toString().trim();
        String gender = genderTextView.getText().toString().trim();

        if (username.isEmpty() || ageStr.isEmpty() || gender.equals(getString(R.string.gender))) {
            Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show();
            return;
        }

        saveUserDataToFirebase(username, gender, Integer.parseInt(ageStr));

        // Add user to the queue
        mQueueRef.child(currentUser.getUid()).setValue(true).addOnSuccessListener(aVoid -> {
            isSearching = true;
            updateUiForSearch(true);
            findAndClaimMatch();
            listenForMatch();
        });
    }

    private void cancelSearch() {
        isSearching = false;
        updateUiForSearch(false);
        mQueueRef.child(currentUser.getUid()).removeValue();
        if (queueListener != null) {
            mQueueRef.child(currentUser.getUid()).removeEventListener(queueListener);
        }
    }

    private void updateUiForSearch(boolean isSearching) {
        if (isSearching) {
            startChatButton.setText("Searching...");
            startChatButton.setEnabled(false);
            cancelChatButton.setVisibility(View.VISIBLE);
        } else {
            startChatButton.setText(R.string.start_chat);
            startChatButton.setEnabled(true);
            cancelChatButton.setVisibility(View.GONE);
        }
    }

    private void listenForMatch() {
        queueListener = mQueueRef.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("matchedWith")) {
                    String chatRoomId = snapshot.child("chatRoomId").getValue(String.class);
                    if (chatRoomId != null) {
                        mQueueRef.child(currentUser.getUid()).removeEventListener(this);
                        navigateToChat(chatRoomId);
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
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String otherUserId = userSnapshot.getKey();
                    if (isSearching && otherUserId != null && !otherUserId.equals(currentUser.getUid())) {
                        attemptToClaim(otherUserId);
                        break; // Attempt to claim the first available user
                    }
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
                    String chatRoomId = mUsersRef.push().getKey();
                    mutableData.child("matchedWith").setValue(currentUser.getUid());
                    mutableData.child("chatRoomId").setValue(chatRoomId);
                    return Transaction.success(mutableData);
                }
                // The user is not available (already being claimed or left)
                return Transaction.abort();
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (committed && error == null) {
                    // Successfully claimed the other user. Now update our own status.
                    String chatRoomId = currentData.child("chatRoomId").getValue(String.class);
                    mQueueRef.child(currentUser.getUid()).child("matchedWith").setValue(otherUserId);
                    mQueueRef.child(currentUser.getUid()).child("chatRoomId").setValue(chatRoomId);
                } else {
                    // Failed to claim, look for another match
                    findAndClaimMatch();
                }
            }
        });
    }

    private void navigateToChat(String chatRoomId) {
        isSearching = false;
        updateUiForSearch(false);
        mQueueRef.child(currentUser.getUid()).removeValue();

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("CHAT_ROOM_ID", chatRoomId);
        startActivity(intent);
    }

    private void saveUserDataToFirebase(String username, String gender, int age) {
        if (currentUser != null) {
            DatabaseReference currentUserRef = mUsersRef.child(currentUser.getUid());
            currentUserRef.child("name").setValue(username);
            currentUserRef.child("gender").setValue(gender);
            currentUserRef.child("age").setValue(age);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (queueListener != null) {
            mQueueRef.child(currentUser.getUid()).removeEventListener(queueListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // If the user is searching and closes the app, ensure they are removed from the queue
        if (isSearching) {
            mQueueRef.child(currentUser.getUid()).removeValue();
        }
    }
}