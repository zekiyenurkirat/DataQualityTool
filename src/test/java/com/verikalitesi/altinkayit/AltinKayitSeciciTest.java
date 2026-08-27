package com.verikalitesi.altinkayit;

import com.verikalitesi.altinkayit.AltinKayitKarari.Kural;
import com.verikalitesi.rule.PlaceholderKurali;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AltinKayitSeciciTest {

    private AltinKayitSecici secici;

    @BeforeEach
    void kur() {
        secici = new AltinKayitSecici(new PlaceholderKurali(
                List.of("n/a", "none", "null", "yok", "bilinmiyor")));
    }

    private Map<String, String> alanlar(String... anahtarDeger) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < anahtarDeger.length; i += 2) {
            map.put(anahtarDeger[i], anahtarDeger[i + 1]);
        }
        return map;
    }

    // ---------------------------------------------------------------- 1. kural

    @Test
    @DisplayName("Daha çok alanı dolu olan kayıt ana kayıt seçilir")
    void enDoluKayitKazanir() {
        AltinKayitKarari karar = secici.sec(
                "A", alanlar("ad", "Alfa Ltd", "telefon", "07701234567", "eposta", "a@alfa.iq"),
                "B", alanlar("ad", "Alfa Ltd", "telefon", null, "eposta", null));

        assertEquals("A", karar.getKazananKimlik());
        assertEquals("B", karar.getKaybedenKimlik());
        assertEquals(Kural.DOLULUK, karar.getKural());
        assertTrue(karar.isGuvenilirKarar());
        assertEquals(3, karar.getBirinciDoluluk());
        assertEquals(1, karar.getIkinciDoluluk());
    }

    @Test
    @DisplayName("İkinci kayıt daha doluysa o kazanır")
    void ikinciKayitDaKazanabilir() {
        AltinKayitKarari karar = secici.sec(
                "A", alanlar("ad", "Alfa", "telefon", ""),
                "B", alanlar("ad", "Alfa", "telefon", "07701234567"));

        assertEquals("B", karar.getKazananKimlik());
        assertEquals(Kural.DOLULUK, karar.getKural());
    }

    /**
     * Kuralın asıl değeri burada. Ham NULL sayan bir uygulama ikinci kaydı
     * "daha dolu" sayardı; oysa o alanlarda bilgi değil yer tutucu var.
     */
    @Test
    @DisplayName("Yer tutucular dolu sayılmaz, boş kayıt zengin görünmez")
    void yerTutucularDoluSayilmaz() {
        AltinKayitKarari karar = secici.sec(
                "A", alanlar("ad", "Alfa Ltd", "adres", "Bağdat Kerrade 12", "telefon", null),
                "B", alanlar("ad", "Alfa Ltd", "adres", "-", "telefon", "N/A"));

        assertEquals("A", karar.getKazananKimlik());
        assertEquals(2, karar.getBirinciDoluluk());
        assertEquals(1, karar.getIkinciDoluluk());
    }

    @Test
    @DisplayName("Sadece boşluktan ibaret değer dolu sayılmaz")
    void bosluktanIbaretDegerDoluSayilmaz() {
        AltinKayitKarari karar = secici.sec(
                "A", alanlar("ad", "Alfa", "adres", "   "),
                "B", alanlar("ad", "Alfa", "adres", "Basra"));

        assertEquals("B", karar.getKazananKimlik());
    }

    /**
     * İki kaydın kolon kümesi farklı olabilir. Paydayı tek tarafa göre alsaydık,
     * yalnızca bir tarafta bulunan kolon hiç sayılmaz ve o taraf haksız kazanırdı.
     */
    @Test
    @DisplayName("Kolon kümeleri farklıysa payda ikisinin birleşimidir")
    void farkliKolonKumeleriBirlestirilir() {
        AltinKayitKarari karar = secici.sec(
                "A", alanlar("ad", "Alfa"),
                "B", alanlar("ad", "Alfa", "website", "alfa.iq"));

        assertEquals(2, karar.getToplamAlan());
        assertEquals(1, karar.getBirinciDoluluk());
        assertEquals(2, karar.getIkinciDoluluk());
        assertEquals("B", karar.getKazananKimlik());
    }

    // ---------------------------------------------------------------- 2. kural

    @Test
    @DisplayName("Doluluk eşitse karakter uzunluğu karar verir")
    void dolulukEsitseUzunlukKararVerir() {
        AltinKayitKarari karar = secici.sec(
                "A", alanlar("ad", "Alfa Ticaret Limited Şirketi"),
                "B", alanlar("ad", "Alfa Tic."));

        assertEquals("A", karar.getKazananKimlik());
        assertEquals(Kural.KARAKTER_UZUNLUGU, karar.getKural());
        assertEquals(karar.getBirinciDoluluk(), karar.getIkinciDoluluk());
    }

    /**
     * Uzunluk kırpılmış değer üzerinden sayılmalı. Sayılmasaydı baştaki ve
     * sondaki boşluklar bilgi gibi davranır, kirli kayıt temiz kayda karşı
     * haksız avantaj kazanırdı.
     */
    @Test
    @DisplayName("Baş ve sondaki boşluklar uzunluğa sayılmaz")
    void bosluklarUzunlugaSayilmaz() {
        AltinKayitKarari karar = secici.sec(
                "A", alanlar("ad", "      Alfa      "),
                "B", alanlar("ad", "Alfa Ltd"));

        assertEquals("B", karar.getKazananKimlik());
        assertEquals(Kural.KARAKTER_UZUNLUGU, karar.getKural());
    }

    /**
     * Zayıf kuralla verilen karar güvenilir işaretlenmemeli; ekran bu bayrağa
     * bakıp kullanıcıya diğer kaydı seçme imkânı sunuyor.
     */
    @Test
    @DisplayName("Karakter uzunluğuyla verilen karar güvenilir sayılmaz")
    void uzunlukKarariGuvenilirSayilmaz() {
        AltinKayitKarari karar = secici.sec(
                "A", alanlar("ad", "Alfa Ticaret"),
                "B", alanlar("ad", "Alfa"));

        assertFalse(karar.isGuvenilirKarar());
    }

    // ---------------------------------------------------------------- 3. kural

    @Test
    @DisplayName("Doluluk ve uzunluk eşitse kimlik sırası karar verir")
    void herSeyEsitseKimlikSirasiKararVerir() {
        AltinKayitKarari karar = secici.sec(
                "B-KIMLIK", alanlar("ad", "Alfa"),
                "A-KIMLIK", alanlar("ad", "Beta"));

        assertEquals("A-KIMLIK", karar.getKazananKimlik());
        assertEquals(Kural.KIMLIK_SIRASI, karar.getKural());
        assertFalse(karar.isGuvenilirKarar());
    }

    /**
     * Determinizmin kendisi. Aynı veri üzerinde iki çalıştırma aynı ana kaydı
     * seçmeli; aksi hâlde yeni bir çekim geldiğinde eski kararlarla çelişirdi.
     */
    @Test
    @DisplayName("Parametre sırası değişse de aynı kazanan çıkar")
    void siraDegisinceSonucDegismez() {
        Map<String, String> birinci = alanlar("ad", "Alfa");
        Map<String, String> ikinci = alanlar("ad", "Beta");

        AltinKayitKarari duz = secici.sec("A", birinci, "B", ikinci);
        AltinKayitKarari ters = secici.sec("B", ikinci, "A", birinci);

        assertEquals(duz.getKazananKimlik(), ters.getKazananKimlik());
        assertEquals("A", duz.getKazananKimlik());
    }

    // ---------------------------------------------------------------- genel

    @Test
    @DisplayName("Alan haritası null gelirse çökmez, sıfır dolulukla sayılır")
    void nullHaritaCokmez() {
        AltinKayitKarari karar = secici.sec(
                "A", null,
                "B", alanlar("ad", "Alfa"));

        assertEquals("B", karar.getKazananKimlik());
        assertEquals(0, karar.getBirinciDoluluk());
    }

    @Test
    @DisplayName("Latin dışı alfabeler dolu sayılır")
    void latinDisiAlfabelerDoluSayilir() {
        AltinKayitKarari karar = secici.sec(
                "A", alanlar("ad", "شركة العامة", "adres", "بغداد"),
                "B", alanlar("ad", "شركة العامة", "adres", null));

        assertEquals("A", karar.getKazananKimlik());
        assertEquals(2, karar.getBirinciDoluluk());
    }

    @Test
    @DisplayName("Gerekçe hangi kuralın karar verdiğini ve sayıları taşır")
    void gerekceKuraliVeSayilariTasir() {
        AltinKayitKarari doluluk = secici.sec(
                "A", alanlar("ad", "Alfa", "telefon", "0770"),
                "B", alanlar("ad", "Alfa", "telefon", null));
        assertTrue(doluluk.getGerekce().contains("completeness"));
        assertTrue(doluluk.getGerekce().contains("2/2"));

        AltinKayitKarari uzunluk = secici.sec(
                "A", alanlar("ad", "Alfa Ticaret"),
                "B", alanlar("ad", "Alfa"));
        assertTrue(uzunluk.getGerekce().contains("Karakter uzunluğuna"));

        AltinKayitKarari sira = secici.sec("A", alanlar("ad", "Alfa"), "B", alanlar("ad", "Beta"));
        assertTrue(sira.getGerekce().contains("veriye dayanmıyor"));
    }
}
