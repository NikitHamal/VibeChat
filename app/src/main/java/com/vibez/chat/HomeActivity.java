package com.vibez.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity {

    private EditText usernameEditText, ageEditText;
    private AutoCompleteTextView genderAutoCompleteTextView;
    private Button startChatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        usernameEditText = findViewById(R.id.username_edit_text);
        ageEditText = findViewById(R.id.age_edit_text);
        genderAutoCompleteTextView = findViewById(R.id.gender_auto_complete_text_view);
        startChatButton = findViewById(R.id.start_chat_button);

        String[] genders = getResources().getStringArray(R.array.genders);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        genderAutoCompleteTextView.setAdapter(adapter);


        startChatButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String age = ageEditText.getText().toString().trim();
            String gender = genderAutoCompleteTextView.getText().toString();

            if (username.isEmpty() || age.isEmpty() || gender.isEmpty()) {
                Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences sharedPreferences = getSharedPreferences("VibezPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username", username);
            editor.putString("age", age);
            editor.putString("gender", gender);
            editor.apply();

            Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
            startActivity(intent);
        });
    }
}