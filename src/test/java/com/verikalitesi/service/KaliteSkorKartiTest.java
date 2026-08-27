package com.verikalitesi.service;

import com.verikalitesi.core.VeritabaniBaglantiBilgisi;
import com.verikalitesi.dao.BenzerFirmaDao;
import com.verikalitesi.dao.KolonProfilDao;
import com.verikalitesi.dao.SatirVerisiDao;
import com.verikalitesi.dto.AlanEslestirmesi;
import com.verikalitesi.dto.AnalizSonucu;
import com.verikalitesi.dto.KaliteBoyutu;
import com.verikalitesi.dto.KaliteBoyutuTuru;
import com.verikalitesi.dto.SatirVerisi;
import com.verikalitesi.rule.KimlikNoKurali;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KaliteSkorKartiTest {

    @Mock
    private SatirVerisiDao satirVerisiDao;

    @Mock
    private BenzerFirmaDao benzerFirmaDao;

    @Mock
    private KolonProfilDao kolonProfilDao;

    private SatirVerisi satir(String ad, String eposta) {
        SatirVerisi satir = new SatirVerisi();
        Map<String, String> alanlar = new HashMap<>();
        alanlar.put("ad", ad);
        alanlar.put("eposta", eposta);
        satir.setAlanlar(alanlar);
        return satir;
    }

    private AnalizSonucu calistir(List<SatirVerisi> satirlar, AlanEslestirmesi eslestirme) {
        when(satirVerisiDao.satirlariGetir(any(), any(), any(), any())).thenReturn(satirlar);
        when(benzerFirmaDao.benzerFirmalariBul(any(), any(), any(), anyDouble(), any()))
                .thenReturn(List.of());
        ValidationService servis = new ValidationService(satirVerisiDao, benzerFirmaDao,
                kolonProfilDao, List.of("n/a"), 0.7, new KimlikNoKurali(0.8));
        return servis.analizEt(new VeritabaniBaglantiBilgisi(), "tablo", eslestirme, null);
    }

    private AlanEslestirmesi adVeEposta() {
        AlanEslestirmesi eslestirme = new AlanEslestirmesi();
        eslestirme.setFirmaAdiKolonu("ad");
        eslestirme.setePostaKolonu("eposta");
        return eslestirme;
    }

    private KaliteBoyutu boyut(AnalizSonucu sonuc, KaliteBoyutuTuru tur) {
        for (KaliteBoyutu boyut : sonuc.getSkorKarti().getBoyutlar()) {
            if (boyut.getTur() == tur) {
                return boyut;
            }
        }
        throw new AssertionError("boyut yok: " + tur);
    }

    @Test
    void kusursuzVeridePuanYuzYuzOlmali() {
        List<SatirVerisi> satirlar = List.of(
                satir("Ahmet Ticaret", "ahmet@firma.com"),
                satir("Mehmet Ticaret", "mehmet@firma.com"));

        AnalizSonucu sonuc = calistir(satirlar, adVeEposta());

        assertEquals(100.0, boyut(sonuc, KaliteBoyutuTuru.TAMLIK).getPuan());
        assertEquals(100.0, boyut(sonuc, KaliteBoyutuTuru.GECERLILIK).getPuan());
        assertEquals(100.0, boyut(sonuc, KaliteBoyutuTuru.TEKLIK).getPuan());
        assertEquals(100.0, sonuc.getSkorKarti().getGenelPuan());
    }

    @Test
    void olculemeyenBoyutlarPuanaGirmemeli() {
        AnalizSonucu sonuc = calistir(List.of(satir("A", "a@b.com")), adVeEposta());

        assertFalse(boyut(sonuc, KaliteBoyutuTuru.DOGRULUK).isPuanlanabilir());
        assertFalse(boyut(sonuc, KaliteBoyutuTuru.GUNCELLIK).isPuanlanabilir());
        assertEquals(0, boyut(sonuc, KaliteBoyutuTuru.DOGRULUK).getKontrolEdilen());
        // 6 boyuttan yalnizca olculebilen 4'u puanlaniyor
        assertEquals(4, sonuc.getSkorKarti().getPuanlananBoyutSayisi());
    }

    @Test
    void bosAlanTamlikPuaniniDusurmeli() {
        List<SatirVerisi> satirlar = List.of(
                satir("Ahmet", "a@b.com"),
                satir("", "c@d.com"));

        AnalizSonucu sonuc = calistir(satirlar, adVeEposta());

        // 4 hucre tamlik icin denetlendi, 1'i bos
        assertEquals(4, boyut(sonuc, KaliteBoyutuTuru.TAMLIK).getKontrolEdilen());
        assertEquals(1, boyut(sonuc, KaliteBoyutuTuru.TAMLIK).getSorunlu());
        assertEquals(75.0, boyut(sonuc, KaliteBoyutuTuru.TAMLIK).getPuan());
    }

    @Test
    void bozukEpostaGecerlilikPuaniniDusurmeli() {
        List<SatirVerisi> satirlar = List.of(
                satir("Ahmet", "a@b.com"),
                satir("Mehmet", "bozuk-eposta"));

        AnalizSonucu sonuc = calistir(satirlar, adVeEposta());

        assertEquals(2, boyut(sonuc, KaliteBoyutuTuru.GECERLILIK).getKontrolEdilen());
        assertEquals(1, boyut(sonuc, KaliteBoyutuTuru.GECERLILIK).getSorunlu());
        assertEquals(50.0, boyut(sonuc, KaliteBoyutuTuru.GECERLILIK).getPuan());
    }

    @Test
    void yerTutucuHucreGecerlilikPaydasinaGirmemeli() {
        // "n/a" yer tutucu; e-posta bicimi hic denetlenmedi, denetlenmis sayilmamali.
        List<SatirVerisi> satirlar = List.of(
                satir("Ahmet", "n/a"),
                satir("Mehmet", "m@b.com"));

        AnalizSonucu sonuc = calistir(satirlar, adVeEposta());

        assertEquals(1, boyut(sonuc, KaliteBoyutuTuru.GECERLILIK).getKontrolEdilen());
        assertEquals(0, boyut(sonuc, KaliteBoyutuTuru.GECERLILIK).getSorunlu());
        // yer tutucu tamlik sorunu sayilir
        assertEquals(1, boyut(sonuc, KaliteBoyutuTuru.TAMLIK).getSorunlu());
    }

    @Test
    void tumAlanlariBosSatirHicPuanlanmamali() {
        // Boyle bir satir kimlik uretemez, rapora hic girmez. Skor kartina da girmemeli:
        // denetlenmemis satiri "sorunsuz" saymak puani haksiz yere yukseltirdi.
        AnalizSonucu sonuc = calistir(List.of(satir("", ""), satir("Ahmet", "a@b.com")), adVeEposta());

        assertEquals(1, sonuc.getKimliksizSatirSayisi());
        assertEquals(2, boyut(sonuc, KaliteBoyutuTuru.TAMLIK).getKontrolEdilen());
        assertEquals(0, boyut(sonuc, KaliteBoyutuTuru.TAMLIK).getSorunlu());
        assertEquals(1, boyut(sonuc, KaliteBoyutuTuru.TEKLIK).getKontrolEdilen());
    }

    @Test
    void yalnizcaNoktalamaIcerenHucreTamlikSorunuSayilmali() {
        // "---" dolu gorunur ama bilgi tasimaz; yer tutucu kurali bunu tamlik sorunu sayar.
        AnalizSonucu sonuc = calistir(List.of(satir("Ahmet", "---")), adVeEposta());

        assertEquals(2, boyut(sonuc, KaliteBoyutuTuru.TAMLIK).getKontrolEdilen());
        assertEquals(1, boyut(sonuc, KaliteBoyutuTuru.TAMLIK).getSorunlu());
        // bicim denetimi hic yapilmadi, geceerlilik paydasina girmedi
        assertEquals(0, boyut(sonuc, KaliteBoyutuTuru.GECERLILIK).getKontrolEdilen());
    }

    @Test
    void kopyaKayitlarTeklikPuaniniDusurmeli() {
        List<SatirVerisi> satirlar = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            satirlar.add(satir("Ayni Firma", "a@b.com"));
        }
        satirlar.add(satir("Farkli Firma", "c@d.com"));

        AnalizSonucu sonuc = calistir(satirlar, adVeEposta());

        assertEquals(4, boyut(sonuc, KaliteBoyutuTuru.TEKLIK).getKontrolEdilen());
        assertEquals(2, boyut(sonuc, KaliteBoyutuTuru.TEKLIK).getSorunlu());
        assertEquals(50.0, boyut(sonuc, KaliteBoyutuTuru.TEKLIK).getPuan());
    }

    @Test
    void ulkeSecilmediyseTelefonGecerlilikPaydasinaGirmemeli() {
        AlanEslestirmesi eslestirme = new AlanEslestirmesi();
        eslestirme.setFirmaAdiKolonu("ad");
        eslestirme.setTelefonKolonu("telefon");

        SatirVerisi satir = new SatirVerisi();
        Map<String, String> alanlar = new HashMap<>();
        alanlar.put("ad", "Ahmet");
        alanlar.put("telefon", "bu gecersiz bir numara");
        satir.setAlanlar(alanlar);

        AnalizSonucu sonuc = calistir(List.of(satir), eslestirme);

        assertFalse(boyut(sonuc, KaliteBoyutuTuru.GECERLILIK).isPuanlanabilir());
        assertTrue(sonuc.getYapilmayanKontroller().stream()
                .anyMatch(uyari -> uyari.contains("Ülke seçilmediği")));
    }

    @Test
    void hicSatirYoksaKartBosOlmali() {
        AnalizSonucu sonuc = calistir(List.of(), adVeEposta());

        assertTrue(sonuc.getSkorKarti().isBosMu());
        assertEquals(0, sonuc.getSkorKarti().getGenelPuan());
    }
}
