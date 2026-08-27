package com.verikalitesi.rule;

import com.verikalitesi.dto.DuzeltmeEylemi;

import com.verikalitesi.dto.KaliteBoyutuTuru;

import java.util.Optional;

public class EksikAlanKurali implements Kural{
    @Override
    public Optional<DogrulamaSonucu> kontrolEt(String satirNo, String alanAdi, String deger) {


        if(deger == null || deger.trim().isEmpty()){

            DogrulamaSonucu sonuc = new DogrulamaSonucu();
            sonuc.setSatirNo(satirNo);
            sonuc.setAlanAdi(alanAdi);
            sonuc.setMesaj("Alan boş veya eksik");
            sonuc.setDeger(deger);
            sonuc.setEylem(DuzeltmeEylemi.INCELEME);
            sonuc.setEylemNotu("Eksik veri kaynağından tamamlanmalı; araç bir değer uyduramaz.");
            return Optional.of(sonuc);

        }
        return Optional.empty();
    }

    @Override
    public KaliteBoyutuTuru boyut() {
        return KaliteBoyutuTuru.TAMLIK;
    }
}
