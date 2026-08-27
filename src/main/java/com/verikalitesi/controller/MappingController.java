package com.verikalitesi.controller;

import com.verikalitesi.core.KalipKelimeBulucu;
import com.verikalitesi.core.UlkeKoduCozumleyici;
import com.verikalitesi.core.VeritabaniBaglantiBilgisi;
import com.verikalitesi.core.VeritabaniBaglantiHatasi;
import com.verikalitesi.dao.KelimeFrekansiDao;
import com.verikalitesi.dao.SemaDao;
import com.verikalitesi.dto.AlanEslestirmesi;
import com.verikalitesi.dto.AnalizSonucu;
import com.verikalitesi.dto.KalipTespiti;
import com.verikalitesi.dto.KelimeFrekansi;
import com.verikalitesi.dto.KolonBilgisi;
import com.verikalitesi.service.ValidationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Controller
public class MappingController {

    //VeritabaniBaglantiBilgisi bilgi = (VeritabaniBaglantiBilgisi) session.getAttribute("baglantiBilgisi");  //oturumun açık olması için

    // Cunstrocter İnjection

    private final SemaDao semaDao;   // bu controller ın yaptığı işler için bu final alanlara ihtiyacımız var
    private final ValidationService validationService;
    private final KelimeFrekansiDao kelimeFrekansiDao;
    private final KalipKelimeBulucu kalipKelimeBulucu;


    public MappingController(SemaDao semaDao, ValidationService validationService,
                             KelimeFrekansiDao kelimeFrekansiDao, KalipKelimeBulucu kalipKelimeBulucu) {
        this.semaDao = semaDao;
        this.validationService = validationService;
        this.kelimeFrekansiDao = kelimeFrekansiDao;
        this.kalipKelimeBulucu = kalipKelimeBulucu;

    }



    @GetMapping("/tablolar")
    public String tablolariGoster(HttpSession session, Model model) throws VeritabaniBaglantiHatasi {
        VeritabaniBaglantiBilgisi bilgi = (VeritabaniBaglantiBilgisi) session.getAttribute("baglantiBilgisi");
        // Oturum zaman asimina ugramis ya da kullanici dogrudan bu adrese gelmis
        // olabilir. Kontrol olmadan DAO'ya null baglanti bilgisi gidiyor ve
        // kullanici ne oldugunu anlamadigi bir hata ekraniyla karsilasiyordu.
        if (bilgi == null) {
            return "redirect:/baglan";
        }

        List<String> tabloListesi = semaDao.tablolariGetir(bilgi, "public");  // bağlandığımız veritabanı bilgisiyle tabloları getiriyoruz
        model.addAttribute("tablolar", tabloListesi);

        return "tables";



    }

    @PostMapping("/tablolar")
    public String tabloSec( HttpSession session , String tabloAdi){
        session.setAttribute("tabloAdi", tabloAdi);
        return "redirect:/eslestirme";
    }

    @GetMapping("/eslestirme")
    public String eslestirmeFormuGoster( HttpSession session,Model model) throws VeritabaniBaglantiHatasi{
        VeritabaniBaglantiBilgisi bilgi = (VeritabaniBaglantiBilgisi)session.getAttribute("baglantiBilgisi");
        String tabloAdi = (String) session.getAttribute("tabloAdi");
        if (bilgi == null) {
            return "redirect:/baglan";
        }
        if (tabloAdi == null) {
            return "redirect:/tablolar";
        }
        // Seçilen tablonun kolonları eşleştirme formunu besliyor.
        List<KolonBilgisi> kolonlar = semaDao.kolonlariGetir(bilgi ,"public", tabloAdi);

        // ekranda göstermeyi yapıyorum model ile
        model.addAttribute("kolonlar", kolonlar);
        model.addAttribute("tabloAdi", tabloAdi);
        model.addAttribute("ulkeler", UlkeKoduCozumleyici.tumUlkeler());
        model.addAttribute("tahminEdilenUlkeKodu", UlkeKoduCozumleyici.tabloAdindanUlkeKodunuBul(tabloAdi));

        return "mapping";







    }


