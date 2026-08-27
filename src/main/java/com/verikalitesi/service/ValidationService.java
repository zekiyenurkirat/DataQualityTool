package com.verikalitesi.service;

import com.verikalitesi.anahtar.AnahtarZinciri;
import com.verikalitesi.altinkayit.AltinKayitSecici;
import com.verikalitesi.core.VeritabaniBaglantiBilgisi;
import com.verikalitesi.dao.BenzerFirmaDao;
import com.verikalitesi.dao.KolonProfilDao;
import com.verikalitesi.dao.SatirVerisiDao;
import com.verikalitesi.dto.AlanEslestirmesi;
import com.verikalitesi.dto.AnalizSonucu;
import com.verikalitesi.dto.BenzerFirmaCifti;
import com.verikalitesi.dto.KaliteBoyutuTuru;
import com.verikalitesi.dto.KaliteSkorKarti;
import com.verikalitesi.dto.KimlikNoProfili;
import com.verikalitesi.dto.KolonProfili;
import com.verikalitesi.dto.KopyaGrubu;
import com.verikalitesi.dto.MesajOzeti;
import com.verikalitesi.dto.SatirVerisi;
import com.verikalitesi.rule.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class ValidationService {

    private final SatirVerisiDao satirVerisiDao;
    private final BenzerFirmaDao benzerFirmaDao;
    private final KolonProfilDao kolonProfilDao;
    private final List<String> yerTutucular;
    private final double benzerlikEsigi;
    private final KimlikNoKurali kimlikNoKurali;
    private final AnahtarZinciri anahtarZinciri = AnahtarZinciri.varsayilan();

    public ValidationService(SatirVerisiDao satirVerisiDao, BenzerFirmaDao benzerFirmaDao,
                             KolonProfilDao kolonProfilDao,
                             @Value("${veri.yerTutucular:}") List<String> yerTutucular,
                             @Value("${veri.benzerlikEsigi:0.7}") double benzerlikEsigi,
                             KimlikNoKurali kimlikNoKurali) {
        this.satirVerisiDao = satirVerisiDao;
        this.benzerFirmaDao = benzerFirmaDao;
        this.kolonProfilDao = kolonProfilDao;
        this.yerTutucular = yerTutucular;
        this.benzerlikEsigi = benzerlikEsigi;
        this.kimlikNoKurali = kimlikNoKurali;
    }

    public AnalizSonucu analizEt(VeritabaniBaglantiBilgisi bilgi, String tabloAdi, AlanEslestirmesi alanEslestirmesi, List<String> haricTutulacakKelimeler){

        List<String> okunacakKolonlar = new ArrayList<>();

        // Yalnızca eşleştirilen kolonlar okunuyor; geri kalanı hiç sorgulanmıyor.
        okunacakKolonlar.addAll(alanEslestirmesi.getAdresKolonlari());
        if(alanEslestirmesi.getePostaKolonu() != null){
            okunacakKolonlar.add(alanEslestirmesi.getePostaKolonu());
        }
        if(alanEslestirmesi.getFirmaAdiKolonu() != null){
            okunacakKolonlar.add(alanEslestirmesi.getFirmaAdiKolonu());
        }
        if(alanEslestirmesi.getTelefonKolonu() != null){
            okunacakKolonlar.add(alanEslestirmesi.getTelefonKolonu());
        }

        if(alanEslestirmesi.getWebSitesiKolonu() != null){
            okunacakKolonlar.add(alanEslestirmesi.getWebSitesiKolonu());
        }



        List<SatirVerisi> satirlar = satirVerisiDao.satirlariGetir(bilgi, tabloAdi, okunacakKolonlar, alanEslestirmesi.getIdKolonu());

        // Ham kimlik numaralarını zincir çalışmadan ÖNCE saklıyoruz: zincir boş kimlikleri
        // üretilmiş hash ile doldurduğu için sonrasında orijinal değere ulaşılamıyor.
        List<String> hamKimlikNolari = new ArrayList<>(satirlar.size());
        for (SatirVerisi satir : satirlar) {
            hamKimlikNolari.add(satir.getId());
        }

        int uretilmisKimlikSayisi = 0;
        int kimliksizSatirSayisi = 0;
        for (SatirVerisi satir : satirlar) {
            boolean kimligiVardi = satir.getId() != null && !satir.getId().isBlank();
            Optional<String> anahtar = anahtarZinciri.anahtarUret(satir, alanEslestirmesi);
            if (anahtar.isEmpty()) {
                kimliksizSatirSayisi++;
                continue;
            }
            satir.setId(anahtar.get());
            if (!kimligiVardi) {
                uretilmisKimlikSayisi++;
            }
        }

        List<DogrulamaSonucu> dogrulamaSonuclari = new ArrayList<>();
        List<String> yapilmayanKontroller = new ArrayList<>();

        // kurallar
        EksikAlanKurali eksikAlanKurali = new EksikAlanKurali();
        BoslukKurali boslukKurali = new BoslukKurali();
        EmailRule emailRule = new EmailRule();
        WebSitesiKurali webSitesiKurali = new WebSitesiKurali();
        PlaceholderKurali placeholderKurali = new PlaceholderKurali(yerTutucular);

        String ulkeKodu = alanEslestirmesi.getUlkeKodu();
        TelefonKurali telefonKurali = new TelefonKurali(ulkeKodu);
        boolean telefonKontroluYapilabilir = ulkeKodu != null && !ulkeKodu.isBlank();

        if (alanEslestirmesi.getTelefonKolonu() != null && !telefonKontroluYapilabilir) {
            yapilmayanKontroller.add("Ülke seçilmediği için telefon numarası format kontrolü yapılmadı. "
                    + "Numaraların hangi ülkeye ait olduğu bilinmeden geçerlilik denetlenemiyor.");
        }
        if (alanEslestirmesi.getFirmaAdiKolonu() == null) {
            yapilmayanKontroller.add("Firma Adı kolonu eşleştirilmediği için olası kopya firma kontrolü yapılmadı. "
                    + "Aşağıdaki kopya firma tablosunun boş olması, kopya olmadığı anlamına gelmez.");
        }
        if (uretilmisKimlikSayisi > 0) {
            yapilmayanKontroller.add(uretilmisKimlikSayisi + " satırda kimlik kolonu boştu; bu satırlar için "
                    + "eşleştirilen alanlardan kimlik üretildi. Üretilen kimlikler ⚙ ile işaretlidir ve "
                    + "veritabanında aranamaz.");
        }
        if (kimliksizSatirSayisi > 0) {
            yapilmayanKontroller.add(kimliksizSatirSayisi + " satır için kimlik üretilemedi; hem kimlik kolonu "
                    + "hem de eşleştirilen alanların tamamı boş. Bu satırlar raporda yer almıyor.");
        }

        KimlikNoProfili kimlikNoProfili = null;
        String idKolonu = alanEslestirmesi.getIdKolonu();
        if (idKolonu != null && !idKolonu.isBlank()) {
            kimlikNoProfili = kimlikNoKurali.kolonuCozumle(idKolonu, hamKimlikNolari);
            if (kimlikNoProfili.isBaskinSekilVarMi()) {
                List<String> satirNolari = new ArrayList<>(satirlar.size());
                for (SatirVerisi satir : satirlar) {
                    satirNolari.add(satir.getId());
                }
                dogrulamaSonuclari.addAll(kimlikNoKurali.kaliptanSapanlariBul(
                        kimlikNoProfili, "Kimlik No: ", satirNolari, hamKimlikNolari));
            } else if (kimlikNoProfili.getToplamDeger() > 0) {
                yapilmayanKontroller.add("Kimlik numarası kolonunda baskın bir kalıp bulunamadı, "
                        + "bu yüzden kalıp tutarlılığı denetlenmedi. Değerler rastgele üretilmiş "
                        + "olabilir (UUID gibi); böyle bir kolonda kalıp sapması diye bir şey yoktur.");
            }
        }

        KaliteSkorKarti skorKarti = new KaliteSkorKarti();

        for (SatirVerisi satir : satirlar) {
            String satirNo = satir.getId();
            if (satirNo == null || satirNo.isBlank()) {
                continue;
            }
            Map<String, String> alanlar = satir.getAlanlar();
            // tek seferde kontrol ilgili satır için

            if (alanEslestirmesi.getFirmaAdiKolonu() != null) {
                String deger = alanlar.get(alanEslestirmesi.getFirmaAdiKolonu());
                alaniKontrolEt(placeholderKurali, satirNo, "Firma Adı: ", deger,
                        List.of(eksikAlanKurali, boslukKurali), dogrulamaSonuclari, skorKarti);
            }

            if (alanEslestirmesi.getePostaKolonu() != null) {
                String deger = alanlar.get(alanEslestirmesi.getePostaKolonu());
                alaniKontrolEt(placeholderKurali, satirNo, "E-posta: ", deger,
                        List.of(eksikAlanKurali, boslukKurali, emailRule), dogrulamaSonuclari, skorKarti);
            }

            if (alanEslestirmesi.getTelefonKolonu() != null) {
                String deger = alanlar.get(alanEslestirmesi.getTelefonKolonu());
                List<Kural> telefonKurallari = telefonKontroluYapilabilir
                        ? List.of(eksikAlanKurali, boslukKurali, telefonKurali)
                        : List.of(eksikAlanKurali, boslukKurali);
                alaniKontrolEt(placeholderKurali, satirNo, "Telefon: ", deger,
                        telefonKurallari, dogrulamaSonuclari, skorKarti);
            }

            // Adres kolonları tek tek denetlenir, birleştirilmiş hâli değil: şehir dolu
            // mahalle boşsa birleşik değer dolu görünür ve eksiklik gizlenirdi. Alan adına
            // kolon adı da yazılır ki bulgu hangi parçaya ait belli olsun.
            for (String adresKolonu : alanEslestirmesi.getAdresKolonlari()) {
                String deger = alanlar.get(adresKolonu);
                alaniKontrolEt(placeholderKurali, satirNo, "Adres (" + adresKolonu + "): ", deger,
                        List.of(eksikAlanKurali, boslukKurali), dogrulamaSonuclari, skorKarti);
            }

            if (alanEslestirmesi.getWebSitesiKolonu() != null) {
                String deger = alanlar.get(alanEslestirmesi.getWebSitesiKolonu());
                alaniKontrolEt(placeholderKurali, satirNo, "Web Sitesi: ", deger,
                        List.of(eksikAlanKurali, boslukKurali, webSitesiKurali), dogrulamaSonuclari, skorKarti);
            }
        }

        List<BenzerFirmaCifti> hamCiftler = new ArrayList<>();
        if (alanEslestirmesi.getFirmaAdiKolonu() != null) {
            hamCiftler = benzerFirmaDao.benzerFirmalariBul(bilgi, tabloAdi, alanEslestirmesi, benzerlikEsigi, haricTutulacakKelimeler);
        }
        List<BenzerFirmaCifti> benzerFirmalar = ciftleriTekillestir(hamCiftler);

        // Ana kayıt (golden record) kararı her çift için burada hesaplanıyor ama
        // HİÇBİR ŞEY yazılmıyor. Araç yalnızca "doluluk kuralına göre bu kayıt
        // kalmalı" diyor; birleştirme ancak kullanıcı raporda görüp onayladığında
        // gerçekleşiyor. Yer tutucu kuralının aynı örneği veriliyor ki "boş" tanımı
        // doğrulama ile ana kayıt seçimi arasında ayrışmasın.
        AltinKayitSecici altinKayitSecici = new AltinKayitSecici(placeholderKurali);
        for (BenzerFirmaCifti cift : benzerFirmalar) {
            cift.setAltinKayitKarari(altinKayitSecici.sec(
                    cift.getId_1(), cift.getAlanlar1(),
                    cift.getId_2(), cift.getAlanlar2()));
        }

        List<KopyaGrubu> kopyaGruplari = kopyaGruplariniBul(satirlar, alanEslestirmesi);
        int toplamFazlalik = fazlalikToplami(kopyaGruplari);

        // Teklik hücre değil satır düzeyinde ölçülür: kimlik alabilmiş her satır denetlenmiştir,
        // fazlalık satırlar sorunludur. Kimliksiz satırlar hiç gruplanamadığı için paydaya girmez.
        int kimlikliSatirSayisi = satirlar.size() - kimliksizSatirSayisi;
        if (kimlikliSatirSayisi > 0) {
            skorKarti.topluEkle(KaliteBoyutuTuru.TEKLIK, kimlikliSatirSayisi, toplamFazlalik);
        }

        // Kimlik numarası kalıbı da bir tutarlılık ölçümü; hücre sayan diğer tutarlılık
        // kontrolleriyle aynı paydada toplanıyor, ikisi de "yapılan denetim" sayılıyor.
        if (kimlikNoProfili != null && kimlikNoProfili.isBaskinSekilVarMi()) {
            skorKarti.topluEkle(KaliteBoyutuTuru.TUTARLILIK,
                    kimlikNoProfili.getToplamDeger(), kimlikNoProfili.getUymayanAdet());
        }

        AnalizSonucu analizSonucu = new AnalizSonucu();
        analizSonucu.setDogrulamaSonuclari(dogrulamaSonuclari);
        analizSonucu.setBenzerFirmaCiftleri(benzerFirmalar);
        analizSonucu.setHamBenzerCiftSayisi(hamCiftler.size());
        analizSonucu.setKopyaGruplari(kopyaGruplari);
        // Ölçülemeyen her boyutun gerekçesi bir yerde yazmalı. Doğruluk ve güncellik için
        // gerekçe kalıcı (referans kaynak / tarih alanı yok) ve skor kartında duruyor;
        // geçerlilik ise seçime bağlı olarak ölçülemez kalabildiği için buraya yazılıyor.
        if (hicBicimDenetimiYapilmadiMi(alanEslestirmesi, telefonKontroluYapilabilir)) {
            yapilmayanKontroller.add("E-posta, telefon ve web sitesi kolonlarının hiçbiri "
                    + "biçim denetimine girmediği için skor kartındaki Geçerlilik boyutu ölçülemedi. "
                    + "Bu boyutun boş olması, biçim sorunu olmadığı anlamına gelmez.");
        }

        analizSonucu.setToplamFazlalik(toplamFazlalik);
        analizSonucu.setSkorKarti(skorKarti);
        analizSonucu.setYapilmayanKontroller(yapilmayanKontroller);
        analizSonucu.setOzet(ozetCikar(dogrulamaSonuclari));
        analizSonucu.setUretilmisKimlikSayisi(uretilmisKimlikSayisi);
        analizSonucu.setKimliksizSatirSayisi(kimliksizSatirSayisi);
        analizSonucu.setIdKolonuProfili(idKolonuProfiliniAl(bilgi, tabloAdi, alanEslestirmesi.getIdKolonu()));
        analizSonucu.setKimlikNoProfili(kimlikNoProfili);

        return analizSonucu;
    }

    /**
     * Aynı kimliğe düşen satırları tek gruba indirir. Kimlikler bu noktada zaten hesaplanmış
     * olduğu için tek geçiş yeterlidir -- veritabanına ek sorgu gitmez.
     *
     * <p>Yalnızca birden fazla kayıt içeren gruplar döner; tek kayıtlı bir grup kopya değildir.
     */
    private List<KopyaGrubu> kopyaGruplariniBul(List<SatirVerisi> satirlar, AlanEslestirmesi alanEslestirmesi) {
        // LinkedHashMap: eşit sayıdaki gruplar her çalıştırmada aynı sırada kalsın.
        Map<String, KopyaGrubu> gruplar = new LinkedHashMap<>();
        for (SatirVerisi satir : satirlar) {
            String kimlik = satir.getId();
            if (kimlik == null || kimlik.isBlank()) {
                continue;
            }
            KopyaGrubu grup = gruplar.computeIfAbsent(kimlik, k -> {
                KopyaGrubu yeni = new KopyaGrubu();
                yeni.setKimlik(k);
                yeni.setOrnekDeger(ornekDegerBul(satir, alanEslestirmesi));
                return yeni;
            });
            grup.arttir();
        }

        List<KopyaGrubu> kopyalar = new ArrayList<>();
        for (KopyaGrubu grup : gruplar.values()) {
            if (grup.getKayitSayisi() > 1) {
                kopyalar.add(grup);
            }
        }
        kopyalar.sort((a, b) -> Integer.compare(b.getKayitSayisi(), a.getKayitSayisi()));
        return kopyalar;
    }

    /**
     * Geçerlilik boyutunun hiç ölçülemeyeceği durumu tespit eder: biçim kuralı olan üç
     * kolondan hiçbiri denetime girmiyorsa. Telefon ayrıca ülke seçimine bağlı olduğu için
     * kolonun eşleştirilmiş olması tek başına yetmez.
     */
    private boolean hicBicimDenetimiYapilmadiMi(AlanEslestirmesi alanEslestirmesi,
                                                  boolean telefonKontroluYapilabilir) {
        boolean epostaVar = alanEslestirmesi.getePostaKolonu() != null;
        boolean webVar = alanEslestirmesi.getWebSitesiKolonu() != null;
        boolean telefonVar = alanEslestirmesi.getTelefonKolonu() != null && telefonKontroluYapilabilir;
        return !epostaVar && !webVar && !telefonVar;
    }

    private int fazlalikToplami(List<KopyaGrubu> gruplar) {
        int toplam = 0;
        for (KopyaGrubu grup : gruplar) {
            toplam += grup.getFazlalik();
        }
        return toplam;
    }

    /** Grubun ekranda tanınabilmesi için bir değer seçer; firma adı yoksa dolu olan ilk alan. */
    private String ornekDegerBul(SatirVerisi satir, AlanEslestirmesi alanEslestirmesi) {
        Map<String, String> alanlar = satir.getAlanlar();
        List<String> adaylar = new ArrayList<>();
        adaylar.add(alanEslestirmesi.getFirmaAdiKolonu());
        adaylar.addAll(alanEslestirmesi.getSiraliAdresKolonlari());
        adaylar.add(alanEslestirmesi.getePostaKolonu());
        adaylar.add(alanEslestirmesi.getTelefonKolonu());
        adaylar.add(alanEslestirmesi.getWebSitesiKolonu());
        for (String kolon : adaylar) {
            if (kolon == null) {
                continue;
            }
            String deger = alanlar.get(kolon);
            if (deger != null && !deger.isBlank()) {
                return deger.trim();
            }
        }
        return "";
    }

    /**
     * Benzer firma çiftlerini rapora değer hale getirir. İki ayrı eleme yapar:
     *
     * <p><b>1.</b> İki tarafın kimliği aynıysa satır atılır -- bunlar birebir kopyadır ve
     * kopya grupları bölümünde zaten sayılmıştır.
     *
     * <p><b>2.</b> Aynı kimlik çifti daha önce görüldüyse atılır. Sorgu fiziksel satırları
     * eşleştirdiği için, çok kopyalanmış iki firma aynı bilgiyi yüzlerce satırda tekrarlar.
     *
     * <p>Çift anahtarı sıradan bağımsızdır: (X,Y) ile (Y,X) aynı çifttir.
     */
    private List<BenzerFirmaCifti> ciftleriTekillestir(List<BenzerFirmaCifti> ciftler) {
        Set<String> gorulenCiftler = new HashSet<>();
        List<BenzerFirmaCifti> tekilCiftler = new ArrayList<>();

        for (BenzerFirmaCifti cift : ciftler) {
            String birinci = cift.getId_1();
            String ikinci = cift.getId_2();
            if (birinci == null || ikinci == null || birinci.isBlank() || ikinci.isBlank()) {
                continue;
            }
            if (birinci.equals(ikinci)) {
                continue;
            }
            String anahtar = birinci.compareTo(ikinci) < 0
                    ? birinci + "|" + ikinci
                    : ikinci + "|" + birinci;
            // add() zaten "yeni miydi" bilgisini döndürür; ayrıca contains() çağırmaya gerek yok.
            if (gorulenCiftler.add(anahtar)) {
                tekilCiftler.add(cift);
            }
        }
        return tekilCiftler;
    }

    private KolonProfili idKolonuProfiliniAl(VeritabaniBaglantiBilgisi bilgi, String tabloAdi, String idKolonu) {
        if (idKolonu == null || idKolonu.isBlank()) {
            return null;
        }
        return kolonProfilDao.kolonuProfille(bilgi, tabloAdi, idKolonu);
    }

    private List<MesajOzeti> ozetCikar(List<DogrulamaSonucu> sonuclar) {
        Map<String, MesajOzeti> gruplar = new LinkedHashMap<>();
        for (DogrulamaSonucu sonuc : sonuclar) {
            String anahtar = sonuc.getAlanAdi() + "|" + sonuc.getMesaj();
            MesajOzeti ozetSatiri = gruplar.computeIfAbsent(anahtar,
                    k -> new MesajOzeti(sonuc.getAlanAdi(), sonuc.getMesaj()));
            // Ayni mesaji ureten butun bulgular ayni eylemi tasir; ilk gorulen yeterli.
            ozetSatiri.setEylem(sonuc.getEylem());
            ozetSatiri.setEylemNotu(sonuc.getEylemNotu());
            ozetSatiri.arttir();
        }
        List<MesajOzeti> ozet = new ArrayList<>(gruplar.values());
        ozet.sort((a, b) -> Integer.compare(b.getAdet(), a.getAdet()));
        return ozet;
    }

    /**
     * Bir hücreyi önce yer tutucu kuralından, sonra kalan kurallardan geçirir ve aynı anda
     * skor kartını besler.
     *
     * <p>Sayım hücre başınadır, bulgu başına değil: bir hücrede iki tamlık bulgusu çıksa da
     * tamlık paydasına ve payına birer kez girer. Bulgu sayarsak çok kurallı bir alan
     * (e-posta) az kurallı bir alandan (adres) haksız yere daha kötü puan alırdı.
     *
     * <p>Yer tutucu bulunursa kalan kurallar hiç çalışmaz ve <b>paydalarına da girmez</b>:
     * "n/a" yazan bir hücrenin e-posta biçimi denetlenmemiştir, denetlenmiş gibi sayıp
     * sorunsuz kabul etmek geçerlilik puanını haksız yere yükseltirdi.
     */
    private void alaniKontrolEt(Kural yerTutucuKurali, String satirNo, String alanAdi, String deger,
                                List<Kural> kurallar, List<DogrulamaSonucu> sonuclar,
                                KaliteSkorKarti skorKarti) {

        EnumSet<KaliteBoyutuTuru> paydayaGirenler = EnumSet.of(yerTutucuKurali.boyut());
        EnumSet<KaliteBoyutuTuru> payaGirenler = EnumSet.noneOf(KaliteBoyutuTuru.class);
        skorKarti.kontrolEkle(yerTutucuKurali.boyut());

        Optional<DogrulamaSonucu> yerTutucuSonucu = yerTutucuKurali.kontrolEt(satirNo, alanAdi, deger);
        if (yerTutucuSonucu.isPresent()) {
            skorKarti.sorunEkle(yerTutucuKurali.boyut());
            sonuclar.add(yerTutucuSonucu.get());
            return;
        }

        for (Kural kural : kurallar) {
            KaliteBoyutuTuru boyut = kural.boyut();
            if (paydayaGirenler.add(boyut)) {
                skorKarti.kontrolEkle(boyut);
            }
            Optional<DogrulamaSonucu> sonuc = kural.kontrolEt(satirNo, alanAdi, deger);
            if (sonuc.isPresent()) {
                sonuclar.add(sonuc.get());
                if (payaGirenler.add(boyut)) {
                    skorKarti.sorunEkle(boyut);
                }
            }
        }
    }
}