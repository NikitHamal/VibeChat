package com.vibez.chat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class CrashActivity extends AppCompatActivity {

    public static final String EXTRA_CRASH_LOG = "extra_crash_log";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);

        TextView crashLogTextView = findViewById(R.id.crash_log_text_view);
        MaterialButton copyButton = findViewById(R.id.copy_button);
        MaterialButton restartButton = findViewById(R.id.restart_button);

        String crashLog = getIntent().getStringExtra(EXTRA_CRASH_LOG);
        crashLogTextView.setText(crashLog);

        copyButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Crash Log", crashLog);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Log copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        restartButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}