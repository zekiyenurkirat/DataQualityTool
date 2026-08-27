package com.verikalitesi.rule;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TelefonKuraliTest {

    private final TelefonKurali irakKurali = new TelefonKurali("IQ");
    private final TelefonKurali kazakistanKurali = new TelefonKurali("KZ");

    @Test
    void gecerliNumaraIcinHataUretilmemeli() {
        assertTrue(irakKurali.kontrolEt("1", "Telefon: ", "07740905059").isEmpty());
        assertTrue(irakKurali.kontrolEt("1", "Telefon: ", "+9647740905059").isEmpty());
    }

    @Test
    void bosDegerIcinHataUretilmemeli() {
        assertTrue(irakKurali.kontrolEt("1", "Telefon: ", null).isEmpty());
        assertTrue(irakKurali.kontrolEt("1", "Telefon: ", "   ").isEmpty());
    }

    @Test
    void virgulleAyrilmisIkiNumaraYakalanmali() {
        Optional<DogrulamaSonucu> sonuc = irakKurali.kontrolEt("1", "Telefon: ", "7177938, 7177546");
        assertTrue(sonuc.isPresent());
        assertEquals("Hücrede birden fazla telefon numarası var", sonuc.get().getMesaj());
    }

    @Test
    void bosluklarArasindakiTireIleAyrilmisIkiNumaraYakalanmali() {
        Optional<DogrulamaSonucu> sonuc = irakKurali.kontrolEt("1", "Telefon: ",
                "009647729963111 - 009647827836303");
        assertTrue(sonuc.isPresent());
        assertEquals("Hücrede birden fazla telefon numarası var", sonuc.get().getMesaj());
    }

    @Test
    void numaraIcindekiTireCokluSayilmamali() {
        assertTrue(kazakistanKurali.kontrolEt("1", "Telefon: ", "+7 (701) 925-59-66").isEmpty());
        assertTrue(kazakistanKurali.kontrolEt("1", "Telefon: ", "8 (7152) 36 16 16").isEmpty());
    }

    @Test
    void bosluklaAyrilmisTekNumaraCokluSayilmamali() {
        Optional<DogrulamaSonucu> sonuc = irakKurali.kontrolEt("1", "Telefon: ", "0774 - 090 - 5059");
        if (sonuc.isPresent()) {
            assertEquals("Geçersiz telefon formatı.", sonuc.get().getMesaj());
        }
    }

    @Test
    void gecersizNumaraIcinFormatMesajiUretilmeli() {
        Optional<DogrulamaSonucu> sonuc = irakKurali.kontrolEt("1", "Telefon: ", "123");
        assertTrue(sonuc.isPresent());
        assertEquals("Geçersiz telefon formatı.", sonuc.get().getMesaj());
    }

    @Test
    void raporaHamDegerYazilmali() {
        Optional<DogrulamaSonucu> sonuc = irakKurali.kontrolEt("1", "Telefon: ", "7177938, 7177546");
        assertEquals("7177938, 7177546", sonuc.get().getDeger());
        assertEquals("1", sonuc.get().getSatirNo());
        assertEquals("Telefon: ", sonuc.get().getAlanAdi());
    }
}
