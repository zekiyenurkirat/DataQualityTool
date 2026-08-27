package com.verikalitesi.anahtar;

import com.verikalitesi.dto.AlanEslestirmesi;
import com.verikalitesi.dto.SatirVerisi;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class HashAnahtarUretici implements AnahtarUretici {

    private static final String AYRAC = "|";
    private static final String ALGORITMA = "SHA-256";
    private static final String URETILMIS_ISARETI = "⚙";
    private static final int GOSTERILECEK_UZUNLUK = 12;

    @Override
    public Optional<String> uret(SatirVerisi satir, AlanEslestirmesi alanEslestirmesi) {

        Map<String, String> alanlar = satir.getAlanlar();
        if (alanlar == null) {
            return Optional.empty();
        }

        List<String> sirali = new ArrayList<>();
        sirali.add(sadelestir(alanlar.get(alanEslestirmesi.getFirmaAdiKolonu())));
        sirali.add(adresAnahtari(alanlar, alanEslestirmesi));
        sirali.add(sadelestir(alanlar.get(alanEslestirmesi.getTelefonKolonu())));
        sirali.add(sadelestir(alanlar.get(alanEslestirmesi.getePostaKolonu())));
        sirali.add(sadelestir(alanlar.get(alanEslestirmesi.getWebSitesiKolonu())));

        boolean hepsiBos = true;
        for (String parca : sirali) {
            // Ayraçlar sayılmaz: adres birden çok kolondan geldiğinde hepsi boş olsa bile
            // parça "|" gibi yalnızca ayraçtan oluşur. Bunu dolu saymak, tamamen boş bir
            // satıra kimlik üretmek olurdu.
            if (!parca.replace(AYRAC, "").isEmpty()) {
                hepsiBos = false;
                break;
            }
        }
        if (hepsiBos) {
            return Optional.empty();
        }

        String tamHash = hashle(String.join(AYRAC, sirali));
        return Optional.of(URETILMIS_ISARETI + tamHash.substring(0, GOSTERILECEK_UZUNLUK));
    }

    /**
     * Eşleştirilen adres kolonlarını tek bir değere birleştirir.
     *
     * <p>Kolon adları <b>alfabetik sıraya</b> göre gezilir, kullanıcının seçim sırasına göre
     * değil. Aynı satır her çalıştırmada aynı kimliği almalı; seçim sırasına bağlı kalsaydı
     * kullanıcı kolonları farklı sırayla işaretlediğinde kimlik değişir, kopya gruplaması
     * da onunla birlikte bozulurdu.
     *
     * <p>Boş kolonlar atlanmaz, yerleri boş bırakılır. Bir satırda mahalle dolu sokak boş,
     * diğerinde tersi olsaydı ve boşlar atlansaydı ikisi aynı metne düşerdi.
     */
    private String adresAnahtari(Map<String, String> alanlar, AlanEslestirmesi alanEslestirmesi) {
        List<String> kolonlar = alanEslestirmesi.getSiraliAdresKolonlari();
        if (kolonlar.isEmpty()) {
            return "";
        }
        // Her kolon önce tek tek sadeleştirilir, sonra ayraçla birleştirilir. Ters sırada
        // yapılsaydı sadeleştirme ayraçları da silerdi ve alt alanların yeri kaybolurdu:
        // (mahalle="A", sokak=boş) ile (mahalle=boş, sokak="A") aynı kimliğe düşerdi.
        List<String> parcalar = new ArrayList<>();
        for (String kolon : kolonlar) {
            parcalar.add(sadelestir(alanlar.get(kolon)));
        }
        return String.join(AYRAC, parcalar);
    }

    private String sadelestir(String deger) {
        if (deger == null) {
            return "";
        }
        String kucuk = deger.toLowerCase(Locale.ROOT);
        StringBuilder temiz = new StringBuilder();
        boolean oncekiBosluktu = true;
        for (int i = 0; i < kucuk.length(); i++) {
            char karakter = kucuk.charAt(i);
            if (Character.isLetterOrDigit(karakter)) {
                temiz.append(karakter);
                oncekiBosluktu = false;
            } else if (!oncekiBosluktu) {
                temiz.append(' ');
                oncekiBosluktu = true;
            }
        }
        return temiz.toString().trim();
    }

    private String hashle(String metin) {
        try {
            MessageDigest ozetleyici = MessageDigest.getInstance(ALGORITMA);
            byte[] ozet = ozetleyici.digest(metin.getBytes(StandardCharsets.UTF_8));
            StringBuilder onaltilik = new StringBuilder(ozet.length * 2);
            for (byte bayt : ozet) {
                onaltilik.append(Character.forDigit((bayt >> 4) & 0xF, 16));
                onaltilik.append(Character.forDigit(bayt & 0xF, 16));
            }
            return onaltilik.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(ALGORITMA + " bu Java sürümünde bulunamadı", e);
        }
    }
}
