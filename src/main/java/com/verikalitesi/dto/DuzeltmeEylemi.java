package com.verikalitesi.dto;

/**
 * Bir bulgunun ne yapılabileceği.
 *
 * <p>Sektörde temizleme iki kovaya ayrılır: deterministik olan otomatik düzeltilir, belirsiz
 * olan insana yönlendirilir. Araç bulguyu üretiyor ama hangi kovaya düştüğünü söylemiyordu;
 * kullanıcı "bunu nasıl düzelteceğim?" sorusuyla baş başa kalıyordu.
 *
 * <p>Üçüncü kova bilinçli bir karardır: bazı bulgular <b>düzeltilebilir ama düzeltilmemeli</b>.
 * Eksik bir e-postayı uydurmak, şube kaydını merkezle birleştirmek geri dönüşü olmayan
 * veri kaybıdır.
 */
public enum DuzeltmeEylemi {

    OTOMATIK("Otomatik düzeltilebilir",
            "Verileri Temizle ekranından tek adımda düzeltilir; sonuç uygulanmadan önizlenir."),

    INCELEME("İnceleme gerekir",
            "Doğru değerin ne olduğu veriden anlaşılamıyor; kararı insan vermeli."),

    DUZELTILMIYOR("Bilinçli olarak düzeltilmiyor",
            "Otomatik düzeltmek veri kaybına yol açardı. Araç yalnızca işaretliyor.");

    private final String baslik;
    private final String aciklama;

    DuzeltmeEylemi(String baslik, String aciklama) {
        this.baslik = baslik;
        this.aciklama = aciklama;
    }

    public String getBaslik() {
        return baslik;
    }

    public String getAciklama() {
        return aciklama;
    }
}
