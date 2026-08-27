package com.verikalitesi.rule;

import com.verikalitesi.dto.DuzeltmeEylemi;

import com.verikalitesi.dto.KaliteBoyutuTuru;

import java.util.Optional;

public class BoslukKurali implements Kural{
    @Override
    public Optional<DogrulamaSonucu> kontrolEt(String satirNo, String alanAdi, String deger) {



        if (deger == null) {
            return Optional.empty();
        }



        if( !deger.equals(deger.trim())){

            DogrulamaSonucu sonuc = new DogrulamaSonucu();
            sonuc.setMesaj("Başta veya sonda boşluk var.");
            sonuc.setEylem(DuzeltmeEylemi.OTOMATIK);
            sonuc.setEylemNotu("Verileri Temizle ekranında \"Boşluk temizle\" kutusu bunu düzeltir.");
            sonuc.setDeger(deger);
            sonuc.setAlanAdi(alanAdi);
            sonuc.setSatirNo(satirNo);
            return Optional.of(sonuc);

        }


        if ((deger.contains("  "))){

            DogrulamaSonucu sonuc = new DogrulamaSonucu();
            sonuc.setMesaj("Ardışık boşluk karakteri var.");
            sonuc.setEylem(DuzeltmeEylemi.OTOMATIK);
            sonuc.setEylemNotu("Verileri Temizle ekranında \"Boşluk temizle\" kutusu bunu düzeltir.");
            sonuc.setAlanAdi(alanAdi);
            sonuc.setSatirNo(satirNo);
            return Optional.of(sonuc);

        }
        return Optional.empty();
    }

    @Override
    public KaliteBoyutuTuru boyut() {
        return KaliteBoyutuTuru.TUTARLILIK;
    }
}
