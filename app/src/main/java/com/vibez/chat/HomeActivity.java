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
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class HomeActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "VibeZPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_AGE = "age";

    private MaterialToolbar toolbar;
    private EditText usernameEditText, ageEditText;
    private TextView genderTextView;
    private MaterialButton startChatButton;
    private SharedPreferences sharedPreferences;

    private final String[] genders = {"Male", "Female", "Other"};
    private int selectedGenderIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply saved theme before setting content view
        applySavedTheme();

        setContentView(R.layout.activity_home);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        usernameEditText = findViewById(R.id.username_edit_text);
        ageEditText = findViewById(R.id.age_edit_text);
        genderTextView = findViewById(R.id.gender_text_view);
        startChatButton = findViewById(R.id.start_chat_button);

        // Setup Toolbar
        setSupportActionBar(toolbar);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load user data
        loadUserData();

        // Set listeners
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
        String username = sharedPreferences.getString(KEY_USERNAME, "");
        String gender = sharedPreferences.getString(KEY_GENDER, "");
        String age = sharedPreferences.getString(KEY_AGE, "");

        usernameEditText.setText(username);
        ageEditText.setText(age);

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

        saveUserData(username, gender, age);

        Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
        startActivity(intent);
    }

    private void saveUserData(String username, String gender, String age) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_GENDER, gender);
        editor.putString(KEY_AGE, age);
        editor.apply();
    }

    private void applySavedTheme() {
        SharedPreferences prefs = getSharedPreferences("VibeZPrefs", MODE_PRIVATE);
        String themeValue = prefs.getString("theme", "system");

        switch (themeValue) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "system":
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}