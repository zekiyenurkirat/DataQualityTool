package com.verikalitesi.dto;


import com.verikalitesi.rule.DogrulamaSonucu;

import java.util.ArrayList;
import java.util.List;

// raporda ekrana  döndürmek için sarmalayıcı dto
public class AnalizSonucu {

    private List<DogrulamaSonucu> dogrulamaSonuclari;
    private List<BenzerFirmaCifti> benzerFirmaCiftleri;
    private List<String> yapilmayanKontroller = new ArrayList<>();
    private List<MesajOzeti> ozet = new ArrayList<>();
    private KolonProfili idKolonuProfili;
    private KimlikNoProfili kimlikNoProfili;
    private KaliteSkorKarti skorKarti = new KaliteSkorKarti();
    private int uretilmisKimlikSayisi;
    private int kimliksizSatirSayisi;
    private List<KelimeFrekansi> haricTutulanKelimeler = new ArrayList<>();
    private List<KopyaGrubu> kopyaGruplari = new ArrayList<>();
    private int toplamFazlalik;
    /** Tekilleştirmeden önce sorgudan dönen ham çift sayısı; raporda şeffaflık için. */
    private int hamBenzerCiftSayisi;


    public List<DogrulamaSonucu> getDogrulamaSonuclari() {
        return dogrulamaSonuclari;
    }

    public void setDogrulamaSonuclari(List<DogrulamaSonucu> dogrulamaSonuclari) {
        this.dogrulamaSonuclari = dogrulamaSonuclari;
    }

    public List<BenzerFirmaCifti> getBenzerFirmaCiftleri() {
        return benzerFirmaCiftleri;
    }

    public void setBenzerFirmaCiftleri(List<BenzerFirmaCifti> benzerFirmaCiftleri) {
        this.benzerFirmaCiftleri = benzerFirmaCiftleri;


    }

    public List<String> getYapilmayanKontroller() {
        return yapilmayanKontroller;
    }

    public void setYapilmayanKontroller(List<String> yapilmayanKontroller) {
        this.yapilmayanKontroller = yapilmayanKontroller;
    }

    public List<MesajOzeti> getOzet() {
        return ozet;
    }

    public void setOzet(List<MesajOzeti> ozet) {
        this.ozet = ozet;
    }

    public KolonProfili getIdKolonuProfili() {
        return idKolonuProfili;
    }

    public void setIdKolonuProfili(KolonProfili idKolonuProfili) {
        this.idKolonuProfili = idKolonuProfili;
    }

    public int getUretilmisKimlikSayisi() {
        return uretilmisKimlikSayisi;
    }

    public void setUretilmisKimlikSayisi(int uretilmisKimlikSayisi) {
        this.uretilmisKimlikSayisi = uretilmisKimlikSayisi;
    }

    public int getKimliksizSatirSayisi() {
        return kimliksizSatirSayisi;
    }

    public void setKimliksizSatirSayisi(int kimliksizSatirSayisi) {
        this.kimliksizSatirSayisi = kimliksizSatirSayisi;
    }

    public List<KelimeFrekansi> getHaricTutulanKelimeler() {
        return haricTutulanKelimeler;
    }

    public void setHaricTutulanKelimeler(List<KelimeFrekansi> haricTutulanKelimeler) {
        this.haricTutulanKelimeler = haricTutulanKelimeler;
    }

    public List<KopyaGrubu> getKopyaGruplari() {
        return kopyaGruplari;
    }

    public void setKopyaGruplari(List<KopyaGrubu> kopyaGruplari) {
        this.kopyaGruplari = kopyaGruplari;
    }

    public int getToplamFazlalik() {
        return toplamFazlalik;
    }

    public void setToplamFazlalik(int toplamFazlalik) {
        this.toplamFazlalik = toplamFazlalik;
    }

    public KaliteSkorKarti getSkorKarti() {
        return skorKarti;
    }

    public void setSkorKarti(KaliteSkorKarti skorKarti) {
        this.skorKarti = skorKarti;
    }

    public KimlikNoProfili getKimlikNoProfili() {
        return kimlikNoProfili;
    }

    public void setKimlikNoProfili(KimlikNoProfili kimlikNoProfili) {
        this.kimlikNoProfili = kimlikNoProfili;
    }

    public int getHamBenzerCiftSayisi() {
        return hamBenzerCiftSayisi;
    }

    public void setHamBenzerCiftSayisi(int hamBenzerCiftSayisi) {
        this.hamBenzerCiftSayisi = hamBenzerCiftSayisi;
    }
}
