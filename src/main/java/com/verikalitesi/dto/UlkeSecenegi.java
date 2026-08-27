package com.verikalitesi.dto;

public class UlkeSecenegi {

    private final String kod;
    private final String ad;

    public UlkeSecenegi(String kod, String ad) {   // bilgiyi  dışarıdan alacağı için cunstrocter kurduk
        this.kod = kod;
        this.ad = ad;
    }

    public String getKod() {
        return kod;
    }

    public String getAd() {
        return ad;
    }
}
