package com.verikalitesi.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Kimlik numarası kolonunun kalıp profili.
 *
 * <p>Kolonda baskın bir kalıp bulunamadıysa {@code baskinSekil} null kalır. Bu bir hata
 * değildir: UUID gibi rastgele üretilmiş kimliklerde her değer farklı şekle sahiptir ve
 * o kolonda "kalıba uymayan satır" diye bir kavram yoktur.
 */
public class KimlikNoProfili {

    private String kolonAdi;
    private int toplamDeger;
    private String baskinSekil;
    private int baskinSekilAdedi;
    private double kapsamaOrani;
    private List<SekilDagilimi> dagilim = new ArrayList<>();
    private int enKisaUzunluk;
    private int enUzunUzunluk;

    public boolean isBaskinSekilVarMi() {
        return baskinSekil != null;
    }

    /** Baskın kalıba uymayan değer sayısı. */
    public int getUymayanAdet() {
        return isBaskinSekilVarMi() ? toplamDeger - baskinSekilAdedi : 0;
    }

    /**
     * Uzunluğun oynaması genelde verinin bir yerde sayı tipinden geçip baştaki sıfırları
     * kaybettiğine işaret eder; kesin bir hata değil, incelenmesi gereken bir sinyaldir.
     */
    public boolean isUzunlukDegiskenMi() {
        return enUzunUzunluk > enKisaUzunluk;
    }

    public String getKolonAdi() {
        return kolonAdi;
    }

    public void setKolonAdi(String kolonAdi) {
        this.kolonAdi = kolonAdi;
    }

    public int getToplamDeger() {
        return toplamDeger;
    }

    public void setToplamDeger(int toplamDeger) {
        this.toplamDeger = toplamDeger;
    }

    public String getBaskinSekil() {
        return baskinSekil;
    }

    public void setBaskinSekil(String baskinSekil) {
        this.baskinSekil = baskinSekil;
    }

    public int getBaskinSekilAdedi() {
        return baskinSekilAdedi;
    }

    public void setBaskinSekilAdedi(int baskinSekilAdedi) {
        this.baskinSekilAdedi = baskinSekilAdedi;
    }

    public double getKapsamaOrani() {
        return kapsamaOrani;
    }

    public void setKapsamaOrani(double kapsamaOrani) {
        this.kapsamaOrani = kapsamaOrani;
    }

    public List<SekilDagilimi> getDagilim() {
        return dagilim;
    }

    public void setDagilim(List<SekilDagilimi> dagilim) {
        this.dagilim = dagilim;
    }

    public int getEnKisaUzunluk() {
        return enKisaUzunluk;
    }

    public void setEnKisaUzunluk(int enKisaUzunluk) {
        this.enKisaUzunluk = enKisaUzunluk;
    }

    public int getEnUzunUzunluk() {
        return enUzunUzunluk;
    }

    public void setEnUzunUzunluk(int enUzunUzunluk) {
        this.enUzunUzunluk = enUzunUzunluk;
    }
}
