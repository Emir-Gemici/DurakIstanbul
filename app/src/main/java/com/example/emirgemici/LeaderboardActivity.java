package com.example.emirgemici;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

public class LeaderboardActivity extends AppCompatActivity {

    private ArrayList<String> selectedDistricts;
    private ArrayList<GameActivity.Hat> tumHatlar = new ArrayList<>();

    // 🤖 Sahibem, botları kolaydan zora doğru sıraladım.
    private final String[] BOT_ISIMLERI = {
            "Berkay (Başlangıç)", "Melih Gökçek :(", "Yaya" ,"UN", "Dayı",
            "CumhuriyetKöyü", "Kaptan137", "Yemir",
            "Ekrem İmamoğlu", "YunUsta (Efsane)"
    };
    // Bu botlar, o ilçedeki hat sayısının yüzde kaçını doğru biliyor?
    private final double[] ZORLUK_CARPANI = {0.10, 0.20, 0.30, 0.40, 0.50, 0.60, 0.70, 0.79, 0.87, 0.93};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        Button btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish()); // 🔙 Geri butonu aktif!

        selectedDistricts = getIntent().getStringArrayListExtra("SELECTED_DISTRICTS");
        if (selectedDistricts == null) selectedDistricts = new ArrayList<>();

        veriyiYukle();

        // Seçtiğin ilçelerdeki toplam hat sayısını bul
        int toplamHat = hesaplaToplamHat();

        // Benzersiz İlçe Anahtarı oluştur (Örn: "KADIKÖY-MALTEPE")
        Collections.sort(selectedDistricts);
        String havuzAnahtari = TextUtils.join("-", selectedDistricts);
        if (havuzAnahtari.isEmpty()) havuzAnahtari = "İSTANBUL GENEL";

        TextView title = findViewById(R.id.tv_title);
        title.setText("SIRALAMA (" + toplamHat + " Hat)\n" + havuzAnahtari);

        // Sıralamayı Hesapla ve Göster
        siralamaOlustur(havuzAnahtari, toplamHat);
    }

    private void veriyiYukle() {
        try {
            InputStream is = getAssets().open("istanbul_data.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String jsonText = new String(buffer, StandardCharsets.UTF_8);
            tumHatlar = new Gson().fromJson(jsonText, new TypeToken<ArrayList<GameActivity.Hat>>(){}.getType());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private int hesaplaToplamHat() {
        int count = 0;
        for (GameActivity.Hat hat : tumHatlar) {
            if (hat.ilceler != null) {
                for (String sd : selectedDistricts) {
                    if (hat.ilceler.contains(sd.replace("i", "İ").replace("ı", "I").toUpperCase().trim())) {
                        count++; break;
                    }
                }
            }
        }
        return count;
    }

    private void siralamaOlustur(String havuzKey, int hatSayisi) {
        ArrayList<GameActivity.LeaderboardEntry> list = new ArrayList<>();

        // 1. Bot Puanlarını Dinamik Hesapla (Her hat için max 30 puan varsayımıyla)
        int maxOlasıPuan = hatSayisi * 30;
        for (int i = 0; i < BOT_ISIMLERI.length; i++) {
            int botPuan = (int) (maxOlasıPuan * ZORLUK_CARPANI[i]);
            list.add(new GameActivity.LeaderboardEntry(BOT_ISIMLERI[i], botPuan));
        }

        // 2. Oyuncunun o bölgelerdeki rekorunu çek
        SharedPreferences highScores = getSharedPreferences("FullModHighScores", MODE_PRIVATE);
        int userBest = highScores.getInt(havuzKey, 0);

        // 3. Oyuncuyu listeye ekle
        list.add(new GameActivity.LeaderboardEntry("Sahibem (Sen)", userBest));

        // 4. Puanlara göre büyükten küçüğe sırala
        Collections.sort(list, (e1, e2) -> Integer.compare(e2.score, e1.score));

        // 5. Listeyi UI'a bas
        ArrayList<String> displayList = new ArrayList<>();
        int rank = 1;
        for (GameActivity.LeaderboardEntry entry : list) {
            String yazi = rank + ". " + entry.name + " - " + entry.score + " Puan";
            if (entry.name.equals("Sahibem (Sen)")) {
                yazi = "⭐ " + yazi + " ⭐"; // Kullanıcıyı vurgula
            }
            displayList.add(yazi);
            rank++;
        }

        ListView listView = findViewById(R.id.list_view);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        listView.setAdapter(adapter);
    }
}