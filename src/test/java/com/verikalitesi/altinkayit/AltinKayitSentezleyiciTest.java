package com.verikalitesi.altinkayit;

import com.verikalitesi.dto.AlanEslestirmesi;
import com.verikalitesi.dto.BenzerFirmaCifti;
import com.verikalitesi.rule.PlaceholderKurali;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AltinKayitSentezleyiciTest {

    private Map<String, String> alanlar(String... anahtarDeger) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < anahtarDeger.length; i += 2) {
            map.put(anahtarDeger[i], anahtarDeger[i + 1]);
        }
        return map;
    }

    private KopyaKumesi kume(Map<String, String>... kayitlar) {
        List<BenzerFirmaCifti> ciftler = new java.util.ArrayList<>();
        for (int i = 1; i < kayitlar.length; i++) {
            BenzerFirmaCifti cift = new BenzerFirmaCifti();
            cift.setId_1("K" + (i - 1));
            cift.setId_2("K" + i);
            cift.setAlanlar1(kayitlar[i - 1]);
            cift.setAlanlar2(kayitlar[i]);
            cift.setFirma1("kayit" + (i - 1));
            cift.setFirma2("kayit" + i);
            ciftler.add(cift);
        }
        return new KumeBulucu().kumeleriBul(ciftler).get(0);
    }

    private AltinKayitSentezleyici sentezleyici(AlanEslestirmesi eslestirme) {
        return new AltinKayitSentezleyici(
                // Liste application.properties'tekiyle aynı olmalı; testin kendi
                // kısaltılmış listesi gerçek davranışı yansıtmazdı.
                new PlaceholderKurali(List.of("n/a", "none", "yok", "bilinmiyor")),
                eslestirme, "E164");
    }

    private AlanSecenegi secenek(List<AlanSecenegi> secenekler, String kolon) {
        return secenekler.stream()
                .filter(s -> s.getKolonAdi().equals(kolon))
                .findFirst().orElseThrow();
    }

    // ------------------------------------------------------------ N kayıt

    @Test
    @DisplayName("Üç kayıtlık küme için her alanda üç seçenek üretilir")
    void ucKayitUcSecenek() {
        KopyaKumesi k = kume(
                alanlar("ad", "Alfa"),
                alanlar("ad", "Alfa Ltd"),
                alanlar("ad", "Alfa Limited"));

        List<AlanSecenegi> secenekler = sentezleyici(null).secenekleriUret(k);

        assertEquals(3, k.getKayitSayisi());
        assertEquals(1, secenekler.size());
        assertEquals(3, secenek(secenekler, "ad").getDegerler().size());
    }

    /**
     * Alan seviyesinde birleştirmenin bütün sebebi bu: kayıt seviyesinde
     * birleştirmede bir satır kazanır ve ötekinin taşıdığı tek bilgi kaybolurdu.
     */
    @Test
    @DisplayName("Farklı kayıtlardaki farklı alanlar tek ana kayıtta toplanır")
    void alanlarFarkliKayitlardanToplanir() {
        KopyaKumesi k = kume(
                alanlar("ad", "Alfa", "telefon", "07701234567", "eposta", null),
                alanlar("ad", "Alfa", "telefon", null, "eposta", "info@alfa.iq"));

        AltinKayitSentezleyici s = sentezleyici(null);
        List<AlanSecenegi> secenekler = s.secenekleriUret(k);
        Map<String, String> altin = s.altinKayitKur(secenekler, Map.of());

        assertEquals("07701234567", altin.get("telefon"));
        assertEquals("info@alfa.iq", altin.get("eposta"));
    }

    @Test
    @DisplayName("Kolon kümeleri farklıysa hepsi seçeneklere girer")
    void farkliKolonlarBirlesir() {
        KopyaKumesi k = kume(
                alanlar("ad", "Alfa"),
                alanlar("ad", "Alfa", "website", "alfa.iq"));

        List<AlanSecenegi> secenekler = sentezleyici(null).secenekleriUret(k);
        assertEquals(2, secenekler.size());
        assertTrue(secenek(secenekler, "website").getDegerler().get(0).isBosMu());
    }

    // ------------------------------------------------------- onarım + şeffaflık

    @Test
    @DisplayName("Boşluk kiri temizlenir ve onarıldı olarak işaretlenir")
    void bosluklarOnarilirVeIsaretlenir() {
        KopyaKumesi k = kume(
                alanlar("ad", "  Alfa   Trade  LLC "),
                alanlar("ad", "Beta"));

        AlanSecenegi ad = secenek(sentezleyici(null).secenekleriUret(k), "ad");
        AlanSecenegi.Deger ilk = ad.getDegerler().get(0);

        assertTrue(ilk.isOnarildi());
        assertEquals("Alfa Trade LLC", ilk.getTemizDeger());
        assertEquals("  Alfa   Trade  LLC ", ilk.getHamDeger());
        assertEquals("Boşluk düzenleme", ilk.getOnariciAdi());
    }

    @Test
    @DisplayName("Zaten temiz değer onarıldı sayılmaz")
    void temizDegerIsaretlenmez() {
        KopyaKumesi k = kume(alanlar("ad", "Alfa Trade LLC"), alanlar("ad", "Beta"));
        assertFalse(secenek(sentezleyici(null).secenekleriUret(k), "ad")
                .getDegerler().get(0).isOnarildi());
    }

    @Test
    @DisplayName("Web adresi kolonunda URL onarıcısı çalışır, yola dokunmaz")
    void webKolonuUrlOnaricisindanGecer() {
        AlanEslestirmesi eslestirme = new AlanEslestirmesi();
        eslestirme.setWebSitesiKolonu("website");

        KopyaKumesi k = kume(
                alanlar("website", "HTTP://WWW.ALFA.IQ/MyPage"),
                alanlar("website", "alfa.iq"));

        AlanSecenegi.Deger ilk = secenek(sentezleyici(eslestirme).secenekleriUret(k), "website")
                .getDegerler().get(0);

        assertTrue(ilk.isOnarildi());
        // Şema ve alan adı küçüldü, yol olduğu gibi kaldı.
        assertTrue(ilk.getTemizDeger().startsWith("http://www.alfa.iq"));
        assertTrue(ilk.getTemizDeger().endsWith("MyPage"));
    }

    @Test
    @DisplayName("Kolonda onarım varsa başlık bunu bildirir")
    void kolonOnarimBayragi() {
        KopyaKumesi k = kume(alanlar("ad", "  Alfa "), alanlar("ad", "Beta"));
        assertTrue(secenek(sentezleyici(null).secenekleriUret(k), "ad").isOnarimVarMi());
    }

    // ------------------------------------------------------------ öneri

    @Test
    @DisplayName("Öneri, anlamlı ve en uzun değeri işaret eder")
    void oneriEnUzunAnlamliDeger() {
        KopyaKumesi k = kume(
                alanlar("ad", "Alfa"),
                alanlar("ad", "Alfa Ticaret Limited"));

        AlanSecenegi ad = secenek(sentezleyici(null).secenekleriUret(k), "ad");
        assertEquals(1, ad.getOnerilenIndeks());
    }

    /**
     * Yer tutucu, uzun olsa bile önerilmemeli; "bilinmiyor" bir bilgi değildir.
     */
    @Test
    @DisplayName("Yer tutucu değer önerilmez")
    void yerTutucuOnerilmez() {
        KopyaKumesi k = kume(
                alanlar("adres", "bilinmiyor"),
                alanlar("adres", "Basra"));

        assertEquals(1, secenek(sentezleyici(null).secenekleriUret(k), "adres").getOnerilenIndeks());
    }

    @Test
    @DisplayName("Hiçbir değer anlamlı değilse ilk seçenek önerilir, çökmez")
    void hepsiBosOlsaBileCokmez() {
        KopyaKumesi k = kume(alanlar("adres", null), alanlar("adres", "  "));
        AlanSecenegi adres = secenek(sentezleyici(null).secenekleriUret(k), "adres");
        assertEquals(0, adres.getOnerilenIndeks());
    }

    @Test
    @DisplayName("Kullanıcı seçimi aracın önerisini ezer")
    void kullaniciSecimiOneriyiEzer() {
        KopyaKumesi k = kume(
                alanlar("ad", "Alfa"),
                alanlar("ad", "Alfa Ticaret Limited"));

        AltinKayitSentezleyici s = sentezleyici(null);
        List<AlanSecenegi> secenekler = s.secenekleriUret(k);

        assertEquals("Alfa", s.altinKayitKur(secenekler, Map.of("ad", 0)).get("ad"));
        assertEquals("Alfa Ticaret Limited", s.altinKayitKur(secenekler, Map.of()).get("ad"));
    }

    @Test
    @DisplayName("Geçersiz indeks gelirse aracın önerisine düşülür")
    void gecersizIndeksOneriyeDuser() {
        KopyaKumesi k = kume(alanlar("ad", "Alfa"), alanlar("ad", "Alfa Ticaret"));
        AltinKayitSentezleyici s = sentezleyici(null);
        List<AlanSecenegi> secenekler = s.secenekleriUret(k);

        assertEquals("Alfa Ticaret", s.altinKayitKur(secenekler, Map.of("ad", 99)).get("ad"));
    }

    @Test
    @DisplayName("Bütün kayıtlarda aynı olan kolonda fark yok işaretlenir")
    void farkYokBayragi() {
        KopyaKumesi k = kume(alanlar("ad", "Alfa"), alanlar("ad", "Alfa"));
        assertFalse(secenek(sentezleyici(null).secenekleriUret(k), "ad").isFarkVarMi());
    }
}
