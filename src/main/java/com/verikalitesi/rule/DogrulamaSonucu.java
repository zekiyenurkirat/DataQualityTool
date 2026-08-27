package com.verikalitesi.rule;

import com.verikalitesi.dto.DuzeltmeEylemi;

public class DogrulamaSonucu {

    private String satirNo;
    private String alanAdi;
    private String mesaj;
    private String deger;

    /** Bu bulgu icin ne yapilabilecegi; rapordaki eylem haritasini besler. */
    private DuzeltmeEylemi eylem = DuzeltmeEylemi.INCELEME;

    /** Eyleme dair somut yonlendirme; hangi ekranda hangi kutu isaretlenecek. */
    private String eylemNotu = "";

    public String getSatirNo() {
        return satirNo;
    }

    public void setSatirNo(String satirNo) {
        this.satirNo = satirNo;
    }

    public String getAlanAdi() {
        return alanAdi;
    }

    public void setAlanAdi(String alanAdi) {
        this.alanAdi = alanAdi;
    }


    public String getMesaj() {
        return mesaj;
    }

    public void setMesaj(String mesaj) {
        this.mesaj = mesaj;
    }

    public String getDeger() {
        return deger;
    }

    public void setDeger(String deger) {
        this.deger = deger;
    }

    public DuzeltmeEylemi getEylem() {
        return eylem;
    }

    public void setEylem(DuzeltmeEylemi eylem) {
        this.eylem = eylem;
    }

    public String getEylemNotu() {
        return eylemNotu;
    }

    public void setEylemNotu(String eylemNotu) {
        this.eylemNotu = eylemNotu;
    }
}
