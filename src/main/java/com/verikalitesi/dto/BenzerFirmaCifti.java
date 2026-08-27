package com.verikalitesi.dto;

import com.verikalitesi.altinkayit.AltinKayitKarari;

import java.util.Map;

public class BenzerFirmaCifti {

    private String id_1;
    private String id_2;

    /**
     * Iki kaydin eslestirilmis kolon degerleri.
     *
     * <p>DAO bu iki haritayi zaten okuyordu ve firma adi ile adresi cekip geri
     * kalanini atiyordu. Ana kayit secimi (survivorship) tum alanlarin dolulugunu
     * saymak zorunda oldugu icin haritalar artik saklaniyor -- ek sorgu yok.
     */
    private Map<String, String> alanlar1;
    private Map<String, String> alanlar2;

    /** Doluluk kuralinin bu cift icin verdigi karar; hesaplanmadiysa null. */
    private AltinKayitKarari altinKayitKarari;

    public Map<String, String> getAlanlar1() {
        return alanlar1;
    }

    public void setAlanlar1(Map<String, String> alanlar1) {
        this.alanlar1 = alanlar1;
    }

    public Map<String, String> getAlanlar2() {
        return alanlar2;
    }

    public void setAlanlar2(Map<String, String> alanlar2) {
        this.alanlar2 = alanlar2;
    }

    public AltinKayitKarari getAltinKayitKarari() {
        return altinKayitKarari;
    }

    public void setAltinKayitKarari(AltinKayitKarari altinKayitKarari) {
        this.altinKayitKarari = altinKayitKarari;
    }

    /** Sablon kolayligi: karar hesaplandi mi. */
    public boolean isAltinKayitKarariVarMi() {
        return altinKayitKarari != null;
    }


    private String firma1;
    private String firma2;

    private double benzerlikOrani;

    private String adres1;
    private String adres2;

    /**
     * Adres benzerligi. Adres kolonu eslestirilmediyse veya adreslerden biri bossa olculemez;
     * bu durumda -1 kalir ve raporda "-" gorunur. Sifir yazmak "hic benzemiyor" demek olurdu,
     * oysa dogru bilgi "bilmiyoruz".
     */
    private double adresBenzerligi = -1;

    public boolean isAdresBenzerligiOlculdu() {
        return adresBenzerligi >= 0;
    }


    public String getId_1() {
        return id_1;
    }

    public void setId_1(String id_1) {
        this.id_1 = id_1;
    }

    public String getId_2() {
        return id_2;
    }

    public void setId_2(String id_2) {
        this.id_2 = id_2;
    }

    public String getFirma1() {
        return firma1;
    }

    public void setFirma1(String firma1) {
        this.firma1 = firma1;
    }

    public String getFirma2() {
        return firma2;
    }

    public void setFirma2(String firma2) {
        this.firma2 = firma2;
    }

    public double getBenzerlikOrani() {
        return benzerlikOrani;
    }

    public void setBenzerlikOrani(double benzerlikOrani) {
        this.benzerlikOrani = benzerlikOrani;
    }

    public String getAdres1() {
        return adres1;
    }

    public void setAdres1(String adres1) {
        this.adres1 = adres1;
    }

    public String getAdres2() {
        return adres2;
    }

    public void setAdres2(String adres2) {
        this.adres2 = adres2;
    }

    public double getAdresBenzerligi() {
        return adresBenzerligi;
    }

    public void setAdresBenzerligi(double adresBenzerligi) {
        this.adresBenzerligi = adresBenzerligi;
    }
}
