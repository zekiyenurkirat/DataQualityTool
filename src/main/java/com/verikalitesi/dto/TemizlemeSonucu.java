package com.verikalitesi.dto;

import java.util.List;

public class TemizlemeSonucu {

    private List<KolonTemizlemeSonucu> kolonSonuclari;

    public List<KolonTemizlemeSonucu> getKolonSonuclari() {
        return kolonSonuclari;
    }

    public void setKolonSonuclari(List<KolonTemizlemeSonucu> kolonSonuclari) {
        this.kolonSonuclari = kolonSonuclari;
    }
}
