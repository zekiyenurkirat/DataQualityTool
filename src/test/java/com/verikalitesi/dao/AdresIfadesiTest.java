package com.verikalitesi.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Adres karşılaştırma ifadesinin ürettiği SQL'i sabitler.
 *
 * <p>Adres parçaları her zaman metin tipinde değildir: kapı ve daire numarası
 * sayısal kolonda saklanabiliyor. {@code COALESCE(a.kapi_no, '')} yazıldığında
 * PostgreSQL boş metni sayıya çevirmeye çalışır ve sorgu
 * {@code invalid input syntax for type bigint: ""} ile düşer. Bu yüzden her
 * kolon açıkça metne çevriliyor.
 *
 * <p>Test SQL çalıştırmıyor, üretilen ifadeyi denetliyor; böylece veritabanı
 * gerektirmeden bu hata sınıfının geri gelmesi engelleniyor.
 */
class AdresIfadesiTest {

    private final JdbcBenzerFirmaDao dao = new JdbcBenzerFirmaDao();

    @Test
    @DisplayName("Her adres kolonu ::text ile metne çevriliyor")
    void herKolonMetneCevriliyor() {
        String ifade = dao.adresIfadesi("a", List.of("street", "house_number"));

        assertTrue(ifade.contains("a.street::text"), ifade);
        assertTrue(ifade.contains("a.house_number::text"), ifade);
    }

    @Test
    @DisplayName("Tek kolonda da cast uygulanıyor")
    void tekKolondaDaCastVar() {
        assertEquals("NULLIF(TRIM(COALESCE(a.adres::text, '')), '')",
                dao.adresIfadesi("a", List.of("adres")));
    }

    @Test
    @DisplayName("Kolonlar boşlukla birleştiriliyor, sonuç NULLIF ile sarılıyor")
    void kolonlarBoslukIleBirlesiyor() {
        String ifade = dao.adresIfadesi("b", List.of("sehir", "sokak"));

        assertEquals("NULLIF(TRIM(COALESCE(b.sehir::text, '') || ' ' || "
                + "COALESCE(b.sokak::text, '')), '')", ifade);
    }

    /**
     * NULLIF sarmalayıcısı kritik: hepsi boşsa sonuç NULL olmalı ki
     * {@code similarity} de NULL dönsün ve raporda "—" görünsün. Boş metin
     * kalsaydı similarity 0 döner, ekranda "hiç benzemiyor" diye okunurdu --
     * oysa doğru bilgi "adres yok, bilmiyoruz".
     */
    @Test
    @DisplayName("İfade NULLIF ile sarılı kalmalı")
    void nullifKorunuyor() {
        String ifade = dao.adresIfadesi("a", List.of("x", "y", "z"));

        assertTrue(ifade.startsWith("NULLIF(TRIM("), ifade);
        assertTrue(ifade.endsWith("), '')"), ifade);
    }
}
