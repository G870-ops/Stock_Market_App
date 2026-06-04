package com.example.stockapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class HomeActivity extends AppCompatActivity {

    // Handlers for the futuristic effects
    private final Handler flickerHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // --- EXISTING BUTTON INITIALIZATION ---
        MaterialButton btnGoToMain = findViewById(R.id.btnGoToMain);
        MaterialButton btnGoToChat = findViewById(R.id.btnGoToChat);

        // --- NEW FEATURE: SYNC ICON ROTATION ---
        ImageView syncIcon = findViewById(R.id.syncIcon);
        if (syncIcon != null) {
            syncIcon.setOnClickListener(v -> {
                // Load the animation from the anim folder
                Animation rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_sync);
                // Start the animation on the ImageView (8 seconds as per your XML)
                syncIcon.startAnimation(rotateAnim);
                // Optional: Show a "Syncing" toast for that tech feel
                Toast.makeText(this, "RE-SYNCING DATA STREAMS...", Toast.LENGTH_SHORT).show();
            });
        }

        // --- NEW FEATURE: TITLE FLICKER ---
        TextView title = findViewById(R.id.homeTitle);
        if (title != null) {
            startFlickerAnimation(title);
        }

        // --- EXISTING NAVIGATION LOGIC ---
        if (btnGoToMain != null) {
            btnGoToMain.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("HomeActivity", "Error opening MainActivity: " + e.getMessage());
                    Toast.makeText(this, "Cannot open Market: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnGoToChat != null) {
            btnGoToChat.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(HomeActivity.this, AiChatActivity.class);
                    intent.putExtra("SENTIMENT", "Neutral");
                    intent.putExtra("PATTERN", "Scanning...");
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("HomeActivity", "Error opening AiChatActivity: " + e.getMessage());
                    Toast.makeText(this, "Cannot open AI Chat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Flicker logic helper
    private void startFlickerAnimation(TextView title) {
        Runnable flicker = new Runnable() {
            @Override
            public void run() {
                title.setAlpha(0.8f + (float) Math.random() * 0.2f);
                flickerHandler.postDelayed(this, 100);
            }
        };
        flickerHandler.post(flicker);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up the handler to prevent memory leaks
        flickerHandler.removeCallbacksAndMessages(null);
    }
}