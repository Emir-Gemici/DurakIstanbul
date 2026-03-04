package com.example.emirgemici;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.widget.LinearLayout;
import java.util.Locale;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private String gameMode;
    private ArrayList<String> selectedDistricts;
    private ArrayList<Hat> tumHatlar = new ArrayList<>();
    private ArrayList<Hat> uygunHatlar = new ArrayList<>();
    private Hat suAnkiSoruHatti;

    private TextView tvScore, tvQuestionCount, tvQuestion, tvTimer;
    private Button[] secenekButonlari = new Button[4];

    private int score = 0;
    private int sorulanSoruSayisi = 0;
    private int dogruCevapSayisi = 0;
    private int toplamSoruSayisi = 0;

    private CountDownTimer timer;
    private final long SURE_SANIYE = 15000;
    private long kalanSaniye = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameMode = getIntent().getStringExtra("GAME_MODE");
        selectedDistricts = getIntent().getStringArrayListExtra("SELECTED_DISTRICTS");
        if (selectedDistricts == null) selectedDistricts = new ArrayList<>();

        tvScore = findViewById(R.id.tv_score);
        tvQuestionCount = findViewById(R.id.tv_question_count);
        tvQuestion = findViewById(R.id.tv_question);
        tvTimer = findViewById(R.id.tv_timer);
        secenekButonlari[0] = findViewById(R.id.btn_option1);
        secenekButonlari[1] = findViewById(R.id.btn_option2);
        secenekButonlari[2] = findViewById(R.id.btn_option3);
        secenekButonlari[3] = findViewById(R.id.btn_option4);

        veriyiYukle();
        filtrele();

        if (uygunHatlar.isEmpty()) {
            Toast.makeText(this, "Sahibem, bu kriterlerde hat bulunamadı!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Hatları karıştırıyoruz
        Collections.shuffle(uygunHatlar);

        // 🚀 RASGELE MOD MANTIĞI: Soru Sayısını Kullanıcıdan Al
        if (gameMode != null && gameMode.equalsIgnoreCase("RANDOM")) {
            showQuestionCountDialog();
        } else {
            // Full Modda tüm hatları sorar
            toplamSoruSayisi = uygunHatlar.size();
            guncelleUI();
            yeniSoruSor();
        }
    }

    private void showQuestionCountDialog() {
        final int maxHat = uygunHatlar.size();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Soru Sayısını Belirle");
        builder.setMessage("Seçilen bölgede toplam " + maxHat + " hat bulundu. Kaç soru sormamı istersin?");
        builder.setCancelable(false);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Örn: 5");
        builder.setView(input);

        // 🦾 AKILLI SINIRLAYICI
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    try {
                        int enteredVal = Integer.parseInt(s.toString());
                        if (enteredVal > maxHat) {
                            input.setText(String.valueOf(maxHat));
                            input.setSelection(input.getText().length());
                            Toast.makeText(GameActivity.this, "Maksimum " + maxHat + " hat seçilebilir!", Toast.LENGTH_SHORT).show();
                        } else if (enteredVal == 0) {
                            input.setText("1");
                            input.setSelection(1);
                        }
                    } catch (NumberFormatException e) { }
                }
            }
        });

        builder.setPositiveButton("Başlat", (dialog, which) -> {
            String enteredText = input.getText().toString();
            if (!enteredText.isEmpty()) {
                toplamSoruSayisi = Integer.parseInt(enteredText);
            } else {
                toplamSoruSayisi = 1;
            }
            guncelleUI();
            yeniSoruSor();
        });

        builder.setNegativeButton("Vazgeç", (dialog, which) -> finish());
        builder.show();
    }

    private void veriyiYukle() {
        try {
            InputStream is = getAssets().open("istanbul_data.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String jsonText = new String(buffer, StandardCharsets.UTF_8);
            tumHatlar = new Gson().fromJson(jsonText, new TypeToken<ArrayList<Hat>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🚀 KIRILMAZ TÜRKÇE KARAKTER MOTORU (JSON'da ne yazarsa yazsın eşitler)
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

    private void filtrele() {
        uygunHatlar.clear();
        for (Hat hat : tumHatlar) {
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
                        uygunHatlar.add(hat);
                        break;
                    }
                }
            }
        }
    }

    private void yeniSoruSor() {
        if (sorulanSoruSayisi >= toplamSoruSayisi) {
            oyunuBitir();
            return;
        }

        suAnkiSoruHatti = uygunHatlar.get(sorulanSoruSayisi);
        sorulanSoruSayisi++;
        guncelleUI();

        tvQuestion.setText(suAnkiSoruHatti.kod + " rotası nasıldır?");

        List<String> siklar = new ArrayList<>();
        siklar.add(suAnkiSoruHatti.getTemizRota());

        Random r = new Random();
        while (siklar.size() < 4) {
            String yanlis = (uygunHatlar.size() >= 4) ?
                    uygunHatlar.get(r.nextInt(uygunHatlar.size())).getTemizRota() :
                    tumHatlar.get(r.nextInt(tumHatlar.size())).getTemizRota();
            if (!siklar.contains(yanlis)) siklar.add(yanlis);
        }

        Collections.shuffle(siklar);
        for (int i = 0; i < 4; i++) {
            secenekButonlari[i].setText(siklar.get(i));
            secenekButonlari[i].setEnabled(true);
            // 🚀 Şıkları her soruda varsayılan koyu gri rengine geri döndürür
            secenekButonlari[i].setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#424242")));

            final String cevap = siklar.get(i);
            final Button secilenButon = secenekButonlari[i]; // Tıklanan butonu hafızaya al
            secenekButonlari[i].setOnClickListener(v -> cevapKontrol(secilenButon, cevap));
        }
        sureyiBaslat();
    }

    private void sureyiBaslat() {
        if (timer != null) timer.cancel();
        kalanSaniye = 15;
        tvTimer.setText("Süre: " + kalanSaniye);
        timer = new CountDownTimer(15000, 1000) {
            public void onTick(long ms) { kalanSaniye = ms / 1000; tvTimer.setText("Süre: " + kalanSaniye); }
            public void onFinish() { Toast.makeText(GameActivity.this, "Süre Doldu!", Toast.LENGTH_SHORT).show(); yeniSoruGecisYap(); }
        }.start();
    }

    // 🚀 MÜKEMMELLEŞTİRİLMİŞ GÖRSEL GERİ BİLDİRİM
    private void cevapKontrol(Button secilenButon, String cevap) {
        if (timer != null) timer.cancel();

        boolean isCorrect = cevap.equals(suAnkiSoruHatti.getTemizRota());

        if (isCorrect) {
            secilenButon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // 🟢 Doğruysa Yeşil yanar
            int kazanc = 10 + (int)(kalanSaniye * 2);
            score += kazanc; dogruCevapSayisi++;
            Toast t = Toast.makeText(this, "Harika! +" + kazanc + " Puan.", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.BOTTOM, 0, 100); t.show();
        } else {
            secilenButon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336"))); // 🔴 Yanlışsa Kırmızı yanar
            // Sen yanlış yapsan bile, sistem sana DOĞRU olan şıkkı Yeşil yakarak öğretir!
            for (Button b : secenekButonlari) {
                if (b.getText().toString().equals(suAnkiSoruHatti.getTemizRota())) {
                    b.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                }
            }
            Toast t = Toast.makeText(this, "Yanlış! Doğrusu işaretlendi.", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.BOTTOM, 0, 100); t.show();
        }
        guncelleUI();
        yeniSoruGecisYap();
    }

    private void yeniSoruGecisYap() {
        for (Button b : secenekButonlari) b.setEnabled(false);
        tvQuestion.postDelayed(this::yeniSoruSor, 2000);
    }

    private void guncelleUI() {
        tvScore.setText("Puan: " + score);
        tvQuestionCount.setText("Soru: " + sorulanSoruSayisi + "/" + toplamSoruSayisi);
    }

    // 🚀 %85 BAŞARI TOLERANSI + KUPA ANİMASYONLU BİTİŞ EKRANI
    private void oyunuBitir() {
        if (timer != null) timer.cancel();

        Collections.sort(selectedDistricts);
        String havuzKey = android.text.TextUtils.join("-", selectedDistricts);

        String mesaj = "Toplam Puanın: " + score;
        boolean sertifikaKazandi = false; // Kupa animasyonu tetikleyicisi

        if ("FULL".equalsIgnoreCase(gameMode)) {
            SharedPreferences highScores = getSharedPreferences("FullModHighScores", MODE_PRIVATE);
            int currentBest = highScores.getInt(havuzKey, 0);

            if (score > currentBest) {
                highScores.edit().putInt(havuzKey, score).apply();
                mesaj += "\n🔥 YENİ BÖLGE REKORU: " + score;
            } else {
                mesaj += "\nBölge Rekorun: " + currentBest;
            }

            double basariOrani = (double) dogruCevapSayisi / toplamSoruSayisi;
            int yuzde = (int) (basariOrani * 100);

            if (basariOrani >= 0.85) {
                sertifikaKazandi = true;
                SharedPreferences certs = getSharedPreferences("Sertifikalar", MODE_PRIVATE);
                certs.edit().putBoolean(havuzKey, true).apply();
                mesaj += "\n\n🏆 %" + yuzde + " BAŞARI İLE UZMANLIK SERTİFİKASI KAZANDIN!";
            } else {
                mesaj += "\n\n❌ Sertifika için %85 başarı gerekiyor. (Senin Başarın: %" + yuzde + ")";
            }
        } else {
            mesaj += "\n\n(Rekorlar ve Sertifikalar sadece Full Mod'da geçerlidir)";
        }

        showAnimatedResultDialog(sertifikaKazandi, mesaj);
    }

    // 🚀 EFSANEVİ ANİMASYONLU PENCERE
    private void showAnimatedResultDialog(boolean gotCertificate, String mesaj) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 60, 60, 60);
        layout.setGravity(Gravity.CENTER);

        if (gotCertificate) {
            TextView trophy = new TextView(this);
            trophy.setText("🏆");
            trophy.setTextSize(100f);
            trophy.setGravity(Gravity.CENTER);
            layout.addView(trophy);

            // Büyüyüp Küçülen (Kalp Atışı) Animasyonu
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(trophy, "scaleX", 0.8f, 1.3f, 1.0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(trophy, "scaleY", 0.8f, 1.3f, 1.0f);
            scaleX.setDuration(1200);
            scaleY.setDuration(1200);
            scaleX.setRepeatCount(ValueAnimator.INFINITE);
            scaleX.setRepeatMode(ValueAnimator.REVERSE);
            scaleY.setRepeatCount(ValueAnimator.INFINITE);
            scaleY.setRepeatMode(ValueAnimator.REVERSE);
            scaleX.start();
            scaleY.start();
        }

        TextView tvMsg = new TextView(this);
        tvMsg.setText(mesaj);
        tvMsg.setTextSize(18f);
        tvMsg.setGravity(Gravity.CENTER);
        tvMsg.setTextColor(Color.BLACK);
        tvMsg.setPadding(0, 30, 0, 0);
        layout.addView(tvMsg);

        builder.setView(layout)
                .setTitle(gotCertificate ? "✨ TEBRİKLER! ✨" : "Oyun Bitti!")
                .setCancelable(false)
                .setPositiveButton("Menüye Dön", (dialog, which) -> finish())
                .show();
    }

    @Override
    protected void onDestroy() { super.onDestroy(); if (timer != null) timer.cancel(); }

    public static class Hat {
        public String kod;
        public String ad;
        public ArrayList<String> ilceler;

        public String getTemizRota() {
            if (ad != null && ad.contains("/")) {
                return ad.substring(ad.indexOf("/") + 1).trim();
            }
            return ad;
        }
    }

    // 🚀 Liderlik Tablosunun Çalışması İçin Gereken Sınıf Modeli
    public static class LeaderboardEntry {
        public String name;
        public int score;
        public LeaderboardEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }
}