package com.verikalitesi.dto;

/**
 * Skor kartının tek satırı: bir boyutun bu tabloda aldığı puan.
 *
 * <p>Puan, <b>kontrol edilen hücrelerin</b> ne kadarının sorunsuz çıktığıdır. Payda toplam
 * hücre sayısı değil, o boyut adına gerçekten denetlenen hücre sayısıdır: ülke seçilmediği
 * için telefon biçimi denetlenmediyse o hücreler geçerlilik paydasına girmez. Denetlenmemiş
 * bir hücreyi "sorunsuz" saymak skoru haksız yere yükseltirdi.
 */
public class KaliteBoyutu {

    private KaliteBoyutuTuru tur;
    private int kontrolEdilen;
    private int sorunlu;

    public KaliteBoyutu(KaliteBoyutuTuru tur) {
        this.tur = tur;
    }

    public void kontrolEkle() {
        this.kontrolEdilen++;
    }

    public void sorunEkle() {
        this.sorunlu++;
    }

    /** Bu boyut adına hiç denetim yapılmadıysa puan yoktur; sıfır puan ile karıştırılmamalı. */
    public boolean isPuanlanabilir() {
        return kontrolEdilen > 0;
    }

    public int getSorunsuz() {
        return kontrolEdilen - sorunlu;
    }

    public double getPuan() {
        if (!isPuanlanabilir()) {
            return 0;
        }
        return Math.round((getSorunsuz() * 10000.0) / kontrolEdilen) / 100.0;
    }

    public KaliteBoyutuTuru getTur() {
        return tur;
    }

    public void setTur(KaliteBoyutuTuru tur) {
        this.tur = tur;
    }

    public int getKontrolEdilen() {
        return kontrolEdilen;
    }

    public void setKontrolEdilen(int kontrolEdilen) {
        this.kontrolEdilen = kontrolEdilen;
    }

    public int getSorunlu() {
        return sorunlu;
    }

    public void setSorunlu(int sorunlu) {
        this.sorunlu = sorunlu;
    }
}
