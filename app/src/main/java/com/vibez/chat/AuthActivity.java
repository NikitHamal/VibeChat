package com.vibez.chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class AuthActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "VibeZPrefs";
    private static final String KEY_AUTH_MODE = "auth_mode";

    private MaterialButton googleSignInButton;
    private MaterialButton guestContinueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        googleSignInButton = findViewById(R.id.google_sign_in_button);
        guestContinueButton = findViewById(R.id.guest_continue_button);

        googleSignInButton.setOnClickListener(v -> {
            // Save auth mode as Google
            saveAuthMode("google");
            // TODO: Implement Google Sign In
            Toast.makeText(this, "Google Sign In - Coming Soon", Toast.LENGTH_SHORT).show();
            navigateToHome();
        });

        guestContinueButton.setOnClickListener(v -> {
            // Save auth mode as Guest
            saveAuthMode("guest");
            navigateToHome();
        });
    }

    private void saveAuthMode(String mode) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_AUTH_MODE, mode).apply();
    }

    private void navigateToHome() {
        Intent intent = new Intent(AuthActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
