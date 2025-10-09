package com.vibez.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private FloatingActionButton fabEdit;
    
    // Display TextViews
    private TextView nameDisplay, genderDisplay, ageDisplay, countryDisplay, emailDisplay;
    
    // Edit Views
    private EditText nameEdit, ageEdit, countryEdit;
    private TextView genderEdit;
    
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private FirebaseUser currentUser;

    private boolean isEditMode = false;
    
    private final String[] genders = {"Male", "Female", "Other"};
    private int selectedGenderIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeFirebase();
        initializeViews();
        setupToolbar();
        loadProfile();
        setupClickListeners();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }
        mUserRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        fabEdit = findViewById(R.id.fab_edit);
        nameDisplay = findViewById(R.id.name_display);
        genderDisplay = findViewById(R.id.gender_display);
        ageDisplay = findViewById(R.id.age_display);
        countryDisplay = findViewById(R.id.country_display);
        emailDisplay = findViewById(R.id.email_display);
        
        nameEdit = findViewById(R.id.name_edit);
        genderEdit = findViewById(R.id.gender_edit);
        ageEdit = findViewById(R.id.age_edit);
        countryEdit = findViewById(R.id.country_edit);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupClickListeners() {
        fabEdit.setOnClickListener(v -> {
            if (isEditMode) {
                saveProfile();
            } else {
                enterEditMode();
            }
        });
        genderEdit.setOnClickListener(v -> showGenderDialog());
    }

    private void loadProfile() {
        mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Tolerant parsing to handle type mismatches
                com.google.firebase.database.GenericTypeIndicator<java.util.Map<String, Object>> t = new com.google.firebase.database.GenericTypeIndicator<java.util.Map<String, Object>>() {};
                java.util.Map<String, Object> data = snapshot.getValue(t);
                String name = data != null && data.get("name") != null ? String.valueOf(data.get("name")) : null;
                String gender = data != null && data.get("gender") != null ? String.valueOf(data.get("gender")) : null;
                String country = data != null && data.get("country") != null ? String.valueOf(data.get("country")) : null;
                String email = data != null && data.get("email") != null ? String.valueOf(data.get("email")) : null;
                int age = 0;
                if (data != null && data.get("age") != null) {
                    Object ageObj = data.get("age");
                    if (ageObj instanceof Number) age = ((Number) ageObj).intValue();
                    else { try { age = Integer.parseInt(String.valueOf(ageObj)); } catch (Exception ignore) { age = 0; } }
                }

                // Set display values
                nameDisplay.setText(name != null && !name.isEmpty() ? name : "Not specified");
                genderDisplay.setText(gender != null && !gender.isEmpty() ? gender : "Not specified");
                ageDisplay.setText(age > 0 ? String.valueOf(age) : "Not specified");
                countryDisplay.setText(country != null && !country.isEmpty() ? country : "Not specified");
                emailDisplay.setText(email != null && !email.isEmpty() ? email : "No email");

                // Set edit values
                nameEdit.setText(name != null ? name : "");
                ageEdit.setText(age > 0 ? String.valueOf(age) : "");
                countryEdit.setText(country != null ? country : "");
                genderEdit.setText(gender != null && !gender.isEmpty() ? gender : "Not specified");

                // Find selected gender index
                if (gender != null) {
                    for (int i = 0; i < genders.length; i++) {
                        if (genders[i].equals(gender)) {
                            selectedGenderIndex = i;
                            break;
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enterEditMode() {
        isEditMode = true;
        
        nameDisplay.setVisibility(View.GONE);
        nameEdit.setVisibility(View.VISIBLE);
        
        genderDisplay.setVisibility(View.GONE);
        genderEdit.setVisibility(View.VISIBLE);
        
        ageDisplay.setVisibility(View.GONE);
        ageEdit.setVisibility(View.VISIBLE);
        
        countryDisplay.setVisibility(View.GONE);
        countryEdit.setVisibility(View.VISIBLE);

        fabEdit.setImageResource(R.drawable.ic_check);
        nameEdit.requestFocus();
    }

    private void saveProfile() {
        String name = nameEdit.getText().toString().trim();
        String ageStr = ageEdit.getText().toString().trim();
        String country = countryEdit.getText().toString().trim();
        String gender = genderEdit.getText().toString();

        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mUserRef.child("name").setValue(name);
        mUserRef.child("country").setValue(country);
        mUserRef.child("gender").setValue(gender.equals("Not specified") ? null : gender);
        mUserRef.child("age").setValue(ageStr.isEmpty() ? 0 : Integer.parseInt(ageStr));

        loadProfile();
        exitEditMode();
        
        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
    }

    private void exitEditMode() {
        isEditMode = false;
        
        nameDisplay.setVisibility(View.VISIBLE);
        nameEdit.setVisibility(View.GONE);
        
        genderDisplay.setVisibility(View.VISIBLE);
        genderEdit.setVisibility(View.GONE);
        
        ageDisplay.setVisibility(View.VISIBLE);
        ageEdit.setVisibility(View.GONE);
        
        countryDisplay.setVisibility(View.VISIBLE);
        countryEdit.setVisibility(View.GONE);

        fabEdit.setImageResource(R.drawable.ic_edit);
    }

    private void showGenderDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Gender")
                .setSingleChoiceItems(genders, selectedGenderIndex, (dialog, which) -> {
                    selectedGenderIndex = which;
                    genderEdit.setText(genders[which]);
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (isEditMode) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Unsaved Changes")
                    .setMessage("Do you want to save your changes?")
                    .setPositiveButton("Save", (dialog, which) -> {
                        saveProfile();
                        super.onBackPressed();
                    })
                    .setNegativeButton("Discard", (dialog, which) -> super.onBackPressed())
                    .setNeutralButton("Cancel", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}