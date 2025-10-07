package com.vibez.chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsFragment extends PreferenceFragmentCompat {

    private SharedPreferences sharedPreferences;
    private Preference themePreference;

    private CharSequence[] themeEntries;
    private CharSequence[] themeValues;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        themePreference = findPreference("theme");

        themeEntries = getResources().getTextArray(R.array.theme_entries);
        themeValues = getResources().getTextArray(R.array.theme_values);

        updateThemeSummary();

        if (themePreference != null) {
            themePreference.setOnPreferenceClickListener(preference -> {
                showThemeDialog();
                return true;
            });
        }
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

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Choose theme")
                .setSingleChoiceItems(themeEntries, checkedItem, (dialog, which) -> {
                    String selectedTheme = (String) themeValues[which];
                    sharedPreferences.edit().putString("theme", selectedTheme).apply();
                    applyTheme(selectedTheme);
                    updateThemeSummary();
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updateThemeSummary() {
        if (themePreference != null) {
            String currentThemeValue = sharedPreferences.getString("theme", "system");
            for (int i = 0; i < themeValues.length; i++) {
                if (themeValues[i].equals(currentThemeValue)) {
                    themePreference.setSummary(themeEntries[i]);
                    break;
                }
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