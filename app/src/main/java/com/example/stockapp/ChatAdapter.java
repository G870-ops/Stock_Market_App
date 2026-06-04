package com.example.stockapp;

import android.graphics.Color; // CRITICAL IMPORT
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> chatList;

    public ChatAdapter(List<ChatMessage> chatList) {
        this.chatList = chatList;
    }

    @Override
    public int getItemViewType(int position) {
        return chatList.get(position).isUser ? 1 : 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_ai, parent, false);
            return new AiViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = chatList.get(position);

        // --- USER MESSAGE BINDING ---
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).text.setText(msg.message);
        }

        // --- AI MESSAGE BINDING (ADVANCED) ---
        else if (holder instanceof AiViewHolder) {
            AiViewHolder aiHolder = (AiViewHolder) holder;
            aiHolder.text.setText(msg.message);

            // ELITE FEATURE: Dynamic Glow & Trend Bar based on Stock Performance
            if (msg.relatedStock != null) {
                // Determine color: Green for Profit/Up, Red for Loss/Down
                int color = msg.relatedStock.getChange() >= 0 ?
                        Color.parseColor("#00E676") : Color.parseColor("#FF5252");

                // Update the miniTrendBar visibility and color
                View trendBar = aiHolder.itemView.findViewById(R.id.miniTrendBar);
                if (trendBar != null) {
                    trendBar.setVisibility(View.VISIBLE);
                    trendBar.setBackgroundColor(color);
                }

                // Make the AI text glow in the trend color
                aiHolder.text.setTextColor(color);

            } else {
                // Default Cyan for non-stock related AI messages
                aiHolder.text.setTextColor(Color.parseColor("#00D4FF"));

                View trendBar = aiHolder.itemView.findViewById(R.id.miniTrendBar);
                if (trendBar != null) {
                    trendBar.setVisibility(View.GONE); // Hide if not a stock message
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        UserViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.userMessage);
        }
    }

    static class AiViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        AiViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.botMessageText);
        }
    }
}