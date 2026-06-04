package com.example.stockapp;

import android.content.Intent;
import android.graphics.Color;
import android.content.res.Configuration; // Added Import
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull; // Added Import
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StockAdapter adapter;
    private ArrayList<StockModel> stockList;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    // --- NEW: Realtime Stock Update Handler ---
    private final Handler stockHandler = new Handler(Looper.getMainLooper());
    private final int UPDATE_INTERVAL = 2000; // 2 seconds

    private LineChart marketChart;
    private List<Entry> chartEntries = new ArrayList<>();
    private int currentScrollPosition = 0;

    // --- AI FEATURES FIELDS ---
    private String currentSentiment = "Neutral";
    private String detectedPattern = "Calculating...";

    // --- TRACKING FIELDS FOR CALCULATED METRICS ---
    private double initialMarketValue = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            TextView devName = findViewById(R.id.devSignature);
            if (devName != null) {
                Animation pulse = AnimationUtils.loadAnimation(this, R.anim.glow_pulse);
                devName.startAnimation(pulse);
            }

            setupUI();
            setupThemeSwitch();
            setupNewsTicker();
            setupTimeFilters();
            setupViewModeSelector();
            startAutoScroll();

            // --- START NEW REALTIME STOCK SIMULATION ---
            startMarketSimulation();

        } catch (Exception e) {
            Toast.makeText(this, "Startup Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupUI() {
        marketChart = findViewById(R.id.marketChart);
        if (marketChart != null) setupFuturisticChart();

        recyclerView = findViewById(R.id.stockRecyclerView);
        stockList = new ArrayList<>();
        initializeData();

        // Calculate baseline market value for priceDelta calculations
        for (StockModel stock : stockList) {
            initialMarketValue += stock.getPrice();
        }
    }

    private void setupTimeFilters() {
        LinearLayout timeSelectorRange = findViewById(R.id.timeSelectorRange);
        if (timeSelectorRange != null) {
            for (int i = 0; i < timeSelectorRange.getChildCount(); i++) {
                View child = timeSelectorRange.getChildAt(i);
                if (child instanceof TextView) {
                    TextView timeOption = (TextView) child;
                    timeOption.setOnClickListener(v -> {
                        String selection = timeOption.getText().toString();
                        Toast.makeText(MainActivity.this, "Interval Switched: " + selection, Toast.LENGTH_SHORT).show();

                        // 1. Reset all options to unselected style
                        for (int j = 0; j < timeSelectorRange.getChildCount(); j++) {
                            View unselectedChild = timeSelectorRange.getChildAt(j);
                            if (unselectedChild instanceof TextView) {
                                TextView unselectedOption = (TextView) unselectedChild;
                                unselectedOption.setTextColor(Color.parseColor("#64748B"));
                                unselectedOption.setBackgroundColor(Color.TRANSPARENT);
                            }
                        }

                        // 2. Set current clicked option to active styling
                        timeOption.setTextColor(Color.parseColor("#00D4FF"));
                        timeOption.setBackgroundColor(Color.parseColor("#1E293B"));

                        // 3. Optional: Clear old chart values to show context interval adjustment
                        if (marketChart != null) {
                            chartEntries.clear();
                            marketChart.invalidate();
                        }
                    });
                }
            }
        }
    }

    private void setupViewModeSelector() {
        TextView labelMarket = findViewById(R.id.labelMarket);
        if (labelMarket != null) {
            labelMarket.setOnClickListener(v -> {
                Toast.makeText(MainActivity.this, "Opening AI Market Chat...", Toast.LENGTH_SHORT).show();

                // The moment you type this inside this click listener, the Intent import becomes active!
                Intent intent = new Intent(MainActivity.this, AiChatActivity.class);
                startActivity(intent);
            });
        }
    }

    private void analyzeMarketSentiment() {
        String[] sentiments = {"Bullish", "Bearish", "Neutral", "Highly Volatile"};
        currentSentiment = sentiments[random.nextInt(sentiments.length)];
        TextView sentimentView = findViewById(R.id.sentimentScoreText);
        if (sentimentView != null) {
            sentimentView.setText("Market Sentiment: " + currentSentiment);
            if (currentSentiment.equals("Bullish")) {
                sentimentView.setTextColor(Color.parseColor("#00E676"));
            } else if (currentSentiment.equals("Bearish")) {
                sentimentView.setTextColor(Color.parseColor("#FF5252"));
            } else {
                sentimentView.setTextColor(Color.parseColor("#64748B"));
            }
        }
    }

    private void runPatternRecognition() {
        if (chartEntries.size() < 5) return;
        float last = chartEntries.get(chartEntries.size() - 1).getY();
        float prev = chartEntries.get(chartEntries.size() - 2).getY();
        float start = chartEntries.get(0).getY();

        if (last > prev && prev > start) detectedPattern = "Ascending Channel (Bullish)";
        else if (last < prev && prev < start) detectedPattern = "Descending Triangle (Bearish)";
        else detectedPattern = "Sideways Consolidation";
    }

    private void updateHardwareMetrics() {
        TextView hardwareMonitorText = findViewById(R.id.hardwareMonitorText);
        if (hardwareMonitorText != null) {
            int simulatedCpuLoad = 15 + random.nextInt(35); // Loops loads between 15%-50%
            hardwareMonitorText.setText("SYS_HEALTH // CPU_LOAD: " + simulatedCpuLoad + "% // MEM_OK");
        }
    }

    private void initializeData() {
        // --- High Profit / High Growth ---
        stockList.add(new StockModel("NVDA", "NVIDIA Corp.", 875.20, 4.2));
        stockList.add(new StockModel("BTC", "Bitcoin", 67000.50, 5.4));
        stockList.add(new StockModel("META", "Meta Platforms", 495.30, 3.1));
        stockList.add(new StockModel("AVGO", "Broadcom Inc.", 1350.10, 2.8));
        stockList.add(new StockModel("LLY", "Eli Lilly", 760.40, 2.5));

        // --- Medium Profit / Stable ---
        stockList.add(new StockModel("MSFT", "Microsoft", 420.10, 1.2));
        stockList.add(new StockModel("GOOGL", "Alphabet", 142.30, 0.9));
        stockList.add(new StockModel("AMZN", "Amazon", 178.50, 1.1));
        stockList.add(new StockModel("AAPL", "Apple Inc.", 189.10, 0.5));
        stockList.add(new StockModel("V", "Visa Inc.", 280.15, 0.8));

        // --- Low Profit / Flat ---
        stockList.add(new StockModel("WMT", "Walmart", 60.25, 0.1));
        stockList.add(new StockModel("KO", "Coca-Cola", 59.80, -0.2));
        stockList.add(new StockModel("JNJ", "Johnson & Johnson", 155.40, 0.2));
        stockList.add(new StockModel("PG", "Procter & Gamble", 160.10, -0.1));
        stockList.add(new StockModel("XOM", "Exxon Mobil", 115.30, 0.3));

        // --- Loss Making / High Risk ---
        stockList.add(new StockModel("TSLA", "Tesla Inc.", 175.20, -3.5));
        stockList.add(new StockModel("RIVN", "Rivian Automotive", 10.45, -5.2));
        stockList.add(new StockModel("PYPL", "PayPal Holdings", 62.10, -2.1));
        stockList.add(new StockModel("BA", "Boeing Co.", 180.50, -4.8));
        stockList.add(new StockModel("PTON", "Peloton Interactive", 4.20, -12.4));

        // --- 1. HIGH PROFIT / MARKET LEADERS (The "Crown Jewels") ---
        stockList.add(new StockModel("RELIANCE", "Reliance Industries", 2985.50, 1.8));
        stockList.add(new StockModel("TCS", "Tata Consultancy Services", 4120.00, 1.2));
        stockList.add(new StockModel("SAUDIARAMCO", "Saudi Aramco", 32.40, 0.5));
        stockList.add(new StockModel("HDFCBANK", "HDFC Bank", 1680.15, 0.9));

        adapter = new StockAdapter(stockList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    // 🔄 NEW: Realtime Market Simulation
    private void startMarketSimulation() {
        stockHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateStockPricesRealtime();
                analyzeMarketSentiment();
                runPatternRecognition();
                updateHardwareMetrics();
                stockHandler.postDelayed(this, UPDATE_INTERVAL);
            }
        }, UPDATE_INTERVAL);
    }

    // 🔄 NEW: Update stock prices dynamically
    private void updateStockPricesRealtime() {
        for (int i = 0; i < stockList.size(); i++) {
            StockModel stock = stockList.get(i);
            double oldPrice = stock.getPrice();
            double changePercent = (random.nextDouble() * 3.0) - 1.5;
            double newPrice = oldPrice * (1 + changePercent / 100);
            stock.setPrice(newPrice);
            stock.setChange(newPrice - oldPrice);
            adapter.notifyItemChanged(i);
        }
        updateTotalBalanceAndChart();
    }

    private void updateTotalBalanceAndChart() {
        double totalMarketValue = 0;
        for (StockModel stock : stockList) totalMarketValue += stock.getPrice();

        TextView totalBalance = findViewById(R.id.totalBalance);
        if (totalBalance != null) totalBalance.setText(String.format("$%.2f", totalMarketValue));

        // Dynamically compute and colorize delta change metrics
        TextView priceDeltaText = findViewById(R.id.priceDeltaText);
        if (priceDeltaText != null && initialMarketValue > 0) {
            double netDeltaValue = totalMarketValue - initialMarketValue;
            double percentageDelta = (netDeltaValue / initialMarketValue) * 100;

            String sign = netDeltaValue >= 0 ? "+" : "";
            priceDeltaText.setText(String.format("%s%.2f [%s%.2f%%]", sign, netDeltaValue, sign, percentageDelta));
            priceDeltaText.setTextColor(netDeltaValue >= 0 ? Color.parseColor("#00E676") : Color.parseColor("#FF5252"));
        }

        if (marketChart != null && !stockList.isEmpty())
            updateChart(totalMarketValue / stockList.size());
    }

    private void startAutoScroll() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter != null && !stockList.isEmpty()) {
                    currentScrollPosition = (currentScrollPosition + 1) % adapter.getItemCount();
                    RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(MainActivity.this) {
                        @Override protected int getVerticalSnapPreference() {
                            return LinearSmoothScroller.SNAP_TO_START;
                        }
                    };
                    smoothScroller.setTargetPosition(currentScrollPosition);
                    if (recyclerView.getLayoutManager() != null)
                        recyclerView.getLayoutManager().startSmoothScroll(smoothScroller);
                }
                handler.postDelayed(this, 3000);
            }
        }, 3000);
    }

    private void setupThemeSwitch() {
        MaterialSwitch themeSwitch = findViewById(R.id.themeSwitch);
        if (themeSwitch != null) {
            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            themeSwitch.setChecked(currentNightMode == Configuration.UI_MODE_NIGHT_YES);

            themeSwitch.setOnCheckedChangeListener((v, isChecked) -> {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (isChecked) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                }, 150);
            });
        }
    }

    private void setupNewsTicker() {
        TextView newsTicker = findViewById(R.id.newsTicker);
        if (newsTicker != null) newsTicker.setSelected(true);
    }

    private void setupFuturisticChart() {
        marketChart.getAxisLeft().setTextColor(Color.CYAN);
        marketChart.getAxisRight().setEnabled(false);
        marketChart.getXAxis().setEnabled(false);
        marketChart.getLegend().setEnabled(false);
        marketChart.getDescription().setEnabled(false);
        marketChart.setTouchEnabled(false);
    }

    private void updateChart(double avgPrice) {
        chartEntries.add(new Entry(chartEntries.size(), (float) avgPrice));
        if (chartEntries.size() > 20) chartEntries.remove(0);

        LineDataSet dataSet = new LineDataSet(chartEntries, "Market");
        dataSet.setColor(Color.parseColor("#00D4FF"));
        dataSet.setDrawCircles(false);
        dataSet.setLineWidth(2f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        marketChart.setData(new LineData(dataSet));
        marketChart.invalidate();
    }

    // --- EXACT POSITION OF THE REPAIRED OVERRIDE METHOD ---
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int currentNightMode = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            Toast.makeText(this, "Dark Mode Active", Toast.LENGTH_SHORT).show();
        } else if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
            Toast.makeText(this, "Light Mode Active", Toast.LENGTH_SHORT).show();
        }

        if (recyclerView != null && adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
    private void initializeNetworkClient() {
        // ⚡ THIS ACTIVATES: import retrofit2.Retrofit;
        // ⚡ AND ACTIVATES: import retrofit2.converter.gson.GsonConverterFactory;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.example.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        stockHandler.removeCallbacksAndMessages(null);
    }
}