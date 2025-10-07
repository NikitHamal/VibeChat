package com.vibez.chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private EditText usernameEditText, ageEditText;
    private TextView genderTextView;
    private MaterialButton startChatButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mUsersRef;
    private DatabaseReference mQueueRef;
    private FirebaseUser currentUser;
    private ValueEventListener mUserValueListener;

    private final String[] genders = {"Male", "Female", "Other"};
    private int selectedGenderIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mUsersRef = FirebaseDatabase.getInstance().getReference("users");
        mQueueRef = FirebaseDatabase.getInstance().getReference("queue");

        toolbar = findViewById(R.id.toolbar);
        usernameEditText = findViewById(R.id.username_edit_text);
        ageEditText = findViewById(R.id.age_edit_text);
        genderTextView = findViewById(R.id.gender_text_view);
        startChatButton = findViewById(R.id.start_chat_button);

        setSupportActionBar(toolbar);

        loadUserData();

        genderTextView.setOnClickListener(v -> showGenderSelectionDialog());
        startChatButton.setOnClickListener(v -> startChat());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadUserData() {
        if (currentUser != null) {
            mUserValueListener = mUsersRef.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        usernameEditText.setText(user.getName());
                        // We will add age and gender to the User model later
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(HomeActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
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
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void startChat() {
        String username = usernameEditText.getText().toString().trim();
        String age = ageEditText.getText().toString().trim();
        String gender = genderTextView.getText().toString().trim();

        if (username.isEmpty() || age.isEmpty() || gender.equals(getString(R.string.gender))) {
            Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show();
            return;
        }

        saveUserDataToFirebase(username, gender, age);

        startChatButton.setEnabled(false);
        startChatButton.setText("Searching...");

        // Add user to the queue
        DatabaseReference userInQueueRef = mQueueRef.child(currentUser.getUid());
        userInQueueRef.setValue(true).addOnSuccessListener(aVoid -> {
            findMatch();
        });
    }

    private void findMatch() {
        mQueueRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() > 1) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String otherUserId = userSnapshot.getKey();
                        if (otherUserId != null && !otherUserId.equals(currentUser.getUid())) {
                            // Match found
                            mQueueRef.removeEventListener(this); // Stop listening to the queue
                            createChatRoom(otherUserId);
                            return;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Matchmaking failed.", Toast.LENGTH_SHORT).show();
                startChatButton.setEnabled(true);
                startChatButton.setText("Start Chat");
            }
        });
    }

    private void createChatRoom(String otherUserId) {
        DatabaseReference chatRoomRef = FirebaseDatabase.getInstance().getReference("chats").push();
        String chatRoomId = chatRoomRef.getKey();

        // Add users to the chat room
        chatRoomRef.child("users").child(currentUser.getUid()).setValue(true);
        chatRoomRef.child("users").child(otherUserId).setValue(true);

        // Add a system message
        DatabaseReference messagesRef = chatRoomRef.child("messages");
        String messageId = messagesRef.push().getKey();
        Message systemMessage = new Message("You are now connected. Be nice!", "system", System.currentTimeMillis());
        messagesRef.child(messageId).setValue(systemMessage);

        // Remove users from the queue
        mQueueRef.child(currentUser.getUid()).removeValue();
        mQueueRef.child(otherUserId).removeValue();

        // Navigate to ChatActivity
        Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
        intent.putExtra("CHAT_ROOM_ID", chatRoomId);
        startActivity(intent);

        // Re-enable the button
        startChatButton.setEnabled(true);
        startChatButton.setText("Start Chat");
    }

    private void saveUserDataToFirebase(String username, String gender, String age) {
        if (currentUser != null) {
            DatabaseReference currentUserRef = mUsersRef.child(currentUser.getUid());
            currentUserRef.child("name").setValue(username);
            currentUserRef.child("gender").setValue(gender);
            currentUserRef.child("age").setValue(Integer.parseInt(age));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUserValueListener != null && currentUser != null) {
            mUsersRef.child(currentUser.getUid()).removeEventListener(mUserValueListener);
        }
    }
}