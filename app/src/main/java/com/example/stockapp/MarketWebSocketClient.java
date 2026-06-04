package com.example.stockapp;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashSet;
import java.util.Set;

public class MarketWebSocketClient {
    private static final String TAG = "MarketWSClient";

    // Core structural endpoint for your live data stream
    private static final String BASE_URL = "wss://api.neo-exchange.com/v1/market-data";

    private final OkHttpClient client;
    private WebSocket webSocket;
    private final Set<String> subscribedSymbols = new HashSet<>();
    private MarketWebSocketListener listener;
    private boolean isConnected = false;

    // 🟢 FIXED: Keeps your original unified callback contract layout name for MainActivity compatibility
    public interface MarketWebSocketListener {
        void onPriceTick(String symbol, double price, long timestamp);
    }

    public MarketWebSocketClient() {
        this.client = new OkHttpClient();
    }

    public void setListener(MarketWebSocketListener listener) {
        this.listener = listener;
    }

    public void connect() {
        if (isConnected) return;

        Log.d(TAG, "Initiating secure connection handshake via OkHttp...");

        // 🟢 AUTHENTICATION INTEGRATION: Injecting tracking credentials securely into request header matrix
        Request request = new Request.Builder()
                .url(BASE_URL)
                .addHeader("Authorization", "Bearer NEO_SECURE_AUTH_TOKEN_XYZ")
                .addHeader("X-API-KEY", "GOUTAM_GUPTA_DEV_KEY_2026")
                .addHeader("Content-Type", "application/json")
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                isConnected = true;
                Log.i(TAG, "Gateway Secure Connection Established. Handshake Authenticated.");

                // Automatically re-register watch elements on reconnection events
                for (String symbol : subscribedSymbols) {
                    sendSubscriptionMessage(symbol);
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                parseAndEmitTick(text);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket Channel Closing: " + reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                isConnected = false;
                Log.w(TAG, "WebSocket Channel Offline");
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                isConnected = false;
                Log.e(TAG, "WebSocket Core Failure / Handshake Reject: " + t.getMessage());
            }
        });
    }

    public void subscribe(String symbol) {
        subscribedSymbols.add(symbol.toUpperCase());
        if (isConnected && webSocket != null) {
            sendSubscriptionMessage(symbol);
        }
    }

    public void unsubscribe(String symbol) {
        subscribedSymbols.remove(symbol.toUpperCase());
        if (isConnected && webSocket != null) {
            webSocket.send("{\"action\":\"unsubscribe\",\"symbol\":\"" + symbol.toUpperCase() + "\"}");
        }
    }

    private void sendSubscriptionMessage(String symbol) {
        // Formats payload text parameters uniformly matching standard network messaging schemas
        String subPayload = String.format("{\"action\":\"subscribe\",\"symbol\":\"%s\"}", symbol.toUpperCase());
        webSocket.send(subPayload);
    }

    private void parseAndEmitTick(String jsonPayload) {
        try {
            JSONObject root = new JSONObject(jsonPayload);

            // Handles explicit multi-trade array streams cleanly
            if (root.has("type") && "trade".equals(root.getString("type"))) {
                JSONArray dataArray = root.getJSONArray("data");
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject trade = dataArray.getJSONObject(i);
                    String symbol = trade.getString("s");
                    double price = trade.getDouble("p");
                    long timestamp = trade.getLong("t");

                    if (listener != null) {
                        listener.onPriceTick(symbol, price, timestamp);
                    }
                }
            }
            // Fallback object structural parsing block matching direct single-tick signatures
            else if (root.has("symbol") && root.has("price")) {
                String symbol = root.getString("symbol");
                double price = root.getDouble("price");
                long timestamp = root.optLong("timestamp", System.currentTimeMillis());

                if (listener != null) {
                    listener.onPriceTick(symbol, price, timestamp);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "JSON Stream Parse Exception: " + e.getMessage());
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "App standard closure");
        }
        isConnected = false;
    }
}