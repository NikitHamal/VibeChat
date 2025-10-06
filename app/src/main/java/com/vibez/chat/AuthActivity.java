package com.vibez.chat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class AuthActivity extends AppCompatActivity {

    private MaterialButton googleSignInButton;
    private MaterialButton guestContinueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        googleSignInButton = findViewById(R.id.google_sign_in_button);
        guestContinueButton = findViewById(R.id.guest_continue_button);

        googleSignInButton.setOnClickListener(v -> {
            // TODO: Implement Google Sign In
            Toast.makeText(this, "Google Sign In - Coming Soon", Toast.LENGTH_SHORT).show();
            navigateToHome();
        });

        guestContinueButton.setOnClickListener(v -> {
            navigateToHome();
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(AuthActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
