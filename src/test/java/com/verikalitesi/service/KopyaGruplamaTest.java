package com.verikalitesi.service;

import com.verikalitesi.core.VeritabaniBaglantiBilgisi;
import com.verikalitesi.dao.BenzerFirmaDao;
import com.verikalitesi.dao.KolonProfilDao;
import com.verikalitesi.dao.SatirVerisiDao;
import com.verikalitesi.dto.AlanEslestirmesi;
import com.verikalitesi.dto.AnalizSonucu;
import com.verikalitesi.dto.BenzerFirmaCifti;
import com.verikalitesi.dto.KopyaGrubu;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KopyaGruplamaTest {

    @Mock
    private SatirVerisiDao satirVerisiDao;

    @Mock
    private BenzerFirmaDao benzerFirmaDao;

    @Mock
    private KolonProfilDao kolonProfilDao;

    private ValidationService servis() {
        return new ValidationService(satirVerisiDao, benzerFirmaDao, kolonProfilDao, List.of(), 0.7, new KimlikNoKurali(0.8));
    }

    private AlanEslestirmesi eslestirme() {
        AlanEslestirmesi eslestirme = new AlanEslestirmesi();
        eslestirme.setFirmaAdiKolonu("ad");
        return eslestirme;
    }

    private SatirVerisi satir(String ad) {
        SatirVerisi satir = new SatirVerisi();
        Map<String, String> alanlar = new HashMap<>();
        alanlar.put("ad", ad);
        satir.setAlanlar(alanlar);
        return satir;
    }

    private BenzerFirmaCifti cift(String idBir, String idIki) {
        BenzerFirmaCifti cift = new BenzerFirmaCifti();
        cift.setId_1(idBir);
        cift.setId_2(idIki);
        cift.setBenzerlikOrani(0.9);
        return cift;
    }

    private AnalizSonucu calistir(List<SatirVerisi> satirlar, List<BenzerFirmaCifti> ciftler) {
        when(satirVerisiDao.satirlariGetir(any(), any(), any(), any())).thenReturn(satirlar);
        when(benzerFirmaDao.benzerFirmalariBul(any(), any(), any(), anyDouble(), any())).thenReturn(ciftler);
        return servis().analizEt(new VeritabaniBaglantiBilgisi(), "tablo", eslestirme(), null);
    }

    // ---------- Parca 1: gruplama ----------

    @Test
    void ayniAdliSatirlarTekGruptaToplanmali() {
        List<SatirVerisi> satirlar = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            satirlar.add(satir("Ahmet Ticaret"));
        }
        satirlar.add(satir("Baska Firma"));

        AnalizSonucu sonuc = calistir(satirlar, List.of());

        assertEquals(1, sonuc.getKopyaGruplari().size());
        KopyaGrubu grup = sonuc.getKopyaGruplari().get(0);
        assertEquals(5, grup.getKayitSayisi());
        assertEquals(4, grup.getFazlalik());
        assertEquals("Ahmet Ticaret", grup.getOrnekDeger());
    }

    @Test
    void tekBasinaGecenKayitKopyaSayilmamali() {
        AnalizSonucu sonuc = calistir(List.of(satir("A"), satir("B"), satir("C")), List.of());

        assertTrue(sonuc.getKopyaGruplari().isEmpty());
        assertEquals(0, sonuc.getToplamFazlalik());
    }

    @Test
    void toplamFazlalikButunGruplariKapsamali() {
        List<SatirVerisi> satirlar = new ArrayList<>();
        for (int i = 0; i < 3; i++) satirlar.add(satir("A"));   // 2 fazlalik
        for (int i = 0; i < 4; i++) satirlar.add(satir("B"));   // 3 fazlalik
        satirlar.add(satir("C"));                                // 0

        AnalizSonucu sonuc = calistir(satirlar, List.of());

        assertEquals(2, sonuc.getKopyaGruplari().size());
        assertEquals(5, sonuc.getToplamFazlalik());
    }

    @Test
    void gruplarKayitSayisinaGoreAzalanSiralanmali() {
        List<SatirVerisi> satirlar = new ArrayList<>();
        for (int i = 0; i < 2; i++) satirlar.add(satir("Az"));
        for (int i = 0; i < 7; i++) satirlar.add(satir("Cok"));

        AnalizSonucu sonuc = calistir(satirlar, List.of());

        assertEquals("Cok", sonuc.getKopyaGruplari().get(0).getOrnekDeger());
        assertEquals("Az", sonuc.getKopyaGruplari().get(1).getOrnekDeger());
    }

    @Test
    void ayraclariFarkliYazilmisAyniKayitlarAyniGrubaDusmeli() {
        // Noktalama ve fazla bosluk tek bosluga indirgendigi icin ucu de ayni kimligi alir.
        AnalizSonucu sonuc = calistir(
                List.of(satir("Ahmet Ticaret"), satir("Ahmet,  Ticaret"), satir("  AHMET   TICARET ")),
                List.of());

        assertEquals(1, sonuc.getKopyaGruplari().size());
        assertEquals(3, sonuc.getKopyaGruplari().get(0).getKayitSayisi());
    }

    @Test
    void noktaIleBirlesikYazimAyniSayilmamali() {
        // Noktalama silinmiyor, bosluga cevriliyor: "a b c" ile "abc" ayri kayitlardir.
        // Silseydik "A.B" ile "AB" birlesir, farkli kisaltmalar tek kayda duserdi.
        AnalizSonucu sonuc = calistir(List.of(satir("A.B.C Ltd"), satir("ABC Ltd")), List.of());

        assertTrue(sonuc.getKopyaGruplari().isEmpty());
    }

    // ---------- Parca 2: tekillestirme ----------

    @Test
    void ayniKimlikliCiftRapordanDusmeli() {
        AnalizSonucu sonuc = calistir(List.of(satir("A")), List.of(cift("x1", "x1")));

        assertTrue(sonuc.getBenzerFirmaCiftleri().isEmpty());
        assertEquals(1, sonuc.getHamBenzerCiftSayisi());
    }

    @Test
    void tekrarEdenKimlikCiftiSadeceBirKezGorunmeli() {
        List<BenzerFirmaCifti> ciftler = List.of(
                cift("a", "b"), cift("a", "b"), cift("a", "b"));

        AnalizSonucu sonuc = calistir(List.of(satir("A")), ciftler);

        assertEquals(1, sonuc.getBenzerFirmaCiftleri().size());
        assertEquals(3, sonuc.getHamBenzerCiftSayisi());
    }

    @Test
    void tersSiradakiAyniCiftTekrarSayilmali() {
        // (a,b) ile (b,a) ayni cifttir; anahtar siradan bagimsiz olmali.
        AnalizSonucu sonuc = calistir(List.of(satir("A")), List.of(cift("a", "b"), cift("b", "a")));

        assertEquals(1, sonuc.getBenzerFirmaCiftleri().size());
    }

    @Test
    void farkliCiftlerKorunmali() {
        List<BenzerFirmaCifti> ciftler = List.of(
                cift("a", "b"), cift("c", "d"), cift("a", "c"));

        AnalizSonucu sonuc = calistir(List.of(satir("A")), ciftler);

        assertEquals(3, sonuc.getBenzerFirmaCiftleri().size());
    }

    @Test
    void kimligiBosOlanCiftElenmeliVeCokmemeli() {
        AnalizSonucu sonuc = calistir(List.of(satir("A")),
                List.of(cift(null, "b"), cift("a", ""), cift("a", "b")));

        assertEquals(1, sonuc.getBenzerFirmaCiftleri().size());
    }
}
