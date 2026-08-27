package com.verikalitesi.dto;

/**
 * Metin standartlaştırma seçenekleri.
 *
 * <p>Dönüşüm veritabanında yapılıyor ve sonuç <b>veritabanının dil ayarına</b> bağlı.
 * Ayar Türkçe olmadığı sürece Türkçe metin bozulur:
 *
 * <pre>
 *   UPPER('ışık ipek')    -&gt;  ıŞıK IPEK      (beklenen: IŞIK İPEK)
 *   INITCAP('ışık ipek')  -&gt;  ışık Ipek      (beklenen: Işık İpek)
 * </pre>
 *
 * <p>Noktasız {@code ı} harfinin karşılığı bulunamadığı için olduğu gibi kalıyor.
 * Arapçada büyük/küçük harf kavramı yok, dönüşüm hiçbir şeyi değiştirmiyor; Kiril ve
 * Latin alfabelerinde doğru çalışıyor.
 *
 * <p>Bu yüzden varsayılan {@link #YOK}: kullanıcı bilerek açmalı ve ekranda uyarıyı
 * görmeli. Geri alma olmadığı için sessizce bozmak kabul edilemez.
 */
public enum HarfDonusumu {

    YOK("Değiştirme", null),
    KUCUK("Tümü küçük harf", "LOWER"),
    BUYUK("Tümü BÜYÜK HARF", "UPPER"),
    ILK_HARF("İlk Harfler Büyük", "INITCAP");

    private final String etiket;
    private final String sqlFonksiyonu;

    HarfDonusumu(String etiket, String sqlFonksiyonu) {
        this.etiket = etiket;
        this.sqlFonksiyonu = sqlFonksiyonu;
    }

    public boolean isUygulanacakMi() {
        return sqlFonksiyonu != null;
    }

    public String getEtiket() {
        return etiket;
    }

    public String getSqlFonksiyonu() {
        return sqlFonksiyonu;
    }
}
