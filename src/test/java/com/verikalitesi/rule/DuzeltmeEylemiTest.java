package com.verikalitesi.rule;

import com.verikalitesi.dto.DuzeltmeEylemi;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Her bulgunun bir eylemi olmalı ve eylem notu boş kalmamalı: rapordaki eylem haritası
 * doğrudan bu alanlardan besleniyor, biri unutulursa kullanıcı boş hücre görür.
 */
class DuzeltmeEylemiTest {

    private DogrulamaSonucu bulgu(Optional<DogrulamaSonucu> sonuc) {
        assertTrue(sonuc.isPresent(), "kural bulgu üretmeliydi");
        return sonuc.get();
    }

    private void eylemNotuDoluOlmali(DogrulamaSonucu sonuc) {
        assertFalse(sonuc.getEylemNotu() == null || sonuc.getEylemNotu().isBlank(),
                "eylem notu boş: " + sonuc.getMesaj());
    }

    @Test
    void boslukBulgusuOtomatikDuzeltilebilirOlmali() {
        DogrulamaSonucu sonuc = bulgu(new BoslukKurali().kontrolEt("1", "Ad", "  bosluklu"));
        assertEquals(DuzeltmeEylemi.OTOMATIK, sonuc.getEylem());
        eylemNotuDoluOlmali(sonuc);

        DogrulamaSonucu ardisik = bulgu(new BoslukKurali().kontrolEt("1", "Ad", "iki  bosluk"));
        assertEquals(DuzeltmeEylemi.OTOMATIK, ardisik.getEylem());
    }

    @Test
    void yerTutucuBulgusuOtomatikDuzeltilebilirOlmali() {
        PlaceholderKurali kural = new PlaceholderKurali(List.of("n/a"));

        DogrulamaSonucu listede = bulgu(kural.kontrolEt("1", "E-posta", "n/a"));
        assertEquals(DuzeltmeEylemi.OTOMATIK, listede.getEylem());
        eylemNotuDoluOlmali(listede);

        DogrulamaSonucu noktalama = bulgu(kural.kontrolEt("1", "Web", "///"));
        assertEquals(DuzeltmeEylemi.OTOMATIK, noktalama.getEylem());
        eylemNotuDoluOlmali(noktalama);
    }

    @Test
    void eksikAlanIncelemeGerektirmeli() {
        // Eksik veri uydurulmaz; kaynaktan tamamlanmasi gerekir.
        DogrulamaSonucu sonuc = bulgu(new EksikAlanKurali().kontrolEt("1", "Ad", ""));
        assertEquals(DuzeltmeEylemi.INCELEME, sonuc.getEylem());
        eylemNotuDoluOlmali(sonuc);
    }

    @Test
    void bozukEpostaIncelemeGerektirmeli() {
        DogrulamaSonucu sonuc = bulgu(new EmailRule().kontrolEt("1", "E-posta", "bozuk-adres"));
        assertEquals(DuzeltmeEylemi.INCELEME, sonuc.getEylem());
        eylemNotuDoluOlmali(sonuc);
    }

    @Test
    void cokluEpostaIncelemeGerektirmeli() {
        DogrulamaSonucu sonuc = bulgu(new EmailRule().kontrolEt("1", "E-posta", "a@b.com, c@d.com"));
        assertEquals(DuzeltmeEylemi.INCELEME, sonuc.getEylem());
        eylemNotuDoluOlmali(sonuc);
    }

    @Test
    void protokolEksigiBilincliOlarakDuzeltilmemeli() {
        // Basina http:// eklemek eksik veriyi uydurmak olurdu; adres https de olabilir.
        DogrulamaSonucu sonuc = bulgu(new WebSitesiKurali().kontrolEt("1", "Web", "firma.com"));
        assertEquals(DuzeltmeEylemi.DUZELTILMIYOR, sonuc.getEylem());
        eylemNotuDoluOlmali(sonuc);
    }

    @Test
    void webdekiEpostaIncelemeGerektirmeli() {
        DogrulamaSonucu sonuc = bulgu(new WebSitesiKurali().kontrolEt("1", "Web", "info@firma.com"));
        assertEquals(DuzeltmeEylemi.INCELEME, sonuc.getEylem());
        eylemNotuDoluOlmali(sonuc);
    }

    @Test
    void telefonBulgulariIncelemeGerektirmeli() {
        TelefonKurali kural = new TelefonKurali("IQ");

        DogrulamaSonucu coklu = bulgu(kural.kontrolEt("1", "Telefon", "7177938, 7177546"));
        assertEquals(DuzeltmeEylemi.INCELEME, coklu.getEylem());
        eylemNotuDoluOlmali(coklu);

        DogrulamaSonucu ayristirilamadi = bulgu(kural.kontrolEt("1", "Telefon", "abc"));
        assertEquals(DuzeltmeEylemi.INCELEME, ayristirilamadi.getEylem());
        eylemNotuDoluOlmali(ayristirilamadi);
    }

    @Test
    void kimlikNoSapmasiBilincliOlarakDuzeltilmemeli() {
        KimlikNoKurali kural = new KimlikNoKurali(0.8);
        List<String> degerler = List.of("12345", "12345", "12345", "12345", "12345-6");

        List<DogrulamaSonucu> bulgular = kural.kaliptanSapanlariBul(
                kural.kolonuCozumle("nit", degerler), "Kimlik No",
                List.of("a", "b", "c", "d", "e"), degerler);

        assertEquals(1, bulgular.size());
        assertEquals(DuzeltmeEylemi.DUZELTILMIYOR, bulgular.get(0).getEylem());
        eylemNotuDoluOlmali(bulgular.get(0));
    }
}
