package com.verikalitesi.service;

import com.verikalitesi.core.VeritabaniBaglantiBilgisi;
import com.verikalitesi.dao.BenzerFirmaDao;
import com.verikalitesi.dao.KolonProfilDao;
import com.verikalitesi.dao.SatirVerisiDao;
import com.verikalitesi.dto.AlanEslestirmesi;
import com.verikalitesi.dto.AnalizSonucu;
import com.verikalitesi.dto.SatirVerisi;
import com.verikalitesi.rule.DogrulamaSonucu;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ValidationServiceTest {

    @Mock
    private SatirVerisiDao satirVerisiDao;

    @Mock
    private BenzerFirmaDao benzerFirmaDao;

    @Mock
    private KolonProfilDao kolonProfilDao;


    @Test
    void firmaAdiBossaEksikAlanHatasiUretilmeli() {

        AlanEslestirmesi eslestirme = new AlanEslestirmesi();
        eslestirme.setFirmaAdiKolonu("sirket_adi");

        SatirVerisi satir = new SatirVerisi();
        satir.setId("1");
        Map<String, String> alanlar = new HashMap<>();
        alanlar.put("sirket_adi", "");
        satir.setAlanlar(alanlar);


        when(satirVerisiDao.satirlariGetir(any(), any(), any(), any())).thenReturn(List.of(satir));
        when(benzerFirmaDao.benzerFirmalariBul(any(), any(), any(), anyDouble(), any())).thenReturn(List.of());


        ValidationService validationService = new ValidationService(satirVerisiDao, benzerFirmaDao, kolonProfilDao, List.of(), 0.7, new KimlikNoKurali(0.8));

        AnalizSonucu sonuc = validationService.analizEt(new VeritabaniBaglantiBilgisi(), "tablo_irak", eslestirme, null);

        assertEquals(1, sonuc.getDogrulamaSonuclari().size());
        assertEquals("Alan boş veya eksik", sonuc.getDogrulamaSonuclari().get(0).getMesaj());

    }

    @Test
    public void epostaFormatiBozuksaHataUretilmeli() {

        AlanEslestirmesi eslestirme = new AlanEslestirmesi();
        eslestirme.setePostaKolonu("eposta_kolonu");

        SatirVerisi sahteSatir = new SatirVerisi();
        sahteSatir.setId("1");
        sahteSatir.setAlanlar(Map.of("eposta_kolonu", "bozukmail"));

        VeritabaniBaglantiBilgisi bilgi = new VeritabaniBaglantiBilgisi();
        String tabloAdi = "tablo_hollanda";


        when(satirVerisiDao.satirlariGetir(any(), eq(tabloAdi), anyList(), any())).thenReturn(List.of(sahteSatir));

        ValidationService servis = new ValidationService(satirVerisiDao, benzerFirmaDao, kolonProfilDao, List.of(), 0.7, new KimlikNoKurali(0.8));


        AnalizSonucu sonuc = servis.analizEt(bilgi, tabloAdi, eslestirme, null);


        List<DogrulamaSonucu> hatalar = sonuc.getDogrulamaSonuclari();

        assertEquals(1, hatalar.size());

        DogrulamaSonucu hata = hatalar.get(0);
        assertEquals("1", hata.getSatirNo());
        assertEquals("E-posta: ", hata.getAlanAdi());

        assertEquals("Geçersiz e-posta formatı", hata.getMesaj());
    }


    @Test
    public void boslukVarsaHataUretilmeli() {

        AlanEslestirmesi eslestirme = new AlanEslestirmesi();
        eslestirme.setFirmaAdiKolonu("sirket_adi");

        SatirVerisi sahteSatir = new SatirVerisi();
        sahteSatir.setId("1");
        sahteSatir.setAlanlar(Map.of("sirket_adi", " Ahmet Yılmaz "));

        VeritabaniBaglantiBilgisi bilgi = new VeritabaniBaglantiBilgisi();
        String tabloAdi = "tablo_irak";

        when(satirVerisiDao.satirlariGetir(any(), eq(tabloAdi), anyList(), any())).thenReturn(List.of(sahteSatir));
        when(benzerFirmaDao.benzerFirmalariBul(any(), eq(tabloAdi), any(), anyDouble(), any())).thenReturn(new ArrayList<>());

        ValidationService servis = new ValidationService(satirVerisiDao, benzerFirmaDao, kolonProfilDao, List.of(), 0.7, new KimlikNoKurali(0.8));

        AnalizSonucu sonuc = servis.analizEt(bilgi, tabloAdi, eslestirme, null);

        List<DogrulamaSonucu> hatalar = sonuc.getDogrulamaSonuclari();

        assertEquals(1, hatalar.size());

        DogrulamaSonucu hata = hatalar.get(0);
        assertEquals("1", hata.getSatirNo());
        assertEquals("Firma Adı: ", hata.getAlanAdi());
        assertEquals("Başta veya sonda boşluk var.", hata.getMesaj());
    }


    @Test
    public void telefonFormatiBozuksaHataUretilmeli_Hollanda() {

        AlanEslestirmesi eslestirme = new AlanEslestirmesi();
        eslestirme.setTelefonKolonu("telefon_no");
        eslestirme.setUlkeKodu("NL");

        SatirVerisi sahteSatir = new SatirVerisi();
        sahteSatir.setId("1");
        sahteSatir.setAlanlar(Map.of("telefon_no", "123"));

        VeritabaniBaglantiBilgisi bilgi = new VeritabaniBaglantiBilgisi();
        String tabloAdi = "tablo_hollanda";

        when(satirVerisiDao.satirlariGetir(any(), eq(tabloAdi), anyList(), any())).thenReturn(List.of(sahteSatir));

        ValidationService servis = new ValidationService(satirVerisiDao, benzerFirmaDao, kolonProfilDao, List.of(), 0.7, new KimlikNoKurali(0.8));

        AnalizSonucu sonuc = servis.analizEt(bilgi, tabloAdi, eslestirme, null);

        List<DogrulamaSonucu> hatalar = sonuc.getDogrulamaSonuclari();

        assertEquals(1, hatalar.size());
        assertEquals("Telefon: ", hatalar.get(0).getAlanAdi());
        assertEquals("Geçersiz telefon formatı.", hatalar.get(0).getMesaj());
    }


    @Test
    public void tumAlanlarGecerliyseHataUretilmemeli() {

        AlanEslestirmesi eslestirme = new AlanEslestirmesi();
        eslestirme.setFirmaAdiKolonu("sirket_adi");
        eslestirme.setePostaKolonu("eposta_kolonu");
        eslestirme.setTelefonKolonu("telefon_no");
        eslestirme.setUlkeKodu("IQ");

        SatirVerisi sahteSatir = new SatirVerisi();
        sahteSatir.setId("1");
        Map<String, String> alanlar = new HashMap<>();
        alanlar.put("sirket_adi", "Ahmet Yılmaz");
        alanlar.put("eposta_kolonu", "ahmet@example.com");
        alanlar.put("telefon_no", "07740905059");
        sahteSatir.setAlanlar(alanlar);

        VeritabaniBaglantiBilgisi bilgi = new VeritabaniBaglantiBilgisi();
        String tabloAdi = "tablo_irak";

        when(satirVerisiDao.satirlariGetir(any(), eq(tabloAdi), anyList(), any())).thenReturn(List.of(sahteSatir));
        when(benzerFirmaDao.benzerFirmalariBul(any(), eq(tabloAdi), any(), anyDouble(), any())).thenReturn(new ArrayList<>());

        ValidationService servis = new ValidationService(satirVerisiDao, benzerFirmaDao, kolonProfilDao, List.of(), 0.7, new KimlikNoKurali(0.8));

        AnalizSonucu sonuc = servis.analizEt(bilgi, tabloAdi, eslestirme, null);

        assertEquals(0, sonuc.getDogrulamaSonuclari().size());
    }


    @Test
    public void webSitesiFormatiBozuksaHataUretilmeli() {

        AlanEslestirmesi eslestirme = new AlanEslestirmesi();
        eslestirme.setWebSitesiKolonu("web_sitesi");

        SatirVerisi sahteSatir = new SatirVerisi();
        sahteSatir.setId("1");
        sahteSatir.setAlanlar(Map.of("web_sitesi", "bozukwebsite"));

        VeritabaniBaglantiBilgisi bilgi = new VeritabaniBaglantiBilgisi();
        String tabloAdi = "tablo_irak";

        when(satirVerisiDao.satirlariGetir(any(), eq(tabloAdi), anyList(), any())).thenReturn(List.of(sahteSatir));

        ValidationService servis = new ValidationService(satirVerisiDao, benzerFirmaDao, kolonProfilDao, List.of(), 0.7, new KimlikNoKurali(0.8));

        AnalizSonucu sonuc = servis.analizEt(bilgi, tabloAdi, eslestirme, null);

        List<DogrulamaSonucu> hatalar = sonuc.getDogrulamaSonuclari();

        assertEquals(1, hatalar.size());
        assertEquals("1", hatalar.get(0).getSatirNo());
        assertEquals("Web Sitesi: ", hatalar.get(0).getAlanAdi());
    }


    @Test
    public void gecerliWebSitesiIcinHataUretilmemeli() {

        AlanEslestirmesi eslestirme = new AlanEslestirmesi();
        eslestirme.setWebSitesiKolonu("web_sitesi");

        SatirVerisi sahteSatir = new SatirVerisi();
        sahteSatir.setId("1");
        sahteSatir.setAlanlar(Map.of("web_sitesi", "https://www.example.com"));

        VeritabaniBaglantiBilgisi bilgi = new VeritabaniBaglantiBilgisi();
        String tabloAdi = "tablo_irak";

        when(satirVerisiDao.satirlariGetir(any(), eq(tabloAdi), anyList(), any())).thenReturn(List.of(sahteSatir));

        ValidationService servis = new ValidationService(satirVerisiDao, benzerFirmaDao, kolonProfilDao, List.of(), 0.7, new KimlikNoKurali(0.8));

        AnalizSonucu sonuc = servis.analizEt(bilgi, tabloAdi, eslestirme, null);

        assertEquals(0, sonuc.getDogrulamaSonuclari().size());
    }


    @Test
    public void firmaAdiSecilmemisseKopyaFirmaSorgusuHicCalismamali() {

        AlanEslestirmesi eslestirme = new AlanEslestirmesi();
        eslestirme.setAdresKolonlari(List.of("adres_kolonu"));

        SatirVerisi sahteSatir = new SatirVerisi();
        sahteSatir.setId("1");
        sahteSatir.setAlanlar(Map.of("adres_kolonu", "Doha Street 10"));

        VeritabaniBaglantiBilgisi bilgi = new VeritabaniBaglantiBilgisi();
        String tabloAdi = "tablo_kirgizistan";

        when(satirVerisiDao.satirlariGetir(any(), eq(tabloAdi), anyList(), any())).thenReturn(List.of(sahteSatir));

        ValidationService servis = new ValidationService(satirVerisiDao, benzerFirmaDao, kolonProfilDao, List.of(), 0.7, new KimlikNoKurali(0.8));

        AnalizSonucu sonuc = servis.analizEt(bilgi, tabloAdi, eslestirme, null);

        verify(benzerFirmaDao, never()).benzerFirmalariBul(any(), any(), any(), anyDouble(), any());
        assertEquals(0, sonuc.getBenzerFirmaCiftleri().size());
        // Iki uyari: firma adi yok (kopya kontrolu yapilamadi) ve bicim kurali olan hicbir
        // kolon eslestirilmedi (Gecerlilik boyutu olculemedi).
        assertEquals(2, sonuc.getYapilmayanKontroller().size());
        assertTrue(sonuc.getYapilmayanKontroller().get(0).contains("kopya firma"));
        assertTrue(sonuc.getYapilmayanKontroller().get(1).contains("Geçerlilik"));
    }


    @Test
    public void ulkeSecilmemisseTelefonFormatKontroluYapilmamaliVeBildirilmeli() {

        AlanEslestirmesi eslestirme = new AlanEslestirmesi();
        eslestirme.setTelefonKolonu("telefon_no");

        SatirVerisi sahteSatir = new SatirVerisi();
        sahteSatir.setId("1");
        sahteSatir.setAlanlar(Map.of("telefon_no", "123"));

        VeritabaniBaglantiBilgisi bilgi = new VeritabaniBaglantiBilgisi();
        String tabloAdi = "tablo_hollanda";

        when(satirVerisiDao.satirlariGetir(any(), eq(tabloAdi), anyList(), any())).thenReturn(List.of(sahteSatir));

        ValidationService servis = new ValidationService(satirVerisiDao, benzerFirmaDao, kolonProfilDao, List.of(), 0.7, new KimlikNoKurali(0.8));

        AnalizSonucu sonuc = servis.analizEt(bilgi, tabloAdi, eslestirme, null);

        assertEquals(0, sonuc.getDogrulamaSonuclari().size());
        // Uc uyari: ulke yok, firma adi yok, ve telefon ulkesiz denetlenemedigi icin
        // Gecerlilik boyutu bos kaldi.
        assertEquals(3, sonuc.getYapilmayanKontroller().size());
        assertTrue(sonuc.getYapilmayanKontroller().get(0).contains("Ülke seçilmediği"));
        assertTrue(sonuc.getYapilmayanKontroller().get(2).contains("Geçerlilik"));
    }


    @Test
    public void herSeyEslestirilmisseUyariUretilmemeli() {

        AlanEslestirmesi eslestirme = new AlanEslestirmesi();
        eslestirme.setFirmaAdiKolonu("sirket_adi");
        eslestirme.setTelefonKolonu("telefon_no");
        eslestirme.setUlkeKodu("IQ");

        SatirVerisi sahteSatir = new SatirVerisi();
        sahteSatir.setId("1");
        Map<String, String> alanlar = new HashMap<>();
        alanlar.put("sirket_adi", "ABC Ltd");
        alanlar.put("telefon_no", "07740905059");
        sahteSatir.setAlanlar(alanlar);

        VeritabaniBaglantiBilgisi bilgi = new VeritabaniBaglantiBilgisi();
        String tabloAdi = "tablo_irak";

        when(satirVerisiDao.satirlariGetir(any(), eq(tabloAdi), anyList(), any())).thenReturn(List.of(sahteSatir));
        when(benzerFirmaDao.benzerFirmalariBul(any(), eq(tabloAdi), any(), anyDouble(), any())).thenReturn(new ArrayList<>());

        ValidationService servis = new ValidationService(satirVerisiDao, benzerFirmaDao, kolonProfilDao, List.of(), 0.7, new KimlikNoKurali(0.8));

        AnalizSonucu sonuc = servis.analizEt(bilgi, tabloAdi, eslestirme, null);

        assertEquals(0, sonuc.getYapilmayanKontroller().size());
    }


    @Test
    public void ozetAyniBulgulariGruplayipAdedeGoreSiralamali() {

        AlanEslestirmesi eslestirme = new AlanEslestirmesi();
        eslestirme.setAdresKolonlari(List.of("adres"));
        eslestirme.setePostaKolonu("eposta");

        List<SatirVerisi> satirlar = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            SatirVerisi satir = new SatirVerisi();
            satir.setId(String.valueOf(i));
            Map<String, String> alanlar = new HashMap<>();
            alanlar.put("adres", "");
            alanlar.put("eposta", i == 1 ? "bozukmail" : "gecerli@example.com");
            satir.setAlanlar(alanlar);
            satirlar.add(satir);
        }

        VeritabaniBaglantiBilgisi bilgi = new VeritabaniBaglantiBilgisi();
        String tabloAdi = "tablo_irak";

        when(satirVerisiDao.satirlariGetir(any(), eq(tabloAdi), anyList(), any())).thenReturn(satirlar);

        ValidationService servis = new ValidationService(satirVerisiDao, benzerFirmaDao, kolonProfilDao, List.of(), 0.7, new KimlikNoKurali(0.8));

        AnalizSonucu sonuc = servis.analizEt(bilgi, tabloAdi, eslestirme, null);

        assertEquals(4, sonuc.getDogrulamaSonuclari().size());
        assertEquals(2, sonuc.getOzet().size());

        // Alan adında kolon adı da yazıyor: adres birden çok kolondan gelebildiği için
        // bulgunun hangi parçaya ait olduğu görünmeli.
        assertEquals("Adres (adres): ", sonuc.getOzet().get(0).getAlanAdi());
        assertEquals("Alan boş veya eksik", sonuc.getOzet().get(0).getMesaj());
        assertEquals(3, sonuc.getOzet().get(0).getAdet());

        assertEquals("E-posta: ", sonuc.getOzet().get(1).getAlanAdi());
        assertEquals(1, sonuc.getOzet().get(1).getAdet());
    }


    @Test
    public void idKolonuSecilmemisseKimlikUretilmeliVeProfilCikarilmamali() {

        AlanEslestirmesi eslestirme = new AlanEslestirmesi();
        eslestirme.setFirmaAdiKolonu("sirket_adi");
        eslestirme.setAdresKolonlari(List.of("adres"));

        SatirVerisi sahteSatir = new SatirVerisi();
        sahteSatir.setId(null);
        Map<String, String> alanlar = new HashMap<>();
        alanlar.put("sirket_adi", "ABC Ltd");
        alanlar.put("adres", "  Doha  Street 10  ");
        sahteSatir.setAlanlar(alanlar);

        VeritabaniBaglantiBilgisi bilgi = new VeritabaniBaglantiBilgisi();
        String tabloAdi = "tablo_irak_tasjeelmot";

        when(satirVerisiDao.satirlariGetir(any(), eq(tabloAdi), anyList(), any())).thenReturn(List.of(sahteSatir));
        when(benzerFirmaDao.benzerFirmalariBul(any(), eq(tabloAdi), any(), anyDouble(), any())).thenReturn(new ArrayList<>());

        ValidationService servis = new ValidationService(satirVerisiDao, benzerFirmaDao, kolonProfilDao, List.of(), 0.7, new KimlikNoKurali(0.8));

        AnalizSonucu sonuc = servis.analizEt(bilgi, tabloAdi, eslestirme, null);

        assertEquals(1, sonuc.getUretilmisKimlikSayisi());
        assertEquals(0, sonuc.getKimliksizSatirSayisi());
        assertEquals(null, sonuc.getIdKolonuProfili());

        assertEquals(1, sonuc.getDogrulamaSonuclari().size());
        assertTrue(sonuc.getDogrulamaSonuclari().get(0).getSatirNo().startsWith("⚙"));

        verify(kolonProfilDao, never()).kolonuProfille(any(), any(), any());
    }


    @Test
    public void tumAlanlarBossaSatirRaporaGirmemeli() {

        AlanEslestirmesi eslestirme = new AlanEslestirmesi();
        eslestirme.setFirmaAdiKolonu("sirket_adi");

        SatirVerisi sahteSatir = new SatirVerisi();
        sahteSatir.setId(null);
        Map<String, String> alanlar = new HashMap<>();
        alanlar.put("sirket_adi", "");
        sahteSatir.setAlanlar(alanlar);

        VeritabaniBaglantiBilgisi bilgi = new VeritabaniBaglantiBilgisi();
        String tabloAdi = "tablo_irak_tasjeelmot";

        when(satirVerisiDao.satirlariGetir(any(), eq(tabloAdi), anyList(), any())).thenReturn(List.of(sahteSatir));
        when(benzerFirmaDao.benzerFirmalariBul(any(), eq(tabloAdi), any(), anyDouble(), any())).thenReturn(new ArrayList<>());

        ValidationService servis = new ValidationService(satirVerisiDao, benzerFirmaDao, kolonProfilDao, List.of(), 0.7, new KimlikNoKurali(0.8));

        AnalizSonucu sonuc = servis.analizEt(bilgi, tabloAdi, eslestirme, null);

        assertEquals(1, sonuc.getKimliksizSatirSayisi());
        assertEquals(0, sonuc.getUretilmisKimlikSayisi());
        assertEquals(0, sonuc.getDogrulamaSonuclari().size());
        assertTrue(sonuc.getYapilmayanKontroller().get(0).contains("kimlik üretilemedi"));
    }

}