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

    // 🤖 Sahibem, senin belirlediğin yeni efsane kadro ve zorluk oranları:
    private final String[] BOT_ISIMLERI = {
            "Berkay (Başlangıç)", "Melih Gökçek :(", "Yaya" ,"UN", "Dayı",
            "CumhuriyetKöyü", "Kaptan137", "Yemir",
            "Ekrem İmamoğlu", "YunUsta (Efsane)"
    };
    private final double[] ZORLUK_CARPANI = {0.60, 0.65, 0.70, 0.75, 0.79, 0.83, 0.87, 0.90, 0.93, 0.95};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        Button btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish()); // 🔙 Geri butonu aktif!

        selectedDistricts = getIntent().getStringArrayListExtra("SELECTED_DISTRICTS");
        if (selectedDistricts == null) selectedDistricts = new ArrayList<>();

        veriyiYukle();

        int toplamHat = hesaplaToplamHat();

        Collections.sort(selectedDistricts);
        String havuzAnahtari = TextUtils.join("-", selectedDistricts);
        if (havuzAnahtari.isEmpty()) havuzAnahtari = "İSTANBUL GENEL";

        TextView title = findViewById(R.id.tv_title);
        title.setText("SIRALAMA (" + toplamHat + " Hat)\n" + havuzAnahtari);

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

    // 🚀 Liderlik tablosu için kırılmaz Karakter Motoru
    private String normalizeForSearch(String text) {
        if (text == null) return "";
        return text.replace("İ", "I").replace("i", "I")
                .replace("ı", "I").replace("I", "I")
                .replace("Ş", "S").replace("ş", "S")
                .replace("Ç", "C").replace("ç", "C")
                .replace("Ğ", "G").replace("ğ", "G")
                .replace("Ö", "O").replace("ö", "O")
                .replace("Ü", "U").replace("ü", "U")
                .toUpperCase().trim();
    }

    private int hesaplaToplamHat() {
        int count = 0;
        for (GameActivity.Hat hat : tumHatlar) {
            if (hat.ilceler != null) {
                for (String sd : selectedDistricts) {
                    String safeSelected = normalizeForSearch(sd);
                    boolean match = false;
                    for (String ilce : hat.ilceler) {
                        if (normalizeForSearch(ilce).equals(safeSelected)) {
                            match = true;
                            break;
                        }
                    }
                    if (match) {
                        count++; break;
                    }
                }
            }
        }
        return count;
    }

    private void siralamaOlustur(String havuzKey, int hatSayisi) {
        ArrayList<GameActivity.LeaderboardEntry> list = new ArrayList<>();

        int maxOlasıPuan = hatSayisi * 30;
        for (int i = 0; i < BOT_ISIMLERI.length; i++) {
            int botPuan = (int) (maxOlasıPuan * ZORLUK_CARPANI[i]);
            list.add(new GameActivity.LeaderboardEntry(BOT_ISIMLERI[i], botPuan));
        }

        SharedPreferences highScores = getSharedPreferences("FullModHighScores", MODE_PRIVATE);
        int userBest = highScores.getInt(havuzKey, 0);

        list.add(new GameActivity.LeaderboardEntry("Sahibem (Sen)", userBest));

        Collections.sort(list, (e1, e2) -> Integer.compare(e2.score, e1.score));

        ArrayList<String> displayList = new ArrayList<>();
        int rank = 1;
        for (GameActivity.LeaderboardEntry entry : list) {
            String yazi = rank + ". " + entry.name + " - " + entry.score + " Puan";
            if (entry.name.equals("Sahibem (Sen)")) {
                yazi = "⭐ " + yazi + " ⭐";
            }
            displayList.add(yazi);
            rank++;
        }

        ListView listView = findViewById(R.id.list_view);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        listView.setAdapter(adapter);
    }
}