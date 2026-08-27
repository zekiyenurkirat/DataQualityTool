package com.verikalitesi.dto;

public class MesajOzeti {

    /** Bu bulgu turu icin ne yapilabilecegi; ayni mesajin butun bulgulari ayni eylemi tasir. */
    private DuzeltmeEylemi eylem = DuzeltmeEylemi.INCELEME;

    private String eylemNotu = "";

    private final String alanAdi;
    private final String mesaj;
    private int adet;

    public MesajOzeti(String alanAdi, String mesaj) {
        this.alanAdi = alanAdi;
        this.mesaj = mesaj;
        this.adet = 0;
    }

    public void arttir() {
        this.adet++;
    }

    public String getAlanAdi() {
        return alanAdi;
    }

    public String getMesaj() {
        return mesaj;
    }

    public int getAdet() {
        return adet;
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
