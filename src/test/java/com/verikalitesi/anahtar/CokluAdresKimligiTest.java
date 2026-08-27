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

/**
 * Adres birden çok kolondan geldiğinde kimlik üretiminin davranışı.
 *
 * <p>Kimlik kopya gruplamasının tamamını belirlediği için bu davranış kilitlenmeli:
 * aynı satır her çalıştırmada aynı kimliği almalı, farklı satırlar farklı kimlik almalı.
 */
class CokluAdresKimligiTest {

    private final HashAnahtarUretici uretici = new HashAnahtarUretici();

    private AlanEslestirmesi eslestirme(String... adresKolonlari) {
        AlanEslestirmesi eslestirme = new AlanEslestirmesi();
        eslestirme.setFirmaAdiKolonu("ad");
        eslestirme.setAdresKolonlari(List.of(adresKolonlari));
        return eslestirme;
    }

    private SatirVerisi satir(String ad, String sehir, String mahalle) {
        SatirVerisi satir = new SatirVerisi();
        Map<String, String> alanlar = new HashMap<>();
        alanlar.put("ad", ad);
        alanlar.put("sehir", sehir);
        alanlar.put("mahalle", mahalle);
        satir.setAlanlar(alanlar);
        return satir;
    }

    private String kimlik(SatirVerisi satir, AlanEslestirmesi eslestirme) {
        Optional<String> anahtar = uretici.uret(satir, eslestirme);
        assertTrue(anahtar.isPresent(), "kimlik üretilmeliydi");
        return anahtar.get();
    }

    @Test
    void kolonSecimSirasiKimligiDegistirmemeli() {
        // Kullanici kolonlari farkli sirayla isaretlese bile ayni kimlik cikmali; aksi halde
        // ayni satir iki calistirmada iki kimlik alir ve kopya gruplamasi bozulur.
        SatirVerisi satir = satir("Ahmet Ticaret", "Bagdat", "Rusafa");

        assertEquals(kimlik(satir, eslestirme("sehir", "mahalle")),
                     kimlik(satir, eslestirme("mahalle", "sehir")));
    }

    @Test
    void farkliAdresFarkliKimlikUretmeli() {
        AlanEslestirmesi eslestirme = eslestirme("sehir", "mahalle");

        assertNotEquals(kimlik(satir("Ahmet Ticaret", "Bagdat", "Rusafa"), eslestirme),
                        kimlik(satir("Ahmet Ticaret", "Basra", "Rusafa"), eslestirme));
    }

    @Test
    void bosAltAlanlarinYeriKorunmali() {
        // (sehir dolu, mahalle bos) ile (sehir bos, mahalle dolu) ayni degerdir sanilmamali.
        AlanEslestirmesi eslestirme = eslestirme("sehir", "mahalle");

        assertNotEquals(kimlik(satir("Ahmet", "Bagdat", ""), eslestirme),
                        kimlik(satir("Ahmet", "", "Bagdat"), eslestirme));
    }

    @Test
    void adresKolonuEklemekKimligiDegistirmeli() {
        // Daha fazla alan = daha ayirt edici kimlik. Kolon eklemek sonucu degistirmeliydi.
        SatirVerisi satir = satir("Ahmet Ticaret", "Bagdat", "Rusafa");

        assertNotEquals(kimlik(satir, eslestirme("sehir")),
                        kimlik(satir, eslestirme("sehir", "mahalle")));
    }

    @Test
    void tumAdresKolonlariBossaVeDigerAlanlarDoluysaKimlikUretilmeli() {
        SatirVerisi satir = satir("Ahmet Ticaret", "", "");

        assertTrue(uretici.uret(satir, eslestirme("sehir", "mahalle")).isPresent());
    }

    @Test
    void hicbirAlanDoluDegilseKimlikUretilmemeli() {
        // Adres birden cok kolondan gelince bos parca "|" ayraci birakiyor; bunu dolu
        // saymak tamamen bos satira kimlik uretmek olurdu.
        SatirVerisi satir = satir("", "", "");

        assertTrue(uretici.uret(satir, eslestirme("sehir", "mahalle")).isEmpty());
    }

    @Test
    void tekKolonSecildigindeAyracEklenmemeli() {
        // Tek kolonda birlestirme yapilacak bir sey yok; sonuc kolonun sadelestirilmis
        // hali olmali. Bastan ya da sondan ayrac eklenirse kimlik degisir ve tek adres
        // kolonu kullanan tablolar refactor oncesinden farkli gruplanirdi.
        SatirVerisi satir = satir("Ahmet Ticaret", "Bagdat", "Rusafa");

        // Yalnizca sehir secili: mahalle degerinin kimlige hicbir etkisi olmamali.
        SatirVerisi mahallesiFarkli = satir("Ahmet Ticaret", "Bagdat", "BambaskaMahalle");

        assertEquals(kimlik(satir, eslestirme("sehir")),
                     kimlik(mahallesiFarkli, eslestirme("sehir")));
    }

    @Test
    void tekKolonluAdresNormalCalismali() {
        AlanEslestirmesi eslestirme = eslestirme("sehir");

        assertNotEquals(kimlik(satir("Ahmet", "Bagdat", "Rusafa"), eslestirme),
                        kimlik(satir("Ahmet", "Basra", "Rusafa"), eslestirme));
    }

    @Test
    void tekKolonBossaVeDigerAlanlarBossaKimlikUretilmemeli() {
        // Tek kolonda ayrac hic olusmadigi icin parca gercekten bos string; koruma
        // burada da calismali.
        assertTrue(uretici.uret(satir("", "", ""), eslestirme("sehir")).isEmpty());
    }

    @Test
    void adresHicEslestirilmemisseCokmemeli() {
        SatirVerisi satir = satir("Ahmet Ticaret", "Bagdat", "Rusafa");

        AlanEslestirmesi adresYok = new AlanEslestirmesi();
        adresYok.setFirmaAdiKolonu("ad");

        assertTrue(uretici.uret(satir, adresYok).isPresent());
    }

    @Test
    void ayraclariFarkliYazilmisAyniAdresAyniKimligiVermeli() {
        AlanEslestirmesi eslestirme = eslestirme("sehir", "mahalle");

        assertEquals(kimlik(satir("Ahmet", "Bagdat", "Rusafa"), eslestirme),
                     kimlik(satir("Ahmet", " BAGDAT ", "Rusafa."), eslestirme));
    }
}
