package com.verikalitesi.rule;

import com.verikalitesi.dto.DuzeltmeEylemi;

import com.verikalitesi.dto.KaliteBoyutuTuru;

import org.apache.commons.validator.routines.UrlValidator;

import java.util.Optional;

public class WebSitesiKurali implements Kural{

    @Override
    public Optional<DogrulamaSonucu> kontrolEt(String satirNo, String alanAdi, String deger) {

        if (deger == null || deger.trim().isEmpty()) {
            return Optional.empty();
        }

        String temiz = deger.trim();

        if (UrlValidator.getInstance().isValid(temiz)) {
            return Optional.empty();
        }

        if (temiz.contains("@")) {
            return sonucOlustur(satirNo, alanAdi, deger, "Bu alan e-posta adresi içeriyor, web sitesi değil",
                    DuzeltmeEylemi.INCELEME,
                    "Değer yanlış kolonda. E-posta kolonuna taşınmalı mı, silinmeli mi — iş kararı.");
        }

        if (birdenFazlaAdresVarMi(temiz)) {
            return sonucOlustur(satirNo, alanAdi, deger, "Hücrede birden fazla web adresi var",
                    DuzeltmeEylemi.INCELEME,
                    "İkinci adresin nereye yazılacağı iş kararı.");
        }

        if (!alanAdiIceriyorMu(temiz)) {
            return sonucOlustur(satirNo, alanAdi, deger, "Geçerli bir alan adı içermiyor, firma adı veya serbest metin olabilir",
                    DuzeltmeEylemi.INCELEME,
                    "Serbest metin; web adresine çevrilemez.");
        }

        if (!protokolVarMi(temiz) && UrlValidator.getInstance().isValid("http://" + temiz)) {
            return sonucOlustur(satirNo, alanAdi, deger, "Protokol eksik, adresin başında http:// yok",
                    DuzeltmeEylemi.DUZELTILMIYOR,
                    "Başına http:// eklemek eksik veriyi uydurmak olurdu; adres https de olabilir. İşaretlenir, değiştirilmez.");
        }

        return sonucOlustur(satirNo, alanAdi, deger, "Geçersiz web adresi formatı",
                DuzeltmeEylemi.INCELEME,
                "Doğru adresin ne olduğu veriden bilinemez.");
    }

    private boolean birdenFazlaAdresVarMi(String deger) {
        String[] parcalar = deger.split("[;,\\s]+");
        int adresBenzeriSayisi = 0;
        for (String parca : parcalar) {
            if (alanAdiIceriyorMu(parca)) {
                adresBenzeriSayisi++;
            }
        }
        return adresBenzeriSayisi > 1;
    }

    private boolean protokolVarMi(String deger) {
        return deger.regionMatches(true, 0, "http://", 0, 7)
                || deger.regionMatches(true, 0, "https://", 0, 8);
    }

    private boolean alanAdiIceriyorMu(String deger) {
        String govde = deger.replaceFirst("(?i)^https?://", "");
        int noktaYeri = govde.indexOf('.');
        return noktaYeri > 0 && noktaYeri < govde.length() - 1;
    }

    private Optional<DogrulamaSonucu> sonucOlustur(String satirNo, String alanAdi, String deger, String mesaj,
                                                  DuzeltmeEylemi eylem, String eylemNotu) {
        DogrulamaSonucu sonucu = new DogrulamaSonucu();
        sonucu.setSatirNo(satirNo);
        sonucu.setAlanAdi(alanAdi);
        sonucu.setMesaj(mesaj);
        sonucu.setDeger(deger);
        sonucu.setEylem(eylem);
        sonucu.setEylemNotu(eylemNotu);
        return Optional.of(sonucu);
    }

    @Override
    public KaliteBoyutuTuru boyut() {
        return KaliteBoyutuTuru.GECERLILIK;
    }
}
