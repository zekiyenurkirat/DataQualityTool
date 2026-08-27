package com.verikalitesi.rule;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlaceholderKuraliTest {

    private final PlaceholderKurali kural =
            new PlaceholderKurali(List.of("n/a", "none", "null", "nil", "yok", "bilinmiyor"));

    @Test
    void listedekiDegerYakalanmali() {
        Optional<DogrulamaSonucu> sonuc = kural.kontrolEt("1", "E-posta: ", "N/A");
        assertTrue(sonuc.isPresent());
        assertEquals("Alan dolu görünüyor ancak gerçek bir bilgi içermiyor", sonuc.get().getMesaj());
    }

    @Test
    void bosluklarVeBuyukHarfYakalanmayiEngellememeli() {
        assertTrue(kural.kontrolEt("1", "E-posta: ", "  none  ").isPresent());
        assertTrue(kural.kontrolEt("1", "E-posta: ", "NONE").isPresent());
        assertTrue(kural.kontrolEt("1", "E-posta: ", "  Bilinmiyor ").isPresent());
    }

    @Test
    void buyukIHarfiTurkceDildeSorunCikarmamali() {
        assertTrue(kural.kontrolEt("1", "E-posta: ", "NIL").isPresent());
    }

    @Test
    void sadeceNoktalamadanOlusanDegerYakalanmali() {
        assertTrue(kural.kontrolEt("1", "Adres: ", "-").isPresent());
        assertTrue(kural.kontrolEt("1", "Adres: ", "--").isPresent());
        assertTrue(kural.kontrolEt("1", "Adres: ", "- - -").isPresent());
        assertTrue(kural.kontrolEt("1", "Web Sitesi: ", "//").isPresent());
        assertTrue(kural.kontrolEt("1", "E-posta: ", "----").isPresent());

        assertEquals("Alan yalnızca noktalama işareti içeriyor",
                kural.kontrolEt("1", "Adres: ", "-").get().getMesaj());
    }

    @Test
    void latinDisiAlfabelerYerTutucuSanilmamali() {
        assertTrue(kural.kontrolEt("1", "Firma Adı: ", "شركة فل الياسمين للتجارة").isEmpty());
        assertTrue(kural.kontrolEt("1", "Firma Adı: ", "жоопкерчилиги чектелген коому").isEmpty());
        assertTrue(kural.kontrolEt("1", "Firma Adı: ", "ห้างหุ้นส่วนจำกัด").isEmpty());
    }

    @Test
    void gecerliDegerYakalanmamali() {
        assertTrue(kural.kontrolEt("1", "Firma Adı: ", "ABC Ltd.").isEmpty());
        assertTrue(kural.kontrolEt("1", "E-posta: ", "info@example.com").isEmpty());
        assertTrue(kural.kontrolEt("1", "Adres: ", "Doha Street 10").isEmpty());
    }

    @Test
    void bosDegerBuKuralinIsiDegil() {
        assertTrue(kural.kontrolEt("1", "E-posta: ", null).isEmpty());
        assertTrue(kural.kontrolEt("1", "E-posta: ", "").isEmpty());
        assertTrue(kural.kontrolEt("1", "E-posta: ", "   ").isEmpty());
    }

    @Test
    void listeBosOlsaBileNoktalamaKontrolCalismali() {
        PlaceholderKurali listesizKural = new PlaceholderKurali(List.of());
        assertTrue(listesizKural.kontrolEt("1", "Adres: ", "-").isPresent());
        assertTrue(listesizKural.kontrolEt("1", "E-posta: ", "N/A").isEmpty());
    }

    @Test
    void raporaHamDegerYazilmali() {
        Optional<DogrulamaSonucu> sonuc = kural.kontrolEt("1", "E-posta: ", "  N/A  ");
        assertEquals("  N/A  ", sonuc.get().getDeger());
    }
}
