package com.vibez.chat;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String[] THEME_ENTRIES = {"Light", "Dark", "System Default"};
    private static final String[] THEME_VALUES = {"light", "dark", "system"};
    private int selectedThemeIndex = 2; // Default to system

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        Preference themePreference = findPreference("theme");
        if (themePreference != null) {
            // Load current theme preference
            SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
            String currentTheme = prefs.getString("theme", "system");
            updateThemeSummary(currentTheme);

            themePreference.setOnPreferenceClickListener(preference -> {
                showThemeSelectionDialog();
                return true;
            });
        }
    }

    private void showThemeSelectionDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Choose Theme")
                .setSingleChoiceItems(THEME_ENTRIES, selectedThemeIndex, (dialog, which) -> {
                    selectedThemeIndex = which;
                    String themeValue = THEME_VALUES[which];
                    applyTheme(themeValue);
                    updateThemeSummary(themeValue);

                    // Save preference
                    SharedPreferences.Editor editor = getPreferenceManager().getSharedPreferences().edit();
                    editor.putString("theme", themeValue);
                    editor.apply();

                    // Recreate the activity to apply theme changes immediately
                    requireActivity().recreate();

                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
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

    private void updateThemeSummary(String themeValue) {
        Preference themePreference = findPreference("theme");
        if (themePreference != null) {
            for (int i = 0; i < THEME_VALUES.length; i++) {
                if (THEME_VALUES[i].equals(themeValue)) {
                    themePreference.setSummary(THEME_ENTRIES[i]);
                    break;
                }
            }
        }
    }
}