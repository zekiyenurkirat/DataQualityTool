package com.verikalitesi.altinkayit;

import com.verikalitesi.dto.BenzerFirmaCifti;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Benzer kayıt <strong>çiftlerinden</strong> kopya <strong>kümelerini</strong> çıkarır.
 *
 * <p>Sorgu ikişerli eşleşme döndürüyor, ama gerçek dünyada aynı firma üç, beş,
 * on beş kere girilmiş olabiliyor. A~B ve B~C eşleşmesi varsa üçü tek bir
 * firmadır. Çiftleri ikişer ikişer birleştirmek bu üçlüyü iki ayrı ana kayda
 * bölerdi -- ve ikinci birleştirmede birinci ana kayıt zaten pasife çekilmiş
 * olurdu.
 *
 * <p>Kullanılan yöntem <strong>birleştir-bul (union-find / disjoint set)</strong>:
 * her kimlik başta kendi kümesindedir, her çift iki kümeyi birleştirir, sonunda
 * aynı köke bağlı olanlar aynı kümedir. Bağlı bileşen bulmanın standart yolu ve
 * çift sayısına göre neredeyse doğrusal çalışır.
 *
 * <p>Sıra her yerde sabit tutuluyor ({@link TreeMap}, sıralı kimlik listeleri):
 * aynı veri her çalıştırmada aynı kümeleri aynı sırada üretmeli, yoksa
 * kullanıcının ekranda gördüğü küme sayfa yenilendiğinde değişirdi.
 */
public class KumeBulucu {

    /** kimlik -> kök kimlik */
    private final Map<String, String> kok = new HashMap<>();

    public List<KopyaKumesi> kumeleriBul(List<BenzerFirmaCifti> ciftler) {
        kok.clear();
        if (ciftler == null || ciftler.isEmpty()) {
            return List.of();
        }

        // Kayıt içerikleri: aynı kimlik birden çok çiftte geçebilir, ilk görülen yazılır.
        Map<String, Map<String, String>> icerikler = new HashMap<>();
        // Etiket için: kimliğe karşılık gelen firma adı.
        Map<String, String> adlar = new HashMap<>();

        for (BenzerFirmaCifti cift : ciftler) {
            String a = cift.getId_1();
            String b = cift.getId_2();
            if (a == null || b == null || a.isBlank() || b.isBlank()) {
                continue;
            }
            icerikler.putIfAbsent(a, cift.getAlanlar1());
            icerikler.putIfAbsent(b, cift.getAlanlar2());
            adlar.putIfAbsent(a, cift.getFirma1());
            adlar.putIfAbsent(b, cift.getFirma2());
            birlestir(a, b);
        }

        // Köke göre grupla. TreeMap: küme sırası kimliğe göre sabit.
        Map<String, List<String>> kumeler = new TreeMap<>();
        for (String kimlik : icerikler.keySet()) {
            kumeler.computeIfAbsent(bul(kimlik), k -> new ArrayList<>()).add(kimlik);
        }

        List<KopyaKumesi> sonuc = new ArrayList<>();
        for (List<String> uyeler : kumeler.values()) {
            if (uyeler.size() < 2) {
                continue;
            }
            uyeler.sort(Comparator.naturalOrder());

            Map<String, Map<String, String>> kayitlar = new LinkedHashMap<>();
            for (String uye : uyeler) {
                Map<String, String> alanlar = icerikler.get(uye);
                kayitlar.put(uye, alanlar == null ? Map.of() : alanlar);
            }
            String etiket = adlar.get(uyeler.get(0));
            sonuc.add(new KopyaKumesi(uyeler, kayitlar,
                    etiket == null || etiket.isBlank() ? uyeler.get(0) : etiket));
        }

        // Büyük kümeler önce: en çok fazlalık taşıyan en üstte incelensin.
        sonuc.sort(Comparator.comparingInt(KopyaKumesi::getKayitSayisi).reversed()
                .thenComparing(KopyaKumesi::getAnahtar));
        return sonuc;
    }

    /**
     * Yol kısaltmalı kök bulma: bulunan kök doğrudan bağlanıyor, böylece
     * sonraki aramalar zinciri bir daha yürümüyor.
     */
    private String bul(String kimlik) {
        String mevcut = kok.getOrDefault(kimlik, kimlik);
        if (mevcut.equals(kimlik)) {
            return kimlik;
        }
        String gercekKok = bul(mevcut);
        kok.put(kimlik, gercekKok);
        return gercekKok;
    }

    private void birlestir(String a, String b) {
        String kokA = bul(a);
        String kokB = bul(b);
        if (kokA.equals(kokB)) {
            return;
        }
        // Küçük kimlik kök seçiliyor: birleştirme sırası değişse de aynı kök çıksın.
        if (kokA.compareTo(kokB) <= 0) {
            kok.put(kokB, kokA);
        } else {
            kok.put(kokA, kokB);
        }
    }
}