    @PostMapping("/eslestirme")
    public String analizEt(AlanEslestirmesi alanEslestirmesi, HttpSession session, Model model) throws VeritabaniBaglantiHatasi {
        VeritabaniBaglantiBilgisi bilgi = (VeritabaniBaglantiBilgisi) session.getAttribute("baglantiBilgisi");
        String tabloAdi = (String) session.getAttribute("tabloAdi");
        if (bilgi == null) {
            return "redirect:/baglan";
        }
        if (tabloAdi == null) {
            return "redirect:/tablolar";
        }
        if (alanEslestirmesi.getIdKolonu() != null && alanEslestirmesi.getIdKolonu().isEmpty()) {
            alanEslestirmesi.setIdKolonu(null);
        }

        if (alanEslestirmesi.getFirmaAdiKolonu() != null && alanEslestirmesi.getFirmaAdiKolonu().isEmpty()) {
            alanEslestirmesi.setFirmaAdiKolonu(null);
        }
        if (alanEslestirmesi.getePostaKolonu() != null && alanEslestirmesi.getePostaKolonu().isEmpty()) {
            alanEslestirmesi.setePostaKolonu(null);
        }
        if (alanEslestirmesi.getTelefonKolonu() != null && alanEslestirmesi.getTelefonKolonu().isEmpty()) {
            alanEslestirmesi.setTelefonKolonu(null);
        }
        // Çoklu seçimde hiçbir şey seçilmezse Spring boş metin gönderebiliyor; ayıklıyoruz.
        List<String> adresKolonlari = new ArrayList<>();
        for (String kolon : alanEslestirmesi.getAdresKolonlari()) {
            if (kolon != null && !kolon.isBlank()) {
                adresKolonlari.add(kolon);
            }
        }
        alanEslestirmesi.setAdresKolonlari(adresKolonlari);
        if (alanEslestirmesi.getWebSitesiKolonu() != null && alanEslestirmesi.getWebSitesiKolonu().isEmpty()) {
            alanEslestirmesi.setWebSitesiKolonu(null);
        }
        if (alanEslestirmesi.getUlkeKodu() != null && alanEslestirmesi.getUlkeKodu().isEmpty()) {
            alanEslestirmesi.setUlkeKodu(null);
        }

        // Firma adi kolonu metin olmak ZORUNDA: benzerlik sorgusu o kolon uzerinde
        // regexp_replace ve similarity calistiriyor, ikisi de sayisal kolonda
        // calismaz. Kontrol olmadan kullanici sayisal bir kolon secerse veritabani
        // hatasi aliyordu; simdi ayni ekranda anlasilir bir uyari gosteriliyor.
        String tipHatasi = firmaAdiTipiniDogrula(bilgi, tabloAdi, alanEslestirmesi.getFirmaAdiKolonu());
        if (tipHatasi != null) {
            return eslestirmeFormunaDon(bilgi, tabloAdi, model, tipHatasi);
        }

        session.setAttribute("alanEslestirmesi", alanEslestirmesi);

        if (alanEslestirmesi.getFirmaAdiKolonu() == null) {
            AnalizSonucu sonuc = validationService.analizEt(bilgi, tabloAdi, alanEslestirmesi, null);
            // Sonuc oturumda da saklaniyor: birlestirme ekrani rapora geri donerken
            // analizi bastan calistirmasin (5-9 saniye) ve kullanici hangi cifti
            // birlestirdigini ekranda gormeye devam etsin.
            session.setAttribute("analizSonucu", sonuc);
            model.addAttribute("sonuc", sonuc);
            return "rapor";
        }

        List<KelimeFrekansi> kelimeler = kelimeleriYuzdeliGetir(bilgi, tabloAdi, alanEslestirmesi.getFirmaAdiKolonu());
        model.addAttribute("kelimeler", kelimeler);
        model.addAttribute("toplam", kelimeFrekansiDao.toplamFirmaSayisiniGetir(bilgi, tabloAdi, alanEslestirmesi.getFirmaAdiKolonu()));
        KalipTespiti tespit = kalipKelimeBulucu.kalipKelimeleriBul(kelimeler);
        model.addAttribute("kalipKelimeler", tespit.getKelimeAdlari());
        model.addAttribute("tespit", tespit);
        return "kelimeSecimi";
    }

    @PostMapping("/kelimeSecimi")
    public String kelimeSeciminiIsle(@RequestParam(required = false) List<String> haricTutulacakKelimeler, HttpSession session, Model model) throws VeritabaniBaglantiHatasi {
        VeritabaniBaglantiBilgisi bilgi = (VeritabaniBaglantiBilgisi) session.getAttribute("baglantiBilgisi");
        String tabloAdi = (String) session.getAttribute("tabloAdi");
        AlanEslestirmesi alanEslestirmesi = (AlanEslestirmesi) session.getAttribute("alanEslestirmesi");
        if (bilgi == null || tabloAdi == null || alanEslestirmesi == null) {
            return "redirect:/baglan";
        }

        List<KelimeFrekansi> kelimeler = kelimeleriYuzdeliGetir(bilgi, tabloAdi, alanEslestirmesi.getFirmaAdiKolonu());
        KalipTespiti tespit = kalipKelimeBulucu.kalipKelimeleriBul(kelimeler);
        List<KelimeFrekansi> kalipKelimeler = tespit.getKalipKelimeler();

        // LinkedHashSet: aynı kelime hem kullanıcıdan hem kalıp tespitinden gelebilir,
        // tekrarı önler ve sırayı korur.
        Set<String> kelimeSeti = new LinkedHashSet<>();
        if (haricTutulacakKelimeler != null) {
            kelimeSeti.addAll(haricTutulacakKelimeler);
        }
        kelimeSeti.addAll(kalipKelimeAdlari(kalipKelimeler));

        AnalizSonucu sonuc = validationService.analizEt(bilgi, tabloAdi, alanEslestirmesi, new ArrayList<>(kelimeSeti));
        sonuc.setHaricTutulanKelimeler(haricTutulanlariRaporla(kelimeler, kelimeSeti, kalipKelimeler));

        session.setAttribute("analizSonucu", sonuc);
        model.addAttribute("sonuc", sonuc);
        return "rapor";
    }


