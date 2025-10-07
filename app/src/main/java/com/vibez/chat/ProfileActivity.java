package com.vibez.chat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "VibeZPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_AGE = "age";
    private static final String KEY_COUNTRY = "country";

    private MaterialToolbar toolbar;
    private FloatingActionButton fabEdit;
    
    // Display TextViews
    private TextView nameDisplay, genderDisplay, ageDisplay, countryDisplay;
    
    // Edit Views
    private EditText nameEdit, ageEdit, countryEdit;
    private TextView genderEdit;
    
    private SharedPreferences sharedPreferences;
    private boolean isEditMode = false;
    
    private final String[] genders = {"Male", "Female", "Other"};
    private int selectedGenderIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            if (isEditMode) {
                // Ask user to save or discard changes
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Unsaved Changes")
                        .setMessage("Do you want to save your changes?")
                        .setPositiveButton("Save", (dialog, which) -> {
                            saveProfile();
                            finish();
                        })
                        .setNegativeButton("Discard", (dialog, which) -> finish())
                        .setNeutralButton("Cancel", null)
                        .show();
            } else {
                finish();
            }
        });

        // Initialize views
        nameDisplay = findViewById(R.id.name_display);
        genderDisplay = findViewById(R.id.gender_display);
        ageDisplay = findViewById(R.id.age_display);
        countryDisplay = findViewById(R.id.country_display);
        
        nameEdit = findViewById(R.id.name_edit);
        genderEdit = findViewById(R.id.gender_edit);
        ageEdit = findViewById(R.id.age_edit);
        countryEdit = findViewById(R.id.country_edit);
        
        fabEdit = findViewById(R.id.fab_edit);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load profile data
        loadProfile();

        // Setup FAB click listener
        fabEdit.setOnClickListener(v -> {
            if (isEditMode) {
                saveProfile();
            } else {
                enterEditMode();
            }
        });

        // Setup gender edit click listener
        genderEdit.setOnClickListener(v -> showGenderDialog());
    }

    private void loadProfile() {
        String name = sharedPreferences.getString(KEY_USERNAME, "");
        String gender = sharedPreferences.getString(KEY_GENDER, "");
        String age = sharedPreferences.getString(KEY_AGE, "");
        String country = sharedPreferences.getString(KEY_COUNTRY, "");

        // Set display values
        nameDisplay.setText(name.isEmpty() ? "Not specified" : name);
        genderDisplay.setText(gender.isEmpty() ? "Not specified" : gender);
        ageDisplay.setText(age.isEmpty() ? "Not specified" : age);
        countryDisplay.setText(country.isEmpty() ? "Not specified" : country);

        // Set edit values
        nameEdit.setText(name);
        ageEdit.setText(age);
        countryEdit.setText(country);
        genderEdit.setText(gender.isEmpty() ? "Not specified" : gender);

        // Find selected gender index
        if (!gender.isEmpty()) {
            for (int i = 0; i < genders.length; i++) {
                if (genders[i].equals(gender)) {
                    selectedGenderIndex = i;
                    break;
                }
            }
        }
    }

    private void enterEditMode() {
        isEditMode = true;
        
        // Hide display views, show edit views
        nameDisplay.setVisibility(View.GONE);
        nameEdit.setVisibility(View.VISIBLE);
        
        genderDisplay.setVisibility(View.GONE);
        genderEdit.setVisibility(View.VISIBLE);
        
        ageDisplay.setVisibility(View.GONE);
        ageEdit.setVisibility(View.VISIBLE);
        
        countryDisplay.setVisibility(View.GONE);
        countryEdit.setVisibility(View.VISIBLE);

        // Change FAB icon to check
        fabEdit.setImageResource(R.drawable.ic_check);
        
        // Focus on name field
        nameEdit.requestFocus();
    }

    private void saveProfile() {
        String name = nameEdit.getText().toString().trim();
        String age = ageEdit.getText().toString().trim();
        String country = countryEdit.getText().toString().trim();
        String gender = genderEdit.getText().toString().trim();

        // Validate
        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, name);
        editor.putString(KEY_GENDER, gender.equals("Not specified") ? "" : gender);
        editor.putString(KEY_AGE, age);
        editor.putString(KEY_COUNTRY, country);
        editor.apply();

        // Update display
        loadProfile();
        exitEditMode();
        
        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
    }

    private void exitEditMode() {
        isEditMode = false;
        
        // Show display views, hide edit views
        nameDisplay.setVisibility(View.VISIBLE);
        nameEdit.setVisibility(View.GONE);
        
        genderDisplay.setVisibility(View.VISIBLE);
        genderEdit.setVisibility(View.GONE);
        
        ageDisplay.setVisibility(View.VISIBLE);
        ageEdit.setVisibility(View.GONE);
        
        countryDisplay.setVisibility(View.VISIBLE);
        countryEdit.setVisibility(View.GONE);

        // Change FAB icon back to edit
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
