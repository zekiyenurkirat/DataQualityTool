package com.verikalitesi.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlanEslestirmesi {


    private String firmaAdiKolonu;
    private String ePostaKolonu;
    private String telefonKolonu;
    /**
     * Adres bilgisini taşıyan kolonlar. Tek alan yerine liste, çünkü tablolar adresi iki
     * farklı şekilde tutuyor: kimi tek kolonda tam adres yazıyor, kimi şehir/mahalle/sokak
     * diye ayırıyor. Tek kolon seçtirmek ikinci durumda karşılaştırmayı şehir eşleşmesine
     * indiriyordu.
     */
    private List<String> adresKolonlari = new ArrayList<>();
    private String idKolonu;
    private String webSitesiKolonu;
    private String ulkeKodu;

    public String getFirmaAdiKolonu() {
        return firmaAdiKolonu;
    }

    public void setFirmaAdiKolonu(String firmaAdiKolonu) {
        this.firmaAdiKolonu = firmaAdiKolonu;
    }

    public String getePostaKolonu() {
        return ePostaKolonu;
    }

    public void setePostaKolonu(String ePostaKolonu) {
        this.ePostaKolonu = ePostaKolonu;
    }

    public String getTelefonKolonu() {
        return telefonKolonu;
    }

    public void setTelefonKolonu(String telefonKolonu) {
        this.telefonKolonu = telefonKolonu;
    }

    public List<String> getAdresKolonlari() {
        return adresKolonlari;
    }

    public void setAdresKolonlari(List<String> adresKolonlari) {
        this.adresKolonlari = adresKolonlari == null ? new ArrayList<>() : adresKolonlari;
    }

    public boolean adresEslestirildiMi() {
        return !adresKolonlari.isEmpty();
    }

    /**
     * Kolon adlarını alfabetik sırada döndürür. Kimlik üretimi bu sıraya dayanıyor:
     * kullanıcı aynı kolonları farklı sırayla seçse bile aynı kimlik çıkmalı, yoksa
     * aynı satır iki çalıştırmada iki farklı kimlik alır ve kopya gruplaması bozulur.
     */
    public List<String> getSiraliAdresKolonlari() {
        List<String> sirali = new ArrayList<>(adresKolonlari);
        Collections.sort(sirali);
        return sirali;
    }

    public String getIdKolonu() {
        return idKolonu;
    }

    public void setIdKolonu(String idKolonu) {
        this.idKolonu = idKolonu;
    }

    public String getWebSitesiKolonu() {
        return webSitesiKolonu;
    }

    public void setWebSitesiKolonu(String webSitesiKolonu) {
        this.webSitesiKolonu = webSitesiKolonu;
    }

    public String getUlkeKodu() {
        return ulkeKodu;
    }

    public void setUlkeKodu(String ulkeKodu) {
        this.ulkeKodu = ulkeKodu;
    }
}
