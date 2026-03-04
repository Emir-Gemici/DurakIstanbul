package com.example.emirgemici;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class ModeSelectionActivity extends AppCompatActivity {
    private String selectedMode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_selection);

        CardView cardRandom = findViewById(R.id.card_random);
        CardView cardFull = findViewById(R.id.card_full);
        final TextView descRandom = findViewById(R.id.tv_random_desc);
        final TextView descFull = findViewById(R.id.tv_full_desc);

        Button btnStart = findViewById(R.id.btn_start);
        Button btnCerts = findViewById(R.id.btn_certificates);
        Button btnLeader = findViewById(R.id.btn_leaderboard);

        cardRandom.setOnClickListener(v -> {
            selectedMode = "RANDOM";
            descRandom.setVisibility(View.VISIBLE);
            descFull.setVisibility(View.GONE);
            cardRandom.setCardBackgroundColor(android.graphics.Color.parseColor("#443366"));
            cardFull.setCardBackgroundColor(android.graphics.Color.parseColor("#CC1E1E1E"));
        });

        cardFull.setOnClickListener(v -> {
            selectedMode = "FULL";
            descFull.setVisibility(View.VISIBLE);
            descRandom.setVisibility(View.GONE);
            cardFull.setCardBackgroundColor(android.graphics.Color.parseColor("#334444"));
            cardRandom.setCardBackgroundColor(android.graphics.Color.parseColor("#CC1E1E1E"));
        });

        // 🚀 OYUNA GİDİŞ: DistrictActivity'ye "GAME" amacını yollar
        btnStart.setOnClickListener(v -> {
            if (!selectedMode.isEmpty()) {
                Intent intent = new Intent(ModeSelectionActivity.this, DistrictActivity.class);
                intent.putExtra("GAME_MODE", selectedMode);
                intent.putExtra("PURPOSE", "GAME");
                startActivity(intent);
            } else {
                Toast.makeText(this, "Lütfen bir mod seçin!", Toast.LENGTH_SHORT).show();
            }
        });

        // 🚀 SIRALAMAYA GİDİŞ: DistrictActivity'ye "LEADERBOARD" amacını yollar
        btnLeader.setOnClickListener(v -> {
            Intent intent = new Intent(ModeSelectionActivity.this, DistrictActivity.class);
            intent.putExtra("PURPOSE", "LEADERBOARD");
            startActivity(intent);
        });

        // 🚀 SERTİFİKALAR
        btnCerts.setOnClickListener(v -> {
            Intent intent = new Intent(ModeSelectionActivity.this, CertificatesActivity.class);
            startActivity(intent);
        });
    }
}