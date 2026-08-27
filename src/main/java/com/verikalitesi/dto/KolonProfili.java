package com.verikalitesi.dto;

public class KolonProfili {

    private final String kolonAdi;
    private final int toplamSatir;
    private final int doluSatir;
    private final int tekilDeger;

    public KolonProfili(String kolonAdi, int toplamSatir, int doluSatir, int tekilDeger) {
        this.kolonAdi = kolonAdi;
        this.toplamSatir = toplamSatir;
        this.doluSatir = doluSatir;
        this.tekilDeger = tekilDeger;
    }

    public String getKolonAdi() {
        return kolonAdi;
    }

    public int getToplamSatir() {
        return toplamSatir;
    }

    public int getDoluSatir() {
        return doluSatir;
    }

    public int getTekilDeger() {
        return tekilDeger;
    }

    public int getBosSatir() {
        return toplamSatir - doluSatir;
    }

    public double getDolulukYuzdesi() {
        if (toplamSatir == 0) {
            return 0.0;
        }
        return yuvarla(100.0 * doluSatir / toplamSatir);
    }

    public double getTekillikYuzdesi() {
        if (doluSatir == 0) {
            return 0.0;
        }
        return yuvarla(100.0 * tekilDeger / doluSatir);
    }

    public boolean isBos() {
        return doluSatir == 0;
    }

    public boolean isTamamenDolu() {
        return toplamSatir > 0 && doluSatir == toplamSatir;
    }

    public boolean isTekil() {
        return doluSatir > 0 && tekilDeger == doluSatir;
    }

    private double yuvarla(double deger) {
        return Math.round(deger * 100.0) / 100.0;
    }
}
