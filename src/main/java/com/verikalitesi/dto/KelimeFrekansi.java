package com.verikalitesi.dto;

public class KelimeFrekansi {
    private String kelime;
    private int tekrarSayisi;
    private double yuzde;

    /** true ise kelimeyi kullanıcı değil, kalıp kelime tespiti seçmiştir. */
    private boolean otomatikSecildi;

    public String getKelime() {
        return kelime;
    }

    public void setKelime(String kelime) {
        this.kelime = kelime;
    }

    public int getTekrarSayisi() {
        return tekrarSayisi;
    }

    public void setTekrarSayisi(int tekrarSayisi) {
        this.tekrarSayisi = tekrarSayisi;
    }

    public double getYuzde() {
        return yuzde;
    }

    public void setYuzde(double yuzde) {
        this.yuzde = yuzde;
    }

    public boolean isOtomatikSecildi() {
        return otomatikSecildi;
    }

    public void setOtomatikSecildi(boolean otomatikSecildi) {
        this.otomatikSecildi = otomatikSecildi;
    }
}
