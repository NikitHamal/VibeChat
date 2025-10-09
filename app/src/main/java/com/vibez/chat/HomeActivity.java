package com.vibez.chat;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private EditText usernameEditText, ageEditText;
    private TextView genderTextView;
    private MaterialButton startChatButton;
    private View userDetailsForm;
    private View anonymousModeCard;

    private FirebaseAuth mAuth;
    private DatabaseReference mUsersRef;
    private FirebaseUser currentUser;
    private SharedPreferences sharedPreferences;

    private final String[] genders = {"Male", "Female", "Other"};
    private int selectedGenderIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sharedPreferences = getSharedPreferences("VibezPrefs", MODE_PRIVATE);
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
        userDetailsForm = findViewById(R.id.user_details_form);
        anonymousModeCard = findViewById(R.id.anonymous_mode_card);
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
        if (currentUser.isAnonymous()) {
            userDetailsForm.setVisibility(View.GONE);
            anonymousModeCard.setVisibility(View.VISIBLE);
        } else {
            userDetailsForm.setVisibility(View.VISIBLE);
            anonymousModeCard.setVisibility(View.GONE);
            loadUserDataFromCache();
            loadUserDataFromFirebase();
        }
    }

    private void loadUserDataFromCache() {
        String name = sharedPreferences.getString("name", "");
        String gender = sharedPreferences.getString("gender", "");
        int age = sharedPreferences.getInt("age", 0);

        usernameEditText.setText(name);
        if (age > 0) {
            ageEditText.setText(String.valueOf(age));
        }
        if (gender != null && !gender.isEmpty()) {
            genderTextView.setText(gender);
            genderTextView.setTextColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface, 0));
            for (int i = 0; i < genders.length; i++) {
                if (genders[i].equals(gender)) {
                    selectedGenderIndex = i;
                    break;
                }
            }
        }
    }

    private void loadUserDataFromFirebase() {
        mUsersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Tolerant parsing to avoid type issues
                com.google.firebase.database.GenericTypeIndicator<java.util.Map<String, Object>> t = new com.google.firebase.database.GenericTypeIndicator<java.util.Map<String, Object>>() {};
                java.util.Map<String, Object> data = snapshot.getValue(t);
                User user = new User(currentUser.getUid(), currentUser.getDisplayName(), currentUser.getEmail(),
                        currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "");
                if (data != null) {
                    if (data.get("name") != null) user.setName(String.valueOf(data.get("name")));
                    if (data.get("gender") != null) user.setGender(String.valueOf(data.get("gender")));
                    if (data.get("country") != null) user.setCountry(String.valueOf(data.get("country")));
                    if (data.get("age") != null) {
                        Object ageObj = data.get("age");
                        if (ageObj instanceof Number) user.setAge(((Number) ageObj).intValue());
                        else {
                            try { user.setAge(Integer.parseInt(String.valueOf(ageObj))); } catch (Exception ignore) { user.setAge(0); }
                        }
                    }
                } else {
                    mUsersRef.child(currentUser.getUid()).setValue(user);
                }
                updateUIAndCache(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Failed to sync user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIAndCache(User user) {
        usernameEditText.setText(user.getName());
        if (user.getAge() > 0) {
            ageEditText.setText(String.valueOf(user.getAge()));
        } else {
            ageEditText.setText("");
        }

        if (user.getGender() != null && !user.getGender().isEmpty()) {
            genderTextView.setText(user.getGender());
            genderTextView.setTextColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface, 0));
            for (int i = 0; i < genders.length; i++) {
                if (genders[i].equals(user.getGender())) {
                    selectedGenderIndex = i;
                    break;
                }
            }
        } else {
            genderTextView.setText(getString(R.string.gender));
            genderTextView.setTextColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant, 0));
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", user.getName());
        editor.putString("gender", user.getGender());
        editor.putInt("age", user.getAge());
        editor.apply();
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
        if (currentUser.isAnonymous()) {
            DatabaseReference currentUserRef = mUsersRef.child(currentUser.getUid());
            // For anonymous users: name Anonymous, hide age/gender by setting empty/zero, default country blank
            currentUserRef.child("name").setValue("Anonymous");
            currentUserRef.child("gender").setValue("");
            currentUserRef.child("age").setValue(0);
            currentUserRef.child("country").setValue("");
            Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
            startActivity(intent);
            return;
        }

        String username = usernameEditText.getText().toString().trim();
        String ageStr = ageEditText.getText().toString().trim();
        String gender = genderTextView.getText().toString().trim();

        if (username.isEmpty() || ageStr.isEmpty() || gender.equals(getString(R.string.gender))) {
            Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show();
            return;
        }

        saveUserData(username, gender, Integer.parseInt(ageStr));

        Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
        startActivity(intent);
    }

    private void saveUserData(String username, String gender, int age) {
        if (currentUser != null) {
            // Save to Firebase
            DatabaseReference currentUserRef = mUsersRef.child(currentUser.getUid());
            currentUserRef.child("name").setValue(username);
            currentUserRef.child("gender").setValue(gender);
            currentUserRef.child("age").setValue(age);

            // Save to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("name", username);
            editor.putString("gender", gender);
            editor.putInt("age", age);
            editor.apply();
        }
    }
}