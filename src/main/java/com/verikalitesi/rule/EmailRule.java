package com.verikalitesi.rule;

import com.verikalitesi.dto.DuzeltmeEylemi;

import com.verikalitesi.dto.KaliteBoyutuTuru;

import org.apache.commons.validator.routines.EmailValidator;
import java.util.Optional;

public class EmailRule implements Kural {

    @Override
    public Optional<DogrulamaSonucu> kontrolEt(String satirNo, String alanAdi, String deger) {

        if (deger == null || deger.trim().isEmpty()) {
            return Optional.empty();
        }

        String temiz = deger.trim();

        if (EmailValidator.getInstance().isValid(temiz)) {
            return Optional.empty();
        }

        if (birdenFazlaEpostaVarMi(temiz)) {
            return sonucOlustur(satirNo, alanAdi, deger, "Hücrede birden fazla e-posta adresi var",
                    DuzeltmeEylemi.INCELEME,
                    "İkinci adresin nereye yazılacağı iş kararı: yeni kolon mu, yeni satır mı, silinecek mi?");
        }

        return sonucOlustur(satirNo, alanAdi, deger, "Geçersiz e-posta formatı",
                DuzeltmeEylemi.INCELEME,
                "Doğru adresin ne olduğu veriden bilinemez; uydurmak yanlış veri üretmek olurdu.");
    }

    private boolean birdenFazlaEpostaVarMi(String deger) {
        String[] parcalar = deger.split("[;,\\s]+");
        int epostaBenzeriSayisi = 0;
        for (String parca : parcalar) {
            if (parca.contains("@")) {
                epostaBenzeriSayisi++;
            }
        }
        return epostaBenzeriSayisi > 1;
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
        return KaliteBoyutuTuru.GECERLILIK;
    }
}
