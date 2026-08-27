package com.verikalitesi.dto;

/**
 * Bir kimlik numarası kalıbının kolonda kaç kez geçtiği.
 *
 * <p>Şekil, değerin karakter sınıflarına indirgenmiş halidir: rakam dizisi {@code 9},
 * harf dizisi {@code A}, ayraçlar olduğu gibi. Örnek: {@code 99227-3302-OOO} şekli
 * {@code 9-9-A} olur.
 */
public class SekilDagilimi {

    private String sekil;
    private int adet;
    private String ornekDeger;

    public SekilDagilimi(String sekil, String ornekDeger) {
        this.sekil = sekil;
        this.ornekDeger = ornekDeger;
    }

    public void arttir() {
        this.adet++;
    }

    public String getSekil() {
        return sekil;
    }

    public void setSekil(String sekil) {
        this.sekil = sekil;
    }

    public int getAdet() {
        return adet;
    }

    public void setAdet(int adet) {
        this.adet = adet;
    }

    public String getOrnekDeger() {
        return ornekDeger;
    }

    public void setOrnekDeger(String ornekDeger) {
        this.ornekDeger = ornekDeger;
    }
}
