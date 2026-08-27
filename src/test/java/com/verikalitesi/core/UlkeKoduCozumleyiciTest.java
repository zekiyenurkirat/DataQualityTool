package com.verikalitesi.core;

import com.verikalitesi.dto.UlkeSecenegi;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UlkeKoduCozumleyiciTest {

    @Test
    void basitTabloAdlariCozulmeli() {
        assertEquals("IQ", UlkeKoduCozumleyici.tabloAdindanUlkeKodunuBul("tablo_irak"));
        assertEquals("NL", UlkeKoduCozumleyici.tabloAdindanUlkeKodunuBul("tablo_hollanda"));
        assertEquals("CO", UlkeKoduCozumleyici.tabloAdindanUlkeKodunuBul("tablo_kolombiya"));
    }

    @Test
    void turkceKarakterliUlkeAdlariCozulmeli() {
        assertEquals("KG", UlkeKoduCozumleyici.tabloAdindanUlkeKodunuBul("tablo_kirgizistan"));
    }

    @Test
    void eskidenCozulemeyenTablolarArtikCozulmeli() {
        assertEquals("TH", UlkeKoduCozumleyici.tabloAdindanUlkeKodunuBul("tablo_tayland"));
        assertEquals("KZ", UlkeKoduCozumleyici.tabloAdindanUlkeKodunuBul("tablo_kazakistan"));
    }

    @Test
    void birdenFazlaEkiOlanTabloAdlariCozulmeli() {
        assertEquals("IQ", UlkeKoduCozumleyici.tabloAdindanUlkeKodunuBul("tablo_irak_itp_firma"));
        assertEquals("IQ", UlkeKoduCozumleyici.tabloAdindanUlkeKodunuBul("tablo_irak_itp_tedarikci"));
        assertEquals("IQ", UlkeKoduCozumleyici.tabloAdindanUlkeKodunuBul("tablo_irak_tasjeelmot"));
        assertEquals("IQ", UlkeKoduCozumleyici.tabloAdindanUlkeKodunuBul("tablo_irak_firma"));
    }

    @Test
    void ingilizceUlkeAdlariDaCozulmeli() {
        assertEquals("PL", UlkeKoduCozumleyici.tabloAdindanUlkeKodunuBul("firmy_poland"));
        assertEquals("DE", UlkeKoduCozumleyici.tabloAdindanUlkeKodunuBul("tablo_germany"));
    }

    @Test
    void ulkeIcermeyenTabloAdiIcinNullDonmeli() {
        assertNull(UlkeKoduCozumleyici.tabloAdindanUlkeKodunuBul("companies_2024"));
        assertNull(UlkeKoduCozumleyici.tabloAdindanUlkeKodunuBul("dim_customer"));
        assertNull(UlkeKoduCozumleyici.tabloAdindanUlkeKodunuBul(null));
    }

    @Test
    void ulkeListesiDoluVeSiraliOlmali() {
        List<UlkeSecenegi> ulkeler = UlkeKoduCozumleyici.tumUlkeler();

        assertTrue(ulkeler.size() > 200);

        for (int i = 1; i < ulkeler.size(); i++) {
            assertTrue(ulkeler.get(i - 1).getAd().compareToIgnoreCase(ulkeler.get(i).getAd()) <= 0,
                    "Liste alfabetik sırada değil: " + ulkeler.get(i - 1).getAd() + " > " + ulkeler.get(i).getAd());
        }
    }

    @Test
    void ulkeListesindeAranilanUlkelerBulunmali() {
        List<UlkeSecenegi> ulkeler = UlkeKoduCozumleyici.tumUlkeler();
        assertTrue(ulkeler.stream().anyMatch(u -> u.getKod().equals("IQ")));
        assertTrue(ulkeler.stream().anyMatch(u -> u.getKod().equals("TH")));
        assertTrue(ulkeler.stream().anyMatch(u -> u.getKod().equals("KZ")));
    }
}
