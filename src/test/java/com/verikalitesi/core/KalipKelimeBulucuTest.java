package com.verikalitesi.core;

import com.verikalitesi.dto.KalipTespiti;
import com.verikalitesi.dto.KelimeFrekansi;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KalipKelimeBulucuTest {

    private final KalipKelimeBulucu bulucu = new KalipKelimeBulucu(2.0);

    private List<KelimeFrekansi> liste(int... tekrarlar) {
        List<KelimeFrekansi> kelimeler = new ArrayList<>();
        for (int i = 0; i < tekrarlar.length; i++) {
            KelimeFrekansi kf = new KelimeFrekansi();
            kf.setKelime("kelime" + i);
            kf.setTekrarSayisi(tekrarlar[i]);
            kelimeler.add(kf);
        }
        return kelimeler;
    }

    private List<String> adlar(List<KelimeFrekansi> kelimeler) {
        List<String> adlar = new ArrayList<>();
        for (KelimeFrekansi kf : kelimeler) {
            adlar.add(kf.getKelime());
        }
        return adlar;
    }

    @Test
    void gercekVeriIrakFirmaAdiKolonundaAltiKalipKelimeBulunmali() {
        // tablo_irak_tasjeelmot.company_name — 24.6% ile 4.8% arasinda 5.13 kat dusus var
        List<KelimeFrekansi> kelimeler = liste(4937, 4853, 3538, 2613, 1322, 1231, 240, 237, 231, 169);

        List<KelimeFrekansi> sonuc = bulucu.kalipKelimeleriBul(kelimeler).getKalipKelimeler();

        assertEquals(6, sonuc.size());
        assertEquals(Arrays.asList("kelime0", "kelime1", "kelime2", "kelime3", "kelime4", "kelime5"), adlar(sonuc));
    }

    @Test
    void gercekVeriKazakistanKolonundaUcKalipKelimeBulunmali() {
        // tablo_kazakistan.name_ru — 80.0% ile 5.7% arasinda 14 kat dusus var
        List<KelimeFrekansi> kelimeler = liste(4087, 4066, 4000, 285, 255, 245, 190, 189);

        List<KelimeFrekansi> sonuc = bulucu.kalipKelimeleriBul(kelimeler).getKalipKelimeler();

        assertEquals(3, sonuc.size());
    }

    @Test
    void kisiAdiKolonundaHicbirKelimeHaricTutulmamali() {
        // tablo_irak_tasjeelmot.name — kisi adlari, en buyuk dusus yalnizca 1.47 kat.
        // Sabit yuzde esigi burada gercek isimleri silerdi; koruma bunu engelliyor.
        List<KelimeFrekansi> kelimeler = liste(784, 608, 582, 396, 312, 291, 241, 233, 219, 213);

        List<KelimeFrekansi> sonuc = bulucu.kalipKelimeleriBul(kelimeler).getKalipKelimeler();

        assertTrue(sonuc.isEmpty());
    }

    @Test
    void siralanmamisListeDeDogruSonucVermeli() {
        List<KelimeFrekansi> kelimeler = liste(240, 4937, 1231, 4853, 237, 3538, 2613, 1322);

        List<KelimeFrekansi> sonuc = bulucu.kalipKelimeleriBul(kelimeler).getKalipKelimeler();

        assertEquals(6, sonuc.size());
        assertEquals(4937, sonuc.get(0).getTekrarSayisi());
    }

    @Test
    void esikDegistiginceSonucDaDegismeli() {
        List<KelimeFrekansi> kelimeler = liste(784, 608, 582, 396, 312);

        // 1.4 esigi ile 1.47 katlik dusus artik yeterli sayilir
        assertEquals(3, new KalipKelimeBulucu(1.4).kalipKelimeleriBul(kelimeler).getKalipKelimeler().size());
        assertTrue(new KalipKelimeBulucu(2.0).kalipKelimeleriBul(kelimeler).getKalipKelimeler().isEmpty());
    }

    @Test
    void bosVeyaTekElemanliListeCokmemeli() {
        assertTrue(bulucu.kalipKelimeleriBul(null).getKalipKelimeler().isEmpty());
        assertTrue(bulucu.kalipKelimeleriBul(new ArrayList<>()).getKalipKelimeler().isEmpty());
        assertTrue(bulucu.kalipKelimeleriBul(liste(5000)).getKalipKelimeler().isEmpty());
    }

    @Test
    void sifirTekrarliKelimeBolmeHatasiVermemeli() {
        List<KelimeFrekansi> kelimeler = liste(100, 50, 0);

        List<KelimeFrekansi> sonuc = bulucu.kalipKelimeleriBul(kelimeler).getKalipKelimeler();

        assertEquals(1, sonuc.size());
    }

    @Test
    void kirilmaBulunmasaBileOlculenOranTasinmali() {
        // tablo_irak.name -- en buyuk dusus 1.73, esik 2.0. Hicbir kelime secilmez ama
        // kullanici NEDEN secilmedigini gorebilmeli; olcum sonucta tasinir.
        List<KelimeFrekansi> kelimeler = liste(3223, 2637, 1826, 1785, 1375, 793, 542, 539);

        KalipTespiti tespit = bulucu.kalipKelimeleriBul(kelimeler);

        assertFalse(tespit.isKirilmaBulundu());
        assertTrue(tespit.getKalipKelimeler().isEmpty());
        assertEquals(1.73, tespit.getEnBuyukDusus(), 0.01);
        assertEquals(2.0, tespit.getEsik(), 0.001);
    }

    @Test
    void kirilmaBulundugundaDaOranTasinmali() {
        KalipTespiti tespit = bulucu.kalipKelimeleriBul(liste(4937, 4853, 3538, 2613, 1322, 1231, 240));

        assertTrue(tespit.isKirilmaBulundu());
        assertEquals(5.13, tespit.getEnBuyukDusus(), 0.01);
    }
}
