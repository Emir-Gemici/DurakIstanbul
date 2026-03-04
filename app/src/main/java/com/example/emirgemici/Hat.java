package com.example.emirgemici;

import java.util.ArrayList;

/**
 * Sahibem, bu sınıf JSON'dan gelen hat verilerini temsil eder.
 */
public class Hat {
    public String kod;
    public String ad;
    public ArrayList<String> ilceler;

    // "KM45/MARMARA ÜNİVERSİTESİ..." kısmındaki slash'tan sonrasını temizler.
    public String getTemizRota() {
        if (ad != null && ad.contains("/")) {
            return ad.substring(ad.indexOf("/") + 1).trim();
        }
        return ad;
    }
}
