package com.example.emirgemici;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Map;

public class CertificatesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        // 🔙 GERİ BUTONU AKTİF!
        Button btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        TextView title = findViewById(R.id.tv_title);
        title.setText("UZMANLIK SERTİFİKALARIM");

        ListView listView = findViewById(R.id.list_view);
        SharedPreferences prefs = getSharedPreferences("Sertifikalar", MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();

        ArrayList<String> earnedCerts = new ArrayList<>();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getValue() instanceof Boolean && (Boolean) entry.getValue()) {
                earnedCerts.add("🏆 " + entry.getKey() + " Bölge Uzmanı");
            }
        }

        if (earnedCerts.isEmpty()) {
            earnedCerts.add("Henüz sertifikan yok. Full Mod'da hatasız oynamalısın!");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, earnedCerts);
        listView.setAdapter(adapter);
    }
}