    private List<KelimeFrekansi> kelimeleriYuzdeliGetir(VeritabaniBaglantiBilgisi bilgi, String tabloAdi, String firmaAdiKolonu) throws VeritabaniBaglantiHatasi {
        List<KelimeFrekansi> kelimeler = kelimeFrekansiDao.kelimeleriGetir(bilgi, tabloAdi, firmaAdiKolonu);
        int toplam = kelimeFrekansiDao.toplamFirmaSayisiniGetir(bilgi, tabloAdi, firmaAdiKolonu);
        if (toplam <= 0) {
            return kelimeler;
        }
        for (KelimeFrekansi kf : kelimeler) {
            kf.setYuzde((kf.getTekrarSayisi() * 100.0) / toplam);
        }
        return kelimeler;
    }

    private List<String> kalipKelimeAdlari(List<KelimeFrekansi> kalipKelimeler) {
        List<String> adlar = new ArrayList<>();
        for (KelimeFrekansi kf : kalipKelimeler) {
            adlar.add(kf.getKelime());
        }
        return adlar;
    }

    /**
     * Raporda gösterilecek hariç tutulan kelime listesini kurar. Frekans listesinde yer almayan
     * bir kelime gelirse (kullanıcı seçim ekranındaki listeden farklı bir değer göndermişse)
     * yüzdesi bilinmediği için sıfır kalır ama listeden düşmez -- rapor eksik kalmamalı.
     */
    private List<KelimeFrekansi> haricTutulanlariRaporla(List<KelimeFrekansi> tumKelimeler,
                                                          Set<String> haricTutulanlar,
                                                          List<KelimeFrekansi> kalipKelimeler) {
        Set<String> kalipSeti = new HashSet<>(kalipKelimeAdlari(kalipKelimeler));

        List<KelimeFrekansi> rapor = new ArrayList<>();
        for (String kelime : haricTutulanlar) {
            KelimeFrekansi satir = new KelimeFrekansi();
            satir.setKelime(kelime);
            satir.setOtomatikSecildi(kalipSeti.contains(kelime));
            for (KelimeFrekansi kaynak : tumKelimeler) {
                if (kaynak.getKelime().equals(kelime)) {
                    satir.setTekrarSayisi(kaynak.getTekrarSayisi());
                    satir.setYuzde(kaynak.getYuzde());
                    break;
                }
            }
            rapor.add(satir);
        }
        rapor.sort((a, b) -> Double.compare(b.getYuzde(), a.getYuzde()));
        return rapor;
    }

    /**
     * Firma adi kolonunun metin tipinde olup olmadigini denetler.
     *
     * @return kullaniciya gosterilecek hata mesaji; sorun yoksa {@code null}
     */
    private String firmaAdiTipiniDogrula(VeritabaniBaglantiBilgisi bilgi, String tabloAdi,
                                         String firmaAdiKolonu) throws VeritabaniBaglantiHatasi {
        if (firmaAdiKolonu == null || firmaAdiKolonu.isBlank()) {
            return null;
        }
        for (KolonBilgisi kolon : semaDao.kolonlariGetir(bilgi, "public", tabloAdi)) {
            if (!firmaAdiKolonu.equals(kolon.getKolonAdi())) {
                continue;
            }
            // Locale.ROOT: Turkce dil ayarinda buyuk I noktasiz i'ye donusup
            // "CHARACTER" gibi tipleri eslesmez hale getirebilir.
            String tip = kolon.getVeriTipi() == null
                    ? "" : kolon.getVeriTipi().toLowerCase(java.util.Locale.ROOT);
            if (tip.contains("char") || tip.contains("text")) {
                return null;
            }
            return "Firma Adı kolonu metin tipinde olmalı. Seçtiğiniz \"" + firmaAdiKolonu
                    + "\" kolonunun tipi \"" + kolon.getVeriTipi() + "\". Benzer firma taraması "
                    + "bu kolon üzerinde metin karşılaştırması yapıyor; sayısal ya da tarih "
                    + "tipli bir kolonda çalışamaz.";
        }
        return null;
    }

    /** Esleştirme formunu hata mesajiyla birlikte yeniden gosterir. */
    private String eslestirmeFormunaDon(VeritabaniBaglantiBilgisi bilgi, String tabloAdi,
                                        Model model, String hata) throws VeritabaniBaglantiHatasi {
        model.addAttribute("kolonlar", semaDao.kolonlariGetir(bilgi, "public", tabloAdi));
        model.addAttribute("tabloAdi", tabloAdi);
        model.addAttribute("ulkeler", UlkeKoduCozumleyici.tumUlkeler());
        model.addAttribute("tahminEdilenUlkeKodu", UlkeKoduCozumleyici.tabloAdindanUlkeKodunuBul(tabloAdi));
        model.addAttribute("hata", hata);
        return "mapping";
    }
}
