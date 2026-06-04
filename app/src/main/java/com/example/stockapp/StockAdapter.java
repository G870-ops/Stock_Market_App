package com.example.stockapp;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.ViewHolder> {

    private final ArrayList<StockModel> stockList;

    public StockAdapter(ArrayList<StockModel> stockList) {
        this.stockList = stockList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StockModel stock = stockList.get(position);

        // --- TEXT DATA ---
        holder.symbol.setText(stock.getSymbol());
        holder.name.setText(stock.getName());
        holder.price.setText(String.format("$%.2f", stock.getPrice()));

        // --- CHANGE LOGIC ---
        double change = stock.getChange();
        String sign = change >= 0 ? "+" : "";
        holder.change.setText(String.format("%s%.2f%%", sign, change));

        // --- NEON COLOR CODING ---
        if (change >= 0) holder.change.setTextColor(Color.parseColor("#00FF9C")); // Neon Green
        else holder.change.setTextColor(Color.parseColor("#FF3131")); // Neon Red

        // --- TREND BAR FEATURE WITH SMOOTH PULSE + DYNAMIC GLOW ---
        if (holder.trendBar != null) {
            holder.trendBar.setVisibility(View.VISIBLE);

            // Base and target colors
            int startColor = change >= 0 ? Color.parseColor("#00FF9C") : Color.parseColor("#FF5252");
            int endColor = change >= 0 ? Color.parseColor("#00E676") : Color.parseColor("#FF3131");

            // Gradient drawable for neon effect
            GradientDrawable gradientDrawable = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{startColor, endColor, startColor}
            );
            gradientDrawable.setCornerRadius(12f);
            holder.trendBar.setBackground(gradientDrawable);

            // Neon glow shadow
            holder.trendBar.setElevation(8f);
            holder.trendBar.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 12f);
                }
            });
            holder.trendBar.setClipToOutline(false);

            // Width calculation based on % change
            int maxWidth = holder.itemView.getWidth();
            float targetWidth = (float) Math.min(Math.abs(change) / 5.0, 1.0) * maxWidth;

            // Unified pulse animator
            ValueAnimator pulseAnimator = ValueAnimator.ofFloat(0f, 1f);
            pulseAnimator.setDuration(1000 + (long) (Math.abs(change) * 50));
            pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
            pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);

            pulseAnimator.addUpdateListener(animation -> {
                float fraction = (float) animation.getAnimatedValue();

                // Animate width
                ViewGroup.LayoutParams params = holder.trendBar.getLayoutParams();
                params.width = (int) (targetWidth * fraction);
                holder.trendBar.setLayoutParams(params);

                // Animate gradient color
                int color = (int) new ArgbEvaluator().evaluate(fraction, startColor, endColor);
                gradientDrawable.setColors(new int[]{color, endColor, color});

                // Animate dynamic glow intensity by changing elevation
                float glowElevation = 8f + 8f * fraction; // Base 8f, max 16f
                holder.trendBar.setElevation(glowElevation);
            });
            pulseAnimator.start();
        }
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView symbol, name, price, change;
        View trendBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            symbol = itemView.findViewById(R.id.stockSymbol);
            name = itemView.findViewById(R.id.stockName);
            price = itemView.findViewById(R.id.stockPrice);
            change = itemView.findViewById(R.id.stockChange);
            trendBar = itemView.findViewById(R.id.miniTrendBar);
        }
    }
}