package com.verikalitesi.rule;

import com.verikalitesi.dto.DuzeltmeEylemi;

import com.verikalitesi.dto.KaliteBoyutuTuru;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class PlaceholderKurali implements Kural {

    private final Set<String> yerTutucular;

    public PlaceholderKurali(List<String> yerTutucular) {
        Set<String> hazirlanan = new HashSet<>();
        if (yerTutucular != null) {
            for (String yerTutucu : yerTutucular) {
                if (yerTutucu != null && !yerTutucu.isBlank()) {
                    hazirlanan.add(yerTutucu.trim().toLowerCase(Locale.ROOT));
                }
            }
        }
        this.yerTutucular = Collections.unmodifiableSet(hazirlanan);
    }

    @Override
    public Optional<DogrulamaSonucu> kontrolEt(String satirNo, String alanAdi, String deger) {

        if (deger == null || deger.trim().isEmpty()) {
            return Optional.empty();
        }

        String karsilastirma = deger.trim().toLowerCase(Locale.ROOT);

        if (yerTutucular.contains(karsilastirma)) {
            return sonucOlustur(satirNo, alanAdi, deger, "Alan dolu görünüyor ancak gerçek bir bilgi içermiyor",
                    DuzeltmeEylemi.OTOMATIK,
                    "Verileri Temizle ekranında bu kolonu seçip boşaltabilirsiniz; sahte doluluk gerçek boşluğa çevrilir.");
        }

        if (!harfVeyaRakamIceriyorMu(deger)) {
            return sonucOlustur(satirNo, alanAdi, deger, "Alan yalnızca noktalama işareti içeriyor",
                    DuzeltmeEylemi.OTOMATIK,
                    "Verileri Temizle ekranında \"Silinecek karakterler\" seçeneğiyle temizlenebilir.");
        }

        return Optional.empty();
    }

    private boolean harfVeyaRakamIceriyorMu(String deger) {
        for (int i = 0; i < deger.length(); i++) {
            if (Character.isLetterOrDigit(deger.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private Optional<DogrulamaSonucu> sonucOlustur(String satirNo, String alanAdi, String deger, String mesaj,
                                                  DuzeltmeEylemi eylem, String eylemNotu) {
        DogrulamaSonucu sonuc = new DogrulamaSonucu();
        sonuc.setSatirNo(satirNo);
        sonuc.setAlanAdi(alanAdi);
        sonuc.setMesaj(mesaj);
        sonuc.setDeger(deger);
        sonuc.setEylem(eylem);
        sonuc.setEylemNotu(eylemNotu);
        return Optional.of(sonuc);
    }

    @Override
    public KaliteBoyutuTuru boyut() {
        return KaliteBoyutuTuru.TAMLIK;
    }
}
