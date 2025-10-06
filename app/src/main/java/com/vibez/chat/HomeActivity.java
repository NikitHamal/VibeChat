package com.vibez.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class HomeActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText, ageEditText, genderEditText;
    private SharedPreferences sharedPreferences;
    private String selectedGender = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before setContentView
        sharedPreferences = getSharedPreferences("VibezPrefs", MODE_PRIVATE);
        int themeMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(themeMode);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Set up toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Initialize views
        usernameEditText = findViewById(R.id.username_edit_text);
        ageEditText = findViewById(R.id.age_edit_text);
        genderEditText = findViewById(R.id.gender_edit_text);

        // Load saved user data
        loadUserData();

        // Set up gender selection dialog
        genderEditText.setOnClickListener(v -> showGenderDialog());

        // Set up start chat button
        findViewById(R.id.start_chat_button).setOnClickListener(v -> startChat());
    }

    private void loadUserData() {
        String savedUsername = sharedPreferences.getString("username", "");
        String savedAge = sharedPreferences.getString("age", "");
        String savedGender = sharedPreferences.getString("gender", "");

        usernameEditText.setText(savedUsername);
        ageEditText.setText(savedAge);
        if (!savedGender.isEmpty()) {
            selectedGender = savedGender;
            genderEditText.setText(savedGender);
        }
    }

    private void showGenderDialog() {
        String[] genders = getResources().getStringArray(R.array.genders);
        int checkedItem = -1;
        
        // Find currently selected gender index
        for (int i = 0; i < genders.length; i++) {
            if (genders[i].equals(selectedGender)) {
                checkedItem = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.select_gender)
                .setSingleChoiceItems(genders, checkedItem, (dialog, which) -> {
                    selectedGender = genders[which];
                    genderEditText.setText(selectedGender);
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void startChat() {
        String username = usernameEditText.getText().toString().trim();
        String age = ageEditText.getText().toString().trim();

        if (username.isEmpty() || selectedGender.isEmpty()) {
            Toast.makeText(this, R.string.please_fill_username_gender, Toast.LENGTH_SHORT).show();
            return;
        }

        // Save user data
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.putString("age", age);
        editor.putString("gender", selectedGender);
        editor.apply();

        // Start chat activity
        Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
