package com.verikalitesi.anahtar;

import com.verikalitesi.dto.AlanEslestirmesi;
import com.verikalitesi.dto.SatirVerisi;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnahtarZinciriTest {

    private final AnahtarZinciri zincir = AnahtarZinciri.varsayilan();

    private AlanEslestirmesi eslestirme() {
        AlanEslestirmesi eslestirme = new AlanEslestirmesi();
        eslestirme.setFirmaAdiKolonu("isim");
        eslestirme.setAdresKolonlari(List.of("adres"));
        eslestirme.setTelefonKolonu("telefon");
        return eslestirme;
    }

    private SatirVerisi satir(String id, String isim, String adres, String telefon) {
        SatirVerisi satir = new SatirVerisi();
        satir.setId(id);
        Map<String, String> alanlar = new HashMap<>();
        alanlar.put("isim", isim);
        alanlar.put("adres", adres);
        alanlar.put("telefon", telefon);
        satir.setAlanlar(alanlar);
        return satir;
    }

    @Test
    void kimlikKolonuDoluysaOnuKullanmali() {
        Optional<String> anahtar = zincir.anahtarUret(satir("12583", "ABC Ltd", "Doha Street 10", null), eslestirme());
        assertEquals("12583", anahtar.get());
    }

    @Test
    void kimlikKolonuBoslukIceriyorsaTemizlenmeli() {
        assertEquals("12583", zincir.anahtarUret(satir("  12583  ", "ABC", null, null), eslestirme()).get());
    }

    @Test
    void kimlikKolonuBossaHashUretilmeli() {
        Optional<String> anahtar = zincir.anahtarUret(satir(null, "ABC Ltd", "Doha Street 10", null), eslestirme());
        assertTrue(anahtar.isPresent());
        assertTrue(anahtar.get().startsWith("⚙"));
        assertEquals(13, anahtar.get().length());
    }

    @Test
    void ayniIcerikHerZamanAyniAnahtariUretmeli() {
        String birinci = zincir.anahtarUret(satir(null, "ABC Ltd", "Doha Street 10", "07740905059"), eslestirme()).get();
        String ikinci = zincir.anahtarUret(satir(null, "ABC Ltd", "Doha Street 10", "07740905059"), eslestirme()).get();
        assertEquals(birinci, ikinci);
    }

    @Test
    void boslukVeNoktalamaFarklariAnahtariDegistirmemeli() {
        String duz = zincir.anahtarUret(satir(null, "ABC Ltd", "Doha Industrial Area Street 10", null), eslestirme()).get();
        String kirli = zincir.anahtarUret(satir(null, "  ABC   Ltd.  ", "Doha  Industrial Area, Street 10", null), eslestirme()).get();
        assertEquals(duz, kirli);
    }

    @Test
    void buyukKucukHarfFarkiAnahtariDegistirmemeli() {
        String kucuk = zincir.anahtarUret(satir(null, "abc ltd", "doha street 10", null), eslestirme()).get();
        String buyuk = zincir.anahtarUret(satir(null, "ABC LTD", "DOHA STREET 10", null), eslestirme()).get();
        assertEquals(kucuk, buyuk);
    }

    @Test
    void farkliAdresFarkliAnahtarUretmeli() {
        String doha = zincir.anahtarUret(satir(null, "ABC Ltd", "Doha Street 10", null), eslestirme()).get();
        String riyad = zincir.anahtarUret(satir(null, "ABC Ltd", "Riyadh King Road", null), eslestirme()).get();
        assertNotEquals(doha, riyad);
    }

    @Test
    void alanlarinYeriKorunmali() {
        String isimVarAdresYok = zincir.anahtarUret(satir(null, "ABC", null, null), eslestirme()).get();
        String isimYokAdresVar = zincir.anahtarUret(satir(null, null, "ABC", null), eslestirme()).get();
        assertNotEquals(isimVarAdresYok, isimYokAdresVar);
    }

    @Test
    void latinDisiAlfabelerKaybolmamali() {
        String arapca = zincir.anahtarUret(satir(null, "شركة فل الياسمين", null, null), eslestirme()).get();
        String kirgizca = zincir.anahtarUret(satir(null, "жоопкерчилиги чектелген", null, null), eslestirme()).get();
        assertNotEquals(arapca, kirgizca);
    }

    @Test
    void tumAlanlarBossaAnahtarUretilememeli() {
        assertTrue(zincir.anahtarUret(satir(null, null, null, null), eslestirme()).isEmpty());
        assertTrue(zincir.anahtarUret(satir("   ", "  ", "", null), eslestirme()).isEmpty());
    }

    @Test
    void eslestirilmemisAlanlarAnahtariEtkilememeli() {
        AlanEslestirmesi sadeceIsim = new AlanEslestirmesi();
        sadeceIsim.setFirmaAdiKolonu("isim");

        String birinci = zincir.anahtarUret(satir(null, "ABC Ltd", "Doha", "07740905059"), sadeceIsim).get();
        String ikinci = zincir.anahtarUret(satir(null, "ABC Ltd", "Riyadh", "05551112233"), sadeceIsim).get();
        assertEquals(birinci, ikinci);
    }
}
