package com.verikalitesi.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemizlemeKuraliTest {

    private TemizlemeKurali kural() {
        TemizlemeKurali kural = new TemizlemeKurali();
        kural.setKolonAdi("ad");
        return kural;
    }

    @Test
    void hicbirSecenekYoksaMetinIslemiOlmamali() {
        // Kullanici tum kutulari bosaltirsa SQL kurmaya gerek yok.
        assertFalse(kural().metinIslemiVarMi());
    }

    @Test
    void herSecenekTekBasinaMetinIslemiSaymali() {
        TemizlemeKurali kirp = kural();
        kirp.setBosluklariKirp(true);
        assertTrue(kirp.metinIslemiVarMi());

        TemizlemeKurali daralt = kural();
        daralt.setArdisikBosluklariDaralt(true);
        assertTrue(daralt.metinIslemiVarMi());

        TemizlemeKurali karakter = kural();
        karakter.setHaricTutulacakKarakter("#@!");
        assertTrue(karakter.metinIslemiVarMi());

        TemizlemeKurali harf = kural();
        harf.setHarfDonusumu(HarfDonusumu.BUYUK);
        assertTrue(harf.metinIslemiVarMi());
    }

    @Test
    void bosKarakterAlaniIslemSayilmamali() {
        TemizlemeKurali bosluklu = kural();
        bosluklu.setHaricTutulacakKarakter("   ");
        assertFalse(bosluklu.metinIslemiVarMi(), "yalnizca bosluk yazilmissa silinecek karakter yok demektir");

        TemizlemeKurali bos = kural();
        bos.setHaricTutulacakKarakter("");
        assertFalse(bos.metinIslemiVarMi());
    }

    @Test
    void harfDonusumuNullGelirseYokSayilmali() {
        // Formdan bos deger gelebilir; null atanirsa varsayilan korunmali.
        TemizlemeKurali kural = kural();
        kural.setHarfDonusumu(null);

        assertEquals(HarfDonusumu.YOK, kural.getHarfDonusumu());
        assertFalse(kural.getHarfDonusumu().isUygulanacakMi());
    }

    @Test
    void harfDonusumuSqlFonksiyonlariDogruOlmali() {
        assertEquals("LOWER", HarfDonusumu.KUCUK.getSqlFonksiyonu());
        assertEquals("UPPER", HarfDonusumu.BUYUK.getSqlFonksiyonu());
        assertEquals("INITCAP", HarfDonusumu.ILK_HARF.getSqlFonksiyonu());
        assertFalse(HarfDonusumu.YOK.isUygulanacakMi());
    }

    @Test
    void kirpmaVeDaraltmaBirbirindenBagimsizOlmali() {
        // Ikisi farkli sorunlar: " Ahmet " ile "Ahmet  Ticaret" ayni sey degil.
        TemizlemeKurali yalnizKirp = kural();
        yalnizKirp.setBosluklariKirp(true);

        assertTrue(yalnizKirp.isBosluklariKirp());
        assertFalse(yalnizKirp.isArdisikBosluklariDaralt());
    }
}
