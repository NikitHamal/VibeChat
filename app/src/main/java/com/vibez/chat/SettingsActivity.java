package com.vibez.chat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.appbar.MaterialToolbar;

public class SettingsActivity extends AppCompatActivity {

    private RadioGroup themeRadioGroup;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        sharedPreferences = getSharedPreferences("VibezPrefs", MODE_PRIVATE);
        themeRadioGroup = findViewById(R.id.theme_radio_group);

        // Load current theme preference
        int themeMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        
        // Set the appropriate radio button based on saved preference
        switch (themeMode) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                themeRadioGroup.check(R.id.radio_light);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                themeRadioGroup.check(R.id.radio_dark);
                break;
            default:
                themeRadioGroup.check(R.id.radio_system);
                break;
        }

        // Set up theme change listener
        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int selectedThemeMode;
            
            if (checkedId == R.id.radio_light) {
                selectedThemeMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.radio_dark) {
                selectedThemeMode = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                selectedThemeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }
            
            // Save preference
            sharedPreferences.edit().putInt("theme_mode", selectedThemeMode).apply();
            
            // Apply theme
            AppCompatDelegate.setDefaultNightMode(selectedThemeMode);
        });
    }
}
