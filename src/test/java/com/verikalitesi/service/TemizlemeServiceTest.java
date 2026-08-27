package com.verikalitesi.service;

import com.verikalitesi.core.VeritabaniBaglantiBilgisi;
import com.verikalitesi.dao.SatirVerisiDao;
import com.verikalitesi.dao.TemizlemeDao;
import com.verikalitesi.dto.KolonTemizlemeSonucu;
import com.verikalitesi.dto.SatirVerisi;
import com.verikalitesi.dto.TemizlemeKurali;
import com.verikalitesi.dto.TemizlemeOnizlemeSonucu;
import com.verikalitesi.dto.TemizlemeSonucu;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TemizlemeServiceTest {

    @Mock
    private TemizlemeDao temizlemeDao;

    @Mock
    private SatirVerisiDao satirVerisiDao;


    @Test
    void kuralListesiBossaBosOnizlemeDonmeli() {

        TemizlemeService servis = new TemizlemeService(temizlemeDao, satirVerisiDao);

        List<TemizlemeOnizlemeSonucu> sonuclar = servis.onizlemeOlustur(new VeritabaniBaglantiBilgisi(), "tablo_irak", "id", "IQ", null);

        assertEquals(0, sonuclar.size());
    }


    @Test
    void metinKuraliDaoyaIletilmeli() {

        TemizlemeKurali kural = new TemizlemeKurali();
        kural.setKolonAdi("name");
        kural.setBosluklariKirp(true);
        kural.setArdisikBosluklariDaralt(true);

        TemizlemeOnizlemeSonucu sahteSonuc = new TemizlemeOnizlemeSonucu();
        sahteSonuc.setKolonAdi("name");
        sahteSonuc.setEtkilenecekSatirSayisi(7);

        when(temizlemeDao.metinKolonuOnizle(any(), anyString(), anyString(), any(), anyInt()))
                .thenReturn(sahteSonuc);

        TemizlemeService servis = new TemizlemeService(temizlemeDao, satirVerisiDao);

        List<TemizlemeOnizlemeSonucu> sonuclar = servis.onizlemeOlustur(new VeritabaniBaglantiBilgisi(), "tablo_irak", "id", "IQ", List.of(kural));

        assertEquals(1, sonuclar.size());
        assertEquals("name", sonuclar.get(0).getKolonAdi());
        assertEquals(7, sonuclar.get(0).getEtkilenecekSatirSayisi());
    }


    @Test
    void telefonFormatiDegisenSatirOnizlemedeGorunmeli() {

        TemizlemeKurali kural = new TemizlemeKurali();
        kural.setKolonAdi("phone_number");
        kural.setTelefonFormatiUygulanacakMi(true);
        kural.setTelefonHedefFormati("E164");

        SatirVerisi satir = new SatirVerisi();
        satir.setId("1");
        satir.setAlanlar(Map.of("phone_number", "07740905059"));

        when(satirVerisiDao.satirlariGetir(any(), anyString(), any(), anyString())).thenReturn(List.of(satir));

        TemizlemeService servis = new TemizlemeService(temizlemeDao, satirVerisiDao);

        List<TemizlemeOnizlemeSonucu> sonuclar = servis.onizlemeOlustur(new VeritabaniBaglantiBilgisi(), "tablo_irak", "id", "IQ", List.of(kural));

        assertEquals(1, sonuclar.size());
        assertEquals(1, sonuclar.get(0).getEtkilenecekSatirSayisi());
        assertEquals("07740905059", sonuclar.get(0).getOrnekler().get(0).getEskiDeger());
        assertEquals("+9647740905059", sonuclar.get(0).getOrnekler().get(0).getYeniDeger());
    }


    @Test
    void zatenDogruFormattaOlanTelefonDegisiklikSayilmamali() {

        TemizlemeKurali kural = new TemizlemeKurali();
        kural.setKolonAdi("phone_number");
        kural.setTelefonFormatiUygulanacakMi(true);
        kural.setTelefonHedefFormati("E164");

        SatirVerisi satir = new SatirVerisi();
        satir.setId("1");
        satir.setAlanlar(Map.of("phone_number", "+9647740905059"));

        when(satirVerisiDao.satirlariGetir(any(), anyString(), any(), anyString())).thenReturn(List.of(satir));

        TemizlemeService servis = new TemizlemeService(temizlemeDao, satirVerisiDao);

        List<TemizlemeOnizlemeSonucu> sonuclar = servis.onizlemeOlustur(new VeritabaniBaglantiBilgisi(), "tablo_irak", "id", "IQ", List.of(kural));

        assertEquals(0, sonuclar.get(0).getEtkilenecekSatirSayisi());
    }


    @Test
    void ayristirilamayanTelefonAtlananSayisinaEklenmeliVeHataFirlatmamali() {

        TemizlemeKurali kural = new TemizlemeKurali();
        kural.setKolonAdi("phone_number");
        kural.setTelefonFormatiUygulanacakMi(true);
        kural.setTelefonHedefFormati("E164");

        SatirVerisi satir = new SatirVerisi();
        satir.setId("1");
        satir.setAlanlar(Map.of("phone_number", "abc"));

        when(satirVerisiDao.satirlariGetir(any(), anyString(), any(), anyString())).thenReturn(List.of(satir));

        TemizlemeSonucu sahteSonuc = new TemizlemeSonucu();
        KolonTemizlemeSonucu kolonSonucu = new KolonTemizlemeSonucu();
        kolonSonucu.setKolonAdi("phone_number");
        kolonSonucu.setGuncellenenSatirSayisi(0);
        sahteSonuc.setKolonSonuclari(new ArrayList<>(List.of(kolonSonucu)));

        when(temizlemeDao.tumKurallariUygula(any(), anyString(), anyString(), any(), any())).thenReturn(sahteSonuc);

        TemizlemeService servis = new TemizlemeService(temizlemeDao, satirVerisiDao);

        TemizlemeSonucu sonuc = servis.temizlemeyiUygula(new VeritabaniBaglantiBilgisi(), "tablo_irak", "id", "IQ", List.of(kural));

        assertEquals(1, sonuc.getKolonSonuclari().get(0).getAtlananSatirSayisi());
        assertEquals(0, sonuc.getKolonSonuclari().get(0).getGuncellenenSatirSayisi());
    }


    @Test
    void metinVeTelefonKurallariAyriAyriIletilmeli() {

        TemizlemeKurali metinKurali = new TemizlemeKurali();
        metinKurali.setKolonAdi("name");
        metinKurali.setBosluklariKirp(true);
        metinKurali.setArdisikBosluklariDaralt(true);

        TemizlemeKurali telefonKurali = new TemizlemeKurali();
        telefonKurali.setKolonAdi("phone_number");
        telefonKurali.setTelefonFormatiUygulanacakMi(true);
        telefonKurali.setTelefonHedefFormati("E164");

        SatirVerisi satir = new SatirVerisi();
        satir.setId("1");
        satir.setAlanlar(Map.of("phone_number", "07740905059"));

        when(satirVerisiDao.satirlariGetir(any(), anyString(), any(), anyString())).thenReturn(List.of(satir));

        TemizlemeSonucu sahteSonuc = new TemizlemeSonucu();
        sahteSonuc.setKolonSonuclari(new ArrayList<>());
        when(temizlemeDao.tumKurallariUygula(any(), anyString(), anyString(), any(), any())).thenReturn(sahteSonuc);

        TemizlemeService servis = new TemizlemeService(temizlemeDao, satirVerisiDao);

        servis.temizlemeyiUygula(new VeritabaniBaglantiBilgisi(), "tablo_irak", "id", "IQ", List.of(metinKurali, telefonKurali));

        ArgumentCaptor<List<TemizlemeKurali>> metinYakalayici = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, Map<String, String>>> telefonYakalayici = ArgumentCaptor.forClass(Map.class);

        verify(temizlemeDao).tumKurallariUygula(any(), anyString(), anyString(), metinYakalayici.capture(), telefonYakalayici.capture());

        List<TemizlemeKurali> iletilenMetinKurallari = metinYakalayici.getValue();
        assertEquals(1, iletilenMetinKurallari.size());
        assertEquals("name", iletilenMetinKurallari.get(0).getKolonAdi());

        Map<String, Map<String, String>> iletilenTelefonGuncellemeleri = telefonYakalayici.getValue();
        assertTrue(iletilenTelefonGuncellemeleri.containsKey("phone_number"));
        assertEquals("+9647740905059", iletilenTelefonGuncellemeleri.get("phone_number").get("1"));
    }
}
