package com.vibez.chat;

import android.content.Intent;
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
    private FirebaseUser currentUser;

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

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }
        mUsersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        usernameEditText = findViewById(R.id.username_edit_text);
        ageEditText = findViewById(R.id.age_edit_text);
        genderTextView = findViewById(R.id.gender_text_view);
        startChatButton = findViewById(R.id.start_chat_button);
    }

    private void setupListeners() {
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

    private void startChat() {
        String username = usernameEditText.getText().toString().trim();
        String ageStr = ageEditText.getText().toString().trim();
        String gender = genderTextView.getText().toString().trim();

        if (username.isEmpty() || ageStr.isEmpty() || gender.equals(getString(R.string.gender))) {
            Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show();
            return;
        }

        saveUserDataToFirebase(username, gender, Integer.parseInt(ageStr));

        // Directly navigate to ChatActivity
        Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
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
}