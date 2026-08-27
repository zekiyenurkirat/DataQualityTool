package com.verikalitesi.dto;

/**
 * Aynı kimliğe sahip satırların tek satırda özeti.
 *
 * <p>Kopya kayıtları çift çift göstermek kombinasyon sayısını üretir, bilgiyi değil:
 * 135 kez tekrarlanan bir kayıt 9.045 çift demektir ama söylediği tek şey vardır --
 * "bu kayıt 135 kez girilmiş". Grup bu tek şeyi taşır.
 */
public class KopyaGrubu {

    private String kimlik;

    /** Grubun ekranda tanınmasını sağlayan değer; genelde firma adı. */
    private String ornekDeger;

    private int kayitSayisi;

    /** Bu gruptan silinebilecek satır sayısı: bir tanesi kalır, gerisi fazlalıktır. */
    public int getFazlalik() {
        return kayitSayisi > 0 ? kayitSayisi - 1 : 0;
    }

    public void arttir() {
        this.kayitSayisi++;
    }

    public String getKimlik() {
        return kimlik;
    }

    public void setKimlik(String kimlik) {
        this.kimlik = kimlik;
    }

    public String getOrnekDeger() {
        return ornekDeger;
    }

    public void setOrnekDeger(String ornekDeger) {
        this.ornekDeger = ornekDeger;
    }

    public int getKayitSayisi() {
        return kayitSayisi;
    }

    public void setKayitSayisi(int kayitSayisi) {
        this.kayitSayisi = kayitSayisi;
    }
}
