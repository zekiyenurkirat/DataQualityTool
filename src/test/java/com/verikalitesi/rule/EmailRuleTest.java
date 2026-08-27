package com.verikalitesi.rule;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmailRuleTest {

    private final EmailRule kural = new EmailRule();

    @Test
    void gecerliEpostaIcinHataUretilmemeli() {
        assertTrue(kural.kontrolEt("1", "E-posta: ", "info@example.com").isEmpty());
        assertTrue(kural.kontrolEt("1", "E-posta: ", "alsafa44@YAHOO.COM").isEmpty());
    }

    @Test
    void bosDegerIcinHataUretilmemeli() {
        assertTrue(kural.kontrolEt("1", "E-posta: ", null).isEmpty());
        assertTrue(kural.kontrolEt("1", "E-posta: ", "").isEmpty());
        assertTrue(kural.kontrolEt("1", "E-posta: ", "   ").isEmpty());
    }

    @Test
    void noktaliVirgulleAyrilmisCokluEpostaYakalanmali() {
        Optional<DogrulamaSonucu> sonuc = kural.kontrolEt("1", "E-posta: ", "info@a.com; sales@a.com");
        assertTrue(sonuc.isPresent());
        assertEquals("Hücrede birden fazla e-posta adresi var", sonuc.get().getMesaj());
    }

    @Test
    void virgulVeBoslukIleAyrilmisCokluEpostaYakalanmali() {
        assertEquals("Hücrede birden fazla e-posta adresi var",
                kural.kontrolEt("1", "E-posta: ", "info@a.com,sales@a.com").get().getMesaj());

        assertEquals("Hücrede birden fazla e-posta adresi var",
                kural.kontrolEt("1", "E-posta: ", "info@a.com sales@a.com").get().getMesaj());
    }

    @Test
    void ciftAtIsaretiCokluSayilmamali() {
        Optional<DogrulamaSonucu> sonuc = kural.kontrolEt("1", "E-posta: ", "info@@a.com");
        assertTrue(sonuc.isPresent());
        assertEquals("Geçersiz e-posta formatı", sonuc.get().getMesaj());
    }

    @Test
    void icindeBoslukOlanTekEpostaCokluSayilmamali() {
        Optional<DogrulamaSonucu> sonuc = kural.kontrolEt("1", "E-posta: ", "info @example.com");
        assertTrue(sonuc.isPresent());
        assertEquals("Geçersiz e-posta formatı", sonuc.get().getMesaj());
    }

    @Test
    void bozukEpostaIcinGenelMesajUretilmeli() {
        Optional<DogrulamaSonucu> sonuc = kural.kontrolEt("1", "E-posta: ", "bozukmail");
        assertTrue(sonuc.isPresent());
        assertEquals("Geçersiz e-posta formatı", sonuc.get().getMesaj());
    }

    @Test
    void bastaSondaBoslukVarsaGecerliSayilmali() {
        assertTrue(kural.kontrolEt("1", "E-posta: ", "  info@example.com  ").isEmpty());
    }

    @Test
    void raporaHamDegerYazilmali() {
        Optional<DogrulamaSonucu> sonuc = kural.kontrolEt("1", "E-posta: ", "info@a.com; sales@a.com");
        assertEquals("info@a.com; sales@a.com", sonuc.get().getDeger());
        assertEquals("1", sonuc.get().getSatirNo());
        assertEquals("E-posta: ", sonuc.get().getAlanAdi());
    }
}
