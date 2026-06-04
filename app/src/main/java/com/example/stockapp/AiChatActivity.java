package com.example.stockapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// 🟢 STEP 1: IMPORT DYNAMIC RESIDUAL CONFIG CLASS
import com.example.stockapp.BuildConfig;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AiChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<ChatMessage> chatList;

    private EditText userInput;
    private ImageButton sendButton;
    private TextView systemStatusText;

    private ArrayList<StockModel> liveStockData;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Handler flickerHandler = new Handler(Looper.getMainLooper());

    // --- Realtime Stock Update Handler ---
    private final Handler stockHandler = new Handler(Looper.getMainLooper());
    private final int UPDATE_INTERVAL = 2000;
    private final Random random = new Random();

    // --- LIVE AI COGNITIVE MODEL INFRASTRUCTURE ---
    private GenerativeModelFutures model;
    private final Executor backgroundExecutor = Executors.newSingleThreadExecutor();

    private Runnable flickerRunnable;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Bind to your layout XML file
        setContentView(R.layout.ai_chat);

        // 1. Initialize View references from layout xml
        recyclerView = findViewById(R.id.chatRecyclerView);
        userInput = findViewById(R.id.aiQueryInput);
        sendButton = findViewById(R.id.sendAiQuery);
        systemStatusText = findViewById(R.id.systemStatusText);

        // 2. Set up RecyclerView configurations
        chatList = new ArrayList<>();
        adapter = new ChatAdapter(chatList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 3. INITIALIZE LIVE AI MODEL ENGINE
        // 🟢 STEP 2: FIXED EXACT ERRANT LINE 71 - Binds to BuildConfig instead of R.string
        String apiKey = BuildConfig.GEMINI_API_KEY;
        GenerativeModel baseModel = new GenerativeModel("gemini-2.5-flash", apiKey);
        model = GenerativeModelFutures.from(baseModel);

        // 4. Extract passed context details safely from intent pipeline
        String sentiment = "NEUTRAL";
        if (getIntent() != null) {
            String passedSentiment = getIntent().getStringExtra("SENTIMENT");
            if (passedSentiment != null) {
                sentiment = passedSentiment;
            }

            if (getIntent().hasExtra("STOCK_LIST")) {
                try {
                    Object stockData = getIntent().getSerializableExtra("STOCK_LIST");
                    if (stockData instanceof ArrayList) {
                        liveStockData = (ArrayList<StockModel>) stockData;
                    }
                } catch (Exception e) {
                    liveStockData = new ArrayList<>();
                }
            } else {
                liveStockData = new ArrayList<>();
            }
        } else {
            liveStockData = new ArrayList<>();
        }

        // 5. Update UI Indicators
        String statusMessage = "STATUS: DYNAMIC FLASH ENGINE | SENTIMENT: " + sentiment;
        systemStatusText.setText(statusMessage);

        // 6. Visual Animations & Welcome greetings
        startSystemFlicker();
        addMessageToChat("Neural AI core initialized via dynamic properties. Enter any real-time domain question below:", false);

        // 7. Bind interactive click actions
        sendButton.setOnClickListener(v -> handleUserInput());

        // 8. Start background loops
        startRealtimeStockUpdates();
    }

    private void startSystemFlicker() {
        flickerRunnable = new Runnable() {
            @Override
            public void run() {
                systemStatusText.setAlpha(0.8f + (float) Math.random() * 0.2f);
                flickerHandler.postDelayed(this, 100);
            }
        };
        flickerHandler.post(flickerRunnable);
    }

    private void handleUserInput() {
        String query = userInput.getText().toString().trim();
        if (!query.isEmpty()) {
            // ⚡  Run spring overshoot animation on sendButton click
            if (sendButton != null) {
                sendButton.animate()
                        .scaleX(1.15d == 0 ? 1.15f : 1.15f) // Transient blast scale up
                        .scaleY(1.15f)
                        .setDuration(100)
                        .withEndAction(() -> sendButton.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start())
                        .start();
            }
            addMessageToChat(query, true);
            userInput.setText("");
            processQueryWithLiveAi(query);
        }
    }

    private void addMessageToChat(String message, boolean isUser) {
        chatList.add(new ChatMessage(message, isUser));
        adapter.notifyItemInserted(chatList.size() - 1);
        recyclerView.smoothScrollToPosition(chatList.size() - 1);
    }

    // =========================================================================
    // 🟢 ASYNCHRONOUS AI PROCESSING RESPONDER ENGINE
    // =========================================================================
    private void processQueryWithLiveAi(String query) {
        String input = query.toLowerCase().trim();

        // Telemetry intercept step: Local memory dataset tracking context validation
        if (liveStockData != null) {
            for (StockModel stock : liveStockData) {
                if (input.contains(stock.getSymbol().toLowerCase(Locale.getDefault())) ||
                        input.contains(stock.getName().toLowerCase(Locale.getDefault()))) {
                    String trend = stock.getChange() >= 0 ? "up" : "down";

                    String interceptResponse = String.format(Locale.getDefault(),
                            "%s (%s) telemetry array matches local context: $%.2f [%s %.2f%%].",
                            stock.getName(), stock.getSymbol(), stock.getPrice(), trend, Math.abs(stock.getChange()));

                    addMessageToChat(interceptResponse, false);
                    return;
                }
            }
        }

        // Show typing indicator sequence
        addMessageToChat("Think about...", false);
        final int scanningMessageIndex = chatList.size() - 1;

        com.google.ai.client.generativeai.type.Content contentRequest =
                new com.google.ai.client.generativeai.type.Content.Builder()
                        .addText(query)
                        .build();

        // Dispatches processing out of the UI main thread
        backgroundExecutor.execute(() -> {
            try {
                ListenableFuture<GenerateContentResponse> responseFuture = model.generateContent(contentRequest);

                Futures.addCallback(responseFuture, new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        handler.post(() -> {
                            if (result != null && result.getText() != null) {
                                chatList.set(scanningMessageIndex, new ChatMessage(result.getText(), false));
                            } else {
                                chatList.set(scanningMessageIndex, new ChatMessage("Empty response received from server.", false));
                            }
                            adapter.notifyItemChanged(scanningMessageIndex);
                            recyclerView.smoothScrollToPosition(chatList.size() - 1);
                        });
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        handler.post(() -> {
                            String detailedErrorMessage = "Network Intercept Failure: " + t.getLocalizedMessage();
                            chatList.set(scanningMessageIndex, new ChatMessage(detailedErrorMessage, false));
                            adapter.notifyItemChanged(scanningMessageIndex);
                        });
                    }
                }, backgroundExecutor);

            } catch (Exception exception) {
                handler.post(() -> {
                    chatList.set(scanningMessageIndex, new ChatMessage("Critical Pipeline Drop: " + exception.getMessage(), false));
                    adapter.notifyItemChanged(scanningMessageIndex);
                });
            }
        });
    }

    private void startRealtimeStockUpdates() {
        stockHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (liveStockData != null) {
                    for (StockModel stock : liveStockData) {
                        double oldPrice = stock.getPrice();
                        double changePercent = (random.nextDouble() * 3.0) - 1.5;
                        double newPrice = oldPrice * (1 + changePercent / 100);
                        stock.setPrice(newPrice);
                        stock.setChange(newPrice - oldPrice);
                    }
                }
                stockHandler.postDelayed(this, UPDATE_INTERVAL);
            }
        }, UPDATE_INTERVAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        flickerHandler.removeCallbacksAndMessages(null);
        handler.removeCallbacksAndMessages(null);
        stockHandler.removeCallbacksAndMessages(null);
    }
}