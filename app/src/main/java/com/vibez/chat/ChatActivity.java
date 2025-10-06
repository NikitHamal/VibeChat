package com.vibez.chat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messages;
    private EditText messageEditText;
    private TextView strangerNameTextView, strangerDetailsTextView;
    private ImageView strangerFlag;
    private ChipGroup suggestionChipGroup;
    private View suggestionChipsScroll;
    private ConstraintLayout loadingOverlay;

    private String[] botNames = {"Aria", "Leo", "Mia", "Zoe", "Kai"};
    private String[] botGenders = {"female", "male", "female", "female", "male"};
    private int[] botAges = {22, 25, 21, 23, 24};
    private String[] botCountries = {"ca", "gb", "us", "au", "de"};
    private Random random = new Random();
    private boolean hasSentFirstMessage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme
        SharedPreferences sharedPreferences = getSharedPreferences("VibezPrefs", MODE_PRIVATE);
        int themeMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(themeMode);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        strangerNameTextView = findViewById(R.id.stranger_name);
        strangerDetailsTextView = findViewById(R.id.stranger_details);
        strangerFlag = findViewById(R.id.stranger_flag);

        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        messageEditText = findViewById(R.id.message_edit_text);
        suggestionChipGroup = findViewById(R.id.suggestion_chip_group);
        suggestionChipsScroll = findViewById(R.id.suggestion_chips_scroll);
        loadingOverlay = findViewById(R.id.loading_overlay);

        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        findViewById(R.id.send_button).setOnClickListener(v -> sendMessage());
        findViewById(R.id.next_button).setOnClickListener(v -> connectToNewStranger());
        findViewById(R.id.quit_button).setOnClickListener(v -> finish());

        setupSuggestionChips();
        connectToNewStranger();
    }

    private void connectToNewStranger() {
        loadingOverlay.setVisibility(View.VISIBLE);
        messages.clear();
        chatAdapter.notifyDataSetChanged();
        hasSentFirstMessage = false;

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            int botIndex = random.nextInt(botNames.length);
            String botName = botNames[botIndex];
            String botDetails = botGenders[botIndex] + ", " + botAges[botIndex];

            strangerNameTextView.setText(botName);
            strangerDetailsTextView.setText(botDetails);
            
            // Load flag (using a placeholder for now)
            // You can use a library like Glide or Picasso to load flag images
            // For now, we'll just set a background color
            strangerFlag.setBackgroundColor(getColor(R.color.md_theme_light_surfaceVariant));

            addMessage(new Message("You're now chatting with " + botName + ". Be nice!", false, true));
            loadingOverlay.setVisibility(View.GONE);
            suggestionChipsScroll.setVisibility(View.VISIBLE);
        }, 2000);
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (!messageText.isEmpty()) {
            addMessage(new Message(messageText, true, false));
            messageEditText.setText("");
            
            if (!hasSentFirstMessage) {
                suggestionChipsScroll.setVisibility(View.GONE);
                hasSentFirstMessage = true;
            }
            
            simulateBotResponse(messageText);
        }
    }

    private void sendMessage(String messageText) {
        if (!messageText.isEmpty()) {
            addMessage(new Message(messageText, true, false));
            
            if (!hasSentFirstMessage) {
                suggestionChipsScroll.setVisibility(View.GONE);
                hasSentFirstMessage = true;
            }
            
            simulateBotResponse(messageText);
        }
    }

    private void simulateBotResponse(String userMessage) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String response = getBotResponse(userMessage);
            addMessage(new Message(response, false, false));
        }, 1000);
    }

    private String getBotResponse(String userMessage) {
        String lowerCaseMessage = userMessage.toLowerCase();

        if (lowerCaseMessage.contains("how are you")) {
            return "I'm just a bot, but I'm doing great! Thanks for asking.";
        } else if (lowerCaseMessage.contains("asl")) {
            return "I'm a bot from the internet, so age and location don't really apply to me!";
        } else if (lowerCaseMessage.contains("your name")) {
            return "You can call me VibezBot!";
        } else if (lowerCaseMessage.contains("hello") || lowerCaseMessage.contains("hi") || lowerCaseMessage.contains("hey")) {
            String[] greetings = {"Hello there!", "Hi! What's on your mind?", "Hey! Nice to chat with you."};
            return greetings[random.nextInt(greetings.length)];
        } else if (lowerCaseMessage.contains("what's up") || lowerCaseMessage.contains("what are you doing")) {
            return "Just chatting with cool people like you!";
        } else if (lowerCaseMessage.contains("bye")) {
            return "It was nice talking to you! Bye!";
        }

        String[] genericResponses = {
            "That's interesting!",
            "Tell me more.",
            "I'm not sure I understand. Can you explain?",
            "Haha, that's funny!",
            "What do you think?",
            "Cool!",
            "I see."
        };
        return genericResponses[random.nextInt(genericResponses.length)];
    }

    private void addMessage(Message message) {
        messages.add(message);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        chatRecyclerView.scrollToPosition(messages.size() - 1);
    }

    private void setupSuggestionChips() {
        for (int i = 0; i < suggestionChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) suggestionChipGroup.getChildAt(i);
            chip.setOnClickListener(v -> {
                sendMessage(chip.getText().toString());
            });
        }
    }
}
