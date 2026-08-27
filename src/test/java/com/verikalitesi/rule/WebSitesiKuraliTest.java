package com.verikalitesi.rule;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebSitesiKuraliTest {

    private final WebSitesiKurali kural = new WebSitesiKurali();

    @Test
    void gecerliAdresIcinHataUretilmemeli() {
        Optional<DogrulamaSonucu> sonuc = kural.kontrolEt("1", "Web Sitesi: ", "http://www.example.com");
        assertTrue(sonuc.isEmpty());
    }

    @Test
    void bosDegerIcinHataUretilmemeli() {
        assertTrue(kural.kontrolEt("1", "Web Sitesi: ", null).isEmpty());
        assertTrue(kural.kontrolEt("1", "Web Sitesi: ", "   ").isEmpty());
    }

    @Test
    void protokolYoksaProtokolEksikMesajiUretilmeli() {
        Optional<DogrulamaSonucu> sonuc = kural.kontrolEt("1", "Web Sitesi: ", "www.mishraq.industry.gov.iq");
        assertTrue(sonuc.isPresent());
        assertEquals("Protokol eksik, adresin başında http:// yok", sonuc.get().getMesaj());
    }

    @Test
    void epostaAdresiIcinAyriMesajUretilmeli() {
        Optional<DogrulamaSonucu> sonuc = kural.kontrolEt("1", "Web Sitesi: ", "info@alzawraaelectric.com");
        assertTrue(sonuc.isPresent());
        assertEquals("Bu alan e-posta adresi içeriyor, web sitesi değil", sonuc.get().getMesaj());
    }

    @Test
    void birdenFazlaAdresVarsaAyriMesajUretilmeli() {
        Optional<DogrulamaSonucu> sonuc = kural.kontrolEt("1", "Web Sitesi: ", "www.birinci.com, www.ikinci.com");
        assertTrue(sonuc.isPresent());
        assertEquals("Hücrede birden fazla web adresi var", sonuc.get().getMesaj());
    }

    @Test
    void alanAdiYoksaFirmaAdiMesajiUretilmeli() {
        Optional<DogrulamaSonucu> sonuc = kural.kontrolEt("1", "Web Sitesi: ", "SALEH HAMAD JABER AL-SHARQI");
        assertTrue(sonuc.isPresent());
        assertEquals("Geçerli bir alan adı içermiyor, firma adı veya serbest metin olabilir", sonuc.get().getMesaj());
    }

    @Test
    void gercektenBozukAdresIcinGenelMesajUretilmeli() {
        Optional<DogrulamaSonucu> sonuc = kural.kontrolEt("1", "Web Sitesi: ", "http://WWW.KALDARIMARINE .COM");
        assertTrue(sonuc.isPresent());
        assertEquals("Geçersiz web adresi formatı", sonuc.get().getMesaj());
    }

    @Test
    void raporaHamDegerYazilmali() {
        Optional<DogrulamaSonucu> sonuc = kural.kontrolEt("1", "Web Sitesi: ", "  www.example.com  ");
        assertTrue(sonuc.isPresent());
        assertEquals("  www.example.com  ", sonuc.get().getDeger());
    }
}
