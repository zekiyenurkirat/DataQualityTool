package com.verikalitesi.core;

import com.verikalitesi.dto.UlkeSecenegi;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UlkeKoduCozumleyici {

    private static final Locale TURKCE = Locale.forLanguageTag("tr");

    private static final Map<String, String> AD_KOD_HARITASI = adKodHaritasiOlustur();
    private static final List<UlkeSecenegi> ULKE_SECENEKLERI = ulkeSecenekleriOlustur();

    public static String tabloAdindanUlkeKodunuBul(String tabloAdi) {
        if (tabloAdi == null) {
            return null;
        }
        for (String parca : tabloAdi.split("_")) {
            String kod = AD_KOD_HARITASI.get(sadelestir(parca));
            if (kod != null) {
                return kod;
            }
        }
        return null;
    }

    public static List<UlkeSecenegi> tumUlkeler() {
        return ULKE_SECENEKLERI;
    }

    private static Map<String, String> adKodHaritasiOlustur() {
        Map<String, String> harita = new HashMap<>();
        for (String kod : Locale.getISOCountries()) {
            String turkceAd = new Locale("", kod).getDisplayCountry(TURKCE);
            String ingilizceAd = new Locale("", kod).getDisplayCountry(Locale.ENGLISH);
            harita.putIfAbsent(sadelestir(turkceAd), kod);
            harita.putIfAbsent(sadelestir(ingilizceAd), kod);
            harita.putIfAbsent(sadelestir(kod), kod);
        }
        return harita;
    }

    private static List<UlkeSecenegi> ulkeSecenekleriOlustur() {
        List<UlkeSecenegi> secenekler = new ArrayList<>();
        for (String kod : Locale.getISOCountries()) {
            secenekler.add(new UlkeSecenegi(kod, new Locale("", kod).getDisplayCountry(TURKCE)));
        }
        secenekler.sort((a, b) -> a.getAd().compareToIgnoreCase(b.getAd()));
        return secenekler;
    }

    private static String sadelestir(String metin) {
        if (metin == null) {
            return "";
        }
        String kucuk = metin.toLowerCase(TURKCE);
        String ayrilmis = Normalizer.normalize(kucuk, Normalizer.Form.NFD);
        StringBuilder sonuc = new StringBuilder();
        for (int i = 0; i < ayrilmis.length(); i++) {
            char karakter = ayrilmis.charAt(i);
            if (karakter == 'ı') {
                sonuc.append('i');
            } else if (Character.isLetterOrDigit(karakter) && karakter < 128) {
                sonuc.append(karakter);
            }
        }
        return sonuc.toString();
    }
}
