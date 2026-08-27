package com.verikalitesi.core;

public class VeritabaniBaglantiBilgisi {

    private String sunucu;
    private int port;
    private String veriTabani;
    private String kullaniciAdi;
    private String sifre;

    public String getSunucu() { return sunucu; }
    public void setSunucu(String sunucu) { this.sunucu = sunucu; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getVeriTabani() { return veriTabani; }
    public void setVeriTabani(String veriTabani) { this.veriTabani = veriTabani; }

    public String getKullaniciAdi() { return kullaniciAdi; }
    public void setKullaniciAdi(String kullaniciAdi) { this.kullaniciAdi = kullaniciAdi; }

    public String getSifre() { return sifre; }
    public void setSifre(String sifre) { this.sifre = sifre; }


}