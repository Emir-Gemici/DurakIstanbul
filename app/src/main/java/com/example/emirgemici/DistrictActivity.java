package com.example.emirgemici;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class DistrictActivity extends AppCompatActivity {

    private List<District> districtList;
    private LinearLayout container;
    private String gameMode;
    private String purpose; // 🚀 Neden buradayız? (Oyun mu, Sıralama mı?)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_district);

        gameMode = getIntent().getStringExtra("GAME_MODE");
        purpose = getIntent().getStringExtra("PURPOSE"); // ModeSelection'dan gelir

        container = findViewById(R.id.district_container);

        initDistrictData();
        createCheckBoxes();
        setupFilterButtons();

        Button btnDevam = findViewById(R.id.btn_devam);
        Button btnBack = findViewById(R.id.btn_back);

        // 🔙 GERİ BUTONU EKLENDİ
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 🚀 Ekrana geliş amacımıza göre Buton yazısını değiştiriyoruz
        if ("LEADERBOARD".equals(purpose)) {
            btnDevam.setText("Sıralamayı Gör");
        } else if ("RANDOM".equals(gameMode)) {
            btnDevam.setText("Soru Sayısı Seç");
        } else {
            btnDevam.setText("Oyuna Başla");
        }

        btnDevam.setOnClickListener(v -> {
            ArrayList<String> selectedDistricts = new ArrayList<>();
            for (District d : districtList) {
                if (d.isSelected()) selectedDistricts.add(d.getName());
            }

            if (selectedDistricts.isEmpty()) {
                Toast.makeText(this, "Lütfen en az bir ilçe seçin!", Toast.LENGTH_SHORT).show();
            } else {
                // 🚀 KARAR ANI: Oyun mu başlıyor, Liderlik Tablosu mu açılıyor?
                if ("LEADERBOARD".equals(purpose)) {
                    Intent intent = new Intent(DistrictActivity.this, LeaderboardActivity.class);
                    intent.putStringArrayListExtra("SELECTED_DISTRICTS", selectedDistricts);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(DistrictActivity.this, GameActivity.class);
                    intent.putExtra("GAME_MODE", gameMode);
                    intent.putStringArrayListExtra("SELECTED_DISTRICTS", selectedDistricts);
                    startActivity(intent);
                }
            }
        });
    }

    private void initDistrictData() {
        districtList = new ArrayList<>();
        String[] anadolu = {"Adalar", "Ataşehir", "Beykoz", "Çekmeköy", "Kadıköy", "Kartal", "Maltepe", "Pendik", "Sancaktepe", "Sultanbeyli", "Şile", "Tuzla", "Ümraniye", "Üsküdar"};
        for (String name : anadolu) districtList.add(new District(name, "Anadolu"));

        String[] avrupa = {"Arnavutköy", "Avcılar", "Bağcılar", "Bahçelievler", "Bakırköy", "Başakşehir", "Bayrampaşa", "Beşiktaş", "Beylikdüzü", "Beyoğlu", "Büyükçekmece", "Çatalca", "Esenler", "Esenyurt", "Eyüpsultan", "Fatih", "Gaziosmanpaşa", "Güngören", "Kağıthane", "Küçükçekmece", "Sarıyer", "Silivri", "Sultangazi", "Şişli", "Zeytinburnu"};
        for (String name : avrupa) districtList.add(new District(name, "Avrupa"));
    }

    private void createCheckBoxes() {
        container.removeAllViews();
        for (District d : districtList) {
            CheckBox cb = new CheckBox(this);
            cb.setTextColor(android.graphics.Color.WHITE);
            cb.setText(d.getName());
            cb.setTextSize(18);
            cb.setPadding(10, 20, 10, 20);
            cb.setTag(d);
            cb.setOnCheckedChangeListener((bv, isChecked) -> d.setSelected(isChecked));
            container.addView(cb);
        }
    }

    private void setupFilterButtons() {
        findViewById(R.id.btn_select_all).setOnClickListener(v -> updateSelection("Tümü"));
        findViewById(R.id.btn_select_anadolu).setOnClickListener(v -> updateSelection("Anadolu"));
        findViewById(R.id.btn_select_avrupa).setOnClickListener(v -> updateSelection("Avrupa"));
    }

    private void updateSelection(String targetSide) {
        for (int i = 0; i < container.getChildCount(); i++) {
            CheckBox cb = (CheckBox) container.getChildAt(i);
            District d = (District) cb.getTag();
            boolean check = targetSide.equals("Tümü") || d.getSide().equals(targetSide);
            cb.setChecked(check);
            d.setSelected(check);
        }
    }
}