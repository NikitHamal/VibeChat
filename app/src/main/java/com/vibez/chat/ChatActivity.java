package com.vibez.chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messages;
    private EditText messageEditText;
    private MaterialButton sendButton, nextButton;
    private TextView strangerNameTextView, strangerDetailsTextView, strangerFlagTextView;
    private LinearLayout suggestionChipContainer;
    private FrameLayout loadingOverlay;
    private MaterialToolbar toolbar;

    private final String[] botNames = {"Aria", "Leo", "Mia", "Zoe", "Kai"};
    private final String[] botGenders = {"female", "male", "female", "female", "male"};
    private final int[] botAges = {22, 25, 21, 23, 24};
    private final String[] botFlags = {"🇨🇦", "🇺🇸", "🇬🇧", "🇦🇺", "🇮🇳", "🇯🇵", "🇩🇪"};
    private final String[] suggestionChips = {"Hi!", "Hey", "Hello", "ASL?", "What's up?"};
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        strangerNameTextView = findViewById(R.id.stranger_name);
        strangerDetailsTextView = findViewById(R.id.stranger_details);
        strangerFlagTextView = findViewById(R.id.stranger_flag);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);
        nextButton = findViewById(R.id.next_button);
        suggestionChipContainer = findViewById(R.id.suggestion_chip_container);
        loadingOverlay = findViewById(R.id.loading_overlay);

        // Setup Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Show "You left the chat" message and exit after a delay
                addMessage(new Message("You left the chat.", Message.TYPE_SYSTEM));
                messageEditText.setEnabled(false);
                sendButton.setEnabled(false);
                nextButton.setEnabled(false);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    finish();
                }, 900);
            }
        });
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Setup RecyclerView
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        // Set listeners
        sendButton.setOnClickListener(v -> sendMessage());
        nextButton.setOnClickListener(v -> connectToNewStranger());
        setupSuggestionChips();

        // Start chat
        connectToNewStranger();
    }

    private void connectToNewStranger() {
        loadingOverlay.setVisibility(View.VISIBLE);
        suggestionChipContainer.setVisibility(View.VISIBLE); // Show suggestions for new chat
        if (messages != null) {
            messages.clear();
            chatAdapter.notifyDataSetChanged();
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            int botIndex = random.nextInt(botNames.length);
            String botName = botNames[botIndex];
            String botDetails = botGenders[botIndex] + ", " + botAges[botIndex];
            String botFlag = botFlags[random.nextInt(botFlags.length)];

            strangerNameTextView.setText(botName);
            strangerDetailsTextView.setText(botDetails);
            strangerFlagTextView.setText(botFlag);

            addMessage(new Message("You're now chatting with " + botName + ". Be nice!", Message.TYPE_SYSTEM));
            loadingOverlay.setVisibility(View.GONE);
        }, 2000); // Simulate network delay
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (!messageText.isEmpty()) {
            addMessage(new Message(messageText, Message.TYPE_SENT));
            messageEditText.setText("");
            simulateBotResponse(messageText);
        }
    }

    private void sendMessageFromChip(String messageText) {
        if (!messageText.isEmpty()) {
            addMessage(new Message(messageText, Message.TYPE_SENT));
            simulateBotResponse(messageText);
            suggestionChipContainer.setVisibility(View.GONE); // Hide suggestions after use
        }
    }

    private void simulateBotResponse(String userMessage) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String response = getBotResponse(userMessage);
            addMessage(new Message(response, Message.TYPE_RECEIVED));
        }, 1000);
    }

    private String getBotResponse(String userMessage) {
        String lowerCaseMessage = userMessage.toLowerCase();

        if (lowerCaseMessage.contains("how are you")) {
            return "I'm just a bot, but I'm doing great! Thanks for asking.";
        } else if (lowerCaseMessage.contains("asl")) {
            return "I'm a bot from the internet, so age and location don't really apply to me!";
        } else if (lowerCaseMessage.contains("your name")) {
            return "You can call me VibeZBot!";
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
        suggestionChipContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (String suggestion : suggestionChips) {
            TextView chip = (TextView) inflater.inflate(R.layout.item_suggestion_chip, suggestionChipContainer, false);
            chip.setText(suggestion);
            chip.setOnClickListener(v -> sendMessageFromChip(chip.getText().toString()));
            suggestionChipContainer.addView(chip);
        }
    }
}