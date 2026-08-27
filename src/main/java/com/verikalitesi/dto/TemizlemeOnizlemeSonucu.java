package com.verikalitesi.dto;

import java.util.List;

public class TemizlemeOnizlemeSonucu {

    private String kolonAdi;

    private int etkilenecekSatirSayisi;

    private List<TemizlemeOrnek> ornekler;

    public String getKolonAdi() {
        return kolonAdi;
    }

    public void setKolonAdi(String kolonAdi) {
        this.kolonAdi = kolonAdi;
    }

    public int getEtkilenecekSatirSayisi() {
        return etkilenecekSatirSayisi;
    }

    public void setEtkilenecekSatirSayisi(int etkilenecekSatirSayisi) {
        this.etkilenecekSatirSayisi = etkilenecekSatirSayisi;
    }

    public List<TemizlemeOrnek> getOrnekler() {
        return ornekler;
    }

    public void setOrnekler(List<TemizlemeOrnek> ornekler) {
        this.ornekler = ornekler;
    }
}