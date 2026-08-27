package com.verikalitesi.temizleme;

/**
 * Tek bir hücrenin değerini onarmaya çalışan kural.
 *
 * <p>Onarıcılar, SQL ile toplu yapılan metin temizliğinden ayrı bir kategoridir: sonucun
 * <b>doğru olup olmadığını kontrol edebilmek</b> için değeri satır satır Java tarafında
 * işlerler. Telefon numarasının ayrıştırılıp yeniden biçimlendirilmesi ya da bozuk bir
 * e-postanın onarılıp yeniden doğrulanması SQL ifadesiyle yapılamaz.
 *
 * <p>Yeni bir onarıcı eklemek için bu arayüzü uygulamak yeterlidir; önizleme, sayım ve
 * güncelleme akışı değişmez.
 */
public interface DegerOnarici {

    /**
     * @param hamDeger hücredeki mevcut değer
     * @return üç durumdan biri: değişti, gerek yok, onarılamadı
     */
    OnarimCiktisi onar(String hamDeger);

    /** Önizleme ve rapor ekranlarında bu onarıcının ne yaptığını anlatan kısa ad. */
    String adi();
}
