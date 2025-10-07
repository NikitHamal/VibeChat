package com.vibez.chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private TextView themeValue;
    private LinearLayout themeSetting;
    private LinearLayout profileSetting;

    private CharSequence[] themeEntries;
    private CharSequence[] themeValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        themeValue = findViewById(R.id.theme_value);
        themeSetting = findViewById(R.id.theme_setting);
        profileSetting = findViewById(R.id.profile_setting);

        themeEntries = getResources().getTextArray(R.array.theme_entries);
        themeValues = getResources().getTextArray(R.array.theme_values);

        updateThemeDisplay();

        themeSetting.setOnClickListener(v -> showThemeDialog());
        profileSetting.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void showThemeDialog() {
        String currentTheme = sharedPreferences.getString("theme", "system");
        int checkedItem = -1;
        for (int i = 0; i < themeValues.length; i++) {
            if (themeValues[i].equals(currentTheme)) {
                checkedItem = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Choose theme")
                .setSingleChoiceItems(themeEntries, checkedItem, (dialog, which) -> {
                    String selectedTheme = (String) themeValues[which];
                    sharedPreferences.edit().putString("theme", selectedTheme).apply();
                    applyTheme(selectedTheme);
                    updateThemeDisplay();
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updateThemeDisplay() {
        String currentThemeValue = sharedPreferences.getString("theme", "system");
        for (int i = 0; i < themeValues.length; i++) {
            if (themeValues[i].equals(currentThemeValue)) {
                themeValue.setText(themeEntries[i]);
                break;
            }
        }
    }

    private void applyTheme(String themeValue) {
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
