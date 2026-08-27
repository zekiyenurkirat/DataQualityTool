package com.verikalitesi.controller;

import com.verikalitesi.altinkayit.AlanSecenegi;
import com.verikalitesi.altinkayit.AltinKayitKarari;
import com.verikalitesi.altinkayit.AltinKayitSentezleyici;
import com.verikalitesi.altinkayit.KopyaKumesi;
import com.verikalitesi.altinkayit.KumeBulucu;
import com.verikalitesi.dto.AlanEslestirmesi;
import com.verikalitesi.rule.PlaceholderKurali;
import com.verikalitesi.core.VeritabaniBaglantiBilgisi;
import com.verikalitesi.core.VeritabaniBaglantiHatasi;
import com.verikalitesi.dao.BirlestirmeDao;
import com.verikalitesi.dto.AnalizSonucu;
import com.verikalitesi.dto.BenzerFirmaCifti;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Benzer kayıt çiftlerinin ana kayıt (golden record) altında birleştirilmesi.
 *
 * <p>Birleştirme <strong>hiçbir satırı silmez ve kaynak tablonun yapısına
 * dokunmaz.</strong> Kaybeden kayıt, aracın kendi çapraz referans (XREF)
 * tablosunda pasife çekilip kazanan kaydın kimliğine bağlanır.
 */
@Controller
public class BirlestirmeController {

    private final BirlestirmeDao birlestirmeDao;
    private final List<String> yerTutucular;

    public BirlestirmeController(BirlestirmeDao birlestirmeDao,
                                 @org.springframework.beans.factory.annotation.Value("${veri.yerTutucular:}")
                                 List<String> yerTutucular) {
        this.birlestirmeDao = birlestirmeDao;
        this.yerTutucular = yerTutucular;
    }

    // ==================================================================
    // Alan seviyesinde birleştirme (field-level survivorship)
    // ==================================================================

    /**
     * Kümenin birleştirme ekranını açar.
     *
     * <p>Kümeler oturumdaki çift listesinden her istekte yeniden çıkarılıyor.
     * Union-find deterministik olduğu için aynı liste hep aynı kümeleri üretir;
     * ayrıca saklamaya gerek yok ve oturumda bayat veri kalmıyor.
     */
    @org.springframework.web.bind.annotation.GetMapping("/birlestir/gelismis")
    public String gelismisEkran(@RequestParam String kimlik, HttpSession session, Model model) {
        AnalizSonucu sonuc = (AnalizSonucu) session.getAttribute("analizSonucu");
        if (sonuc == null) {
            return "redirect:/tablolar";
        }
        // Rapor ekrani cift listesi gosteriyor, kume anahtarini bilmiyor; bu yuzden
        // baglanti tek bir kimlik tasiyor ve kumeyi burada buluyoruz.
        KopyaKumesi kume = kumeyiKimlikleBul(sonuc, kimlik);
        if (kume == null) {
            return hataylaDon(model, sonuc, "Küme bulunamadı; daha önce birleştirilmiş olabilir.");
        }

        AltinKayitSentezleyici sentezleyici = sentezleyiciKur(session);
        model.addAttribute("kume", kume);
        model.addAttribute("secenekler", sentezleyici.secenekleriUret(kume));
        return "gelismisBirlestirme";
    }

    /** Kullanıcının alan alan yaptığı seçimlerden ana kaydı sentezleyip yazar. */
    @PostMapping("/birlestir/gelismis")
    public String gelismisUygula(@RequestParam String anahtar,
                                 @RequestParam Map<String, String> tumParametreler,
                                 HttpSession session, Model model) {

        VeritabaniBaglantiBilgisi bilgi = (VeritabaniBaglantiBilgisi) session.getAttribute("baglantiBilgisi");
        String tabloAdi = (String) session.getAttribute("tabloAdi");
        AnalizSonucu sonuc = (AnalizSonucu) session.getAttribute("analizSonucu");
        if (bilgi == null || tabloAdi == null || sonuc == null) {
            return "redirect:/tablolar";
        }

        KopyaKumesi kume = kumeyiBul(sonuc, anahtar);
        if (kume == null) {
            return hataylaDon(model, sonuc, "Küme bulunamadı; daha önce birleştirilmiş olabilir.");
        }

        AltinKayitSentezleyici sentezleyici = sentezleyiciKur(session);
        List<AlanSecenegi> secenekler = sentezleyici.secenekleriUret(kume);

        Map<String, Integer> secimler = secimleriCoz(tumParametreler);
        Map<String, String> degerler = sentezleyici.altinKayitKur(secenekler, secimler);
        Map<String, String> kaynaklar = kaynaklariCoz(secenekler, secimler, kume);

        String altinKayitId = kume.getAltinKayitKimligi();
        String gerekce = "Alan seviyesinde birleştirme: " + kume.getKayitSayisi()
                + " kayıttan sentezlendi. Değerler onarım motorundan geçirildi.";
        try {
            birlestirmeDao.altinKaydiSentezle(bilgi, tabloAdi, altinKayitId,
                    degerler, kaynaklar, kume.getKimlikler(), gerekce);
        } catch (VeritabaniBaglantiHatasi | RuntimeException hata) {
            return hataylaDon(model, sonuc, "Ana kayıt yazılamadı: " + hata.getMessage());
        }

        // Kümenin bütün üyelerine dokunan çiftler listeden çıkıyor: karar verildi.
        List<BenzerFirmaCifti> kalanlar = new ArrayList<>();
        for (BenzerFirmaCifti cift : sonuc.getBenzerFirmaCiftleri()) {
            boolean kumedeMi = kume.getKimlikler().contains(cift.getId_1())
                    || kume.getKimlikler().contains(cift.getId_2());
            if (!kumedeMi) {
                kalanlar.add(cift);
            }
        }
        sonuc.setBenzerFirmaCiftleri(kalanlar);

        model.addAttribute("sonuc", sonuc);
        model.addAttribute("birlestirmeSayaci", birlestirmeSayaciniArttir(session));
        model.addAttribute("birlestirmeMesaji",
                kume.getKayitSayisi() + " kayıttan " + altinKayitId + " kimlikli Ana Kayıt "
                        + "(Golden Record) sentezlendi; her alan ayrı ayrı seçildi ve onarım "
                        + "motorundan geçirildi. Kaynak satırların hiçbiri silinmedi, kaynak "
                        + "tabloya satır da eklenmedi.");
        return "rapor";
    }

    private AltinKayitSentezleyici sentezleyiciKur(HttpSession session) {
        AlanEslestirmesi eslestirme = (AlanEslestirmesi) session.getAttribute("alanEslestirmesi");
        return new AltinKayitSentezleyici(new PlaceholderKurali(yerTutucular), eslestirme, "E164");
    }

    /** Rapordan gelen tek kimliği içeren kümeyi bulur. */
    private KopyaKumesi kumeyiKimlikleBul(AnalizSonucu sonuc, String kimlik) {
        for (KopyaKumesi kume : new KumeBulucu().kumeleriBul(sonuc.getBenzerFirmaCiftleri())) {
            if (kume.getKimlikler().contains(kimlik)) {
                return kume;
            }
        }
        return null;
    }

    /** Gelişmiş ekrandan vazgeçildiğinde rapora geri döner; hiçbir şey yazılmaz. */
    @org.springframework.web.bind.annotation.GetMapping("/birlestir/iptal")
    public String iptal(HttpSession session, Model model) {
        AnalizSonucu sonuc = (AnalizSonucu) session.getAttribute("analizSonucu");
        if (sonuc == null) {
            return "redirect:/tablolar";
        }
        model.addAttribute("sonuc", sonuc);
        return "rapor";
    }

    private KopyaKumesi kumeyiBul(AnalizSonucu sonuc, String anahtar) {
        for (KopyaKumesi kume : new KumeBulucu().kumeleriBul(sonuc.getBenzerFirmaCiftleri())) {
            if (kume.getAnahtar().equals(anahtar)) {
                return kume;
            }
        }
        return null;
    }

    /** Form alanları "secim_<kolon>" adıyla geliyor; diğer parametreler yok sayılıyor. */
    private Map<String, Integer> secimleriCoz(Map<String, String> tumParametreler) {
        Map<String, Integer> secimler = new java.util.HashMap<>();
        for (Map.Entry<String, String> giris : tumParametreler.entrySet()) {
            if (!giris.getKey().startsWith("secim_")) {
                continue;
            }
            try {
                secimler.put(giris.getKey().substring("secim_".length()),
                        Integer.parseInt(giris.getValue()));
            } catch (NumberFormatException yoksay) {
                // Bozuk bir indeks gelirse o kolon aracın önerisine düşer.
            }
        }
        return secimler;
    }

    /** Her kolonun değerinin hangi kaynak kayıttan geldiği (alan seviyesinde soyağacı). */
    private Map<String, String> kaynaklariCoz(List<AlanSecenegi> secenekler,
                                              Map<String, Integer> secimler, KopyaKumesi kume) {
        Map<String, String> kaynaklar = new java.util.LinkedHashMap<>();
        for (AlanSecenegi secenek : secenekler) {
            Integer indeks = secimler.get(secenek.getKolonAdi());
            int secilen = (indeks == null || indeks < 0 || indeks >= secenek.getDegerler().size())
                    ? secenek.getOnerilenIndeks()
                    : indeks;
            kaynaklar.put(secenek.getKolonAdi(), secenek.getDegerler().get(secilen).getKaynakKimlik());
        }
        return kaynaklar;
    }

    /**
     * @param altinKayit birleşmede kalacak (ana) kaydın kimliği
     * @param pasifKayit ana kayda bağlanacak kaydın kimliği
     */
    @PostMapping("/birlestir")
    public String birlestir(@RequestParam String altinKayit,
                            @RequestParam String pasifKayit,
                            HttpSession session, Model model) {

        VeritabaniBaglantiBilgisi bilgi = (VeritabaniBaglantiBilgisi) session.getAttribute("baglantiBilgisi");
        String tabloAdi = (String) session.getAttribute("tabloAdi");
        AnalizSonucu sonuc = (AnalizSonucu) session.getAttribute("analizSonucu");

        // Oturum düşmüşse ya da kullanıcı adım atlamışsa rapora dönecek veri yok.
        if (bilgi == null || tabloAdi == null || sonuc == null) {
            return "redirect:/tablolar";
        }

        if (altinKayit.equals(pasifKayit)) {
            return hataylaDon(model, sonuc, "Bir kayıt kendisiyle birleştirilemez.");
        }

        BenzerFirmaCifti cift = ciftiBul(sonuc, altinKayit, pasifKayit);
        if (cift == null) {
            return hataylaDon(model, sonuc,
                    "Bu çift listede bulunamadı. Daha önce birleştirilmiş ya da rapor yenilenmiş olabilir.");
        }

        try {
            birlestirmeDao.birlestirmeyiKaydet(bilgi, tabloAdi, altinKayit, pasifKayit,
                    gerekceUret(cift, altinKayit));
        } catch (VeritabaniBaglantiHatasi | RuntimeException hata) {
            return hataylaDon(model, sonuc, "Birleştirme kaydedilemedi: " + hata.getMessage());
        }

        // Çift listeden çıkarılıyor: karar verildi, artık incelenecek bir şey yok.
        // Liste değiştirilmek yerine yenisiyle değiştiriliyor -- gelen listenin
        // değiştirilebilir olduğuna güvenmek gereksiz bir varsayım olurdu.
        List<BenzerFirmaCifti> kalanlar = new ArrayList<>(sonuc.getBenzerFirmaCiftleri());
        kalanlar.remove(cift);
        sonuc.setBenzerFirmaCiftleri(kalanlar);

        int sayac = birlestirmeSayaciniArttir(session);

        model.addAttribute("sonuc", sonuc);
        model.addAttribute("birlestirmeMesaji",
                altinKayit + " kimlikli kayıt Ana Kayıt (Golden Record) ilan edildi; "
                        + pasifKayit + " kimlikli kayıt bu kayda bağlanıp pasife çekildi. "
                        + "Hiçbir satır silinmedi, kaynak tablo değişmedi.");
        model.addAttribute("birlestirmeSayaci", sayac);
        return "rapor";
    }

    /**
     * Denetim izine yazılacak gerekçe.
     *
     * <p>Kullanıcı aracın önerdiğinden başka bir kaydı seçtiyse bu mutlaka
     * kayda geçmeli; altı ay sonra kararın kimin verdiği sorulduğunda cevap
     * kayıtta durmalı.
     */
    private String gerekceUret(BenzerFirmaCifti cift, String secilenAltinKayit) {
        if (!cift.isAltinKayitKarariVarMi()) {
            return "Kullanıcı seçimi.";
        }
        AltinKayitKarari karar = cift.getAltinKayitKarari();
        String gerekce = karar.getGerekce();
        if (!secilenAltinKayit.equals(karar.getKazananKimlik())) {
            return "Kullanıcı, aracın önerdiği kayıt yerine bunu seçti. Aracın gerekçesi: " + gerekce;
        }
        if (!karar.isGuvenilirKarar()) {
            return "Kullanıcı onayladı. " + gerekce;
        }
        return gerekce;
    }

    private String hataylaDon(Model model, AnalizSonucu sonuc, String mesaj) {
        model.addAttribute("sonuc", sonuc);
        model.addAttribute("birlestirmeHatasi", mesaj);
        return "rapor";
    }

    private int birlestirmeSayaciniArttir(HttpSession session) {
        Integer mevcut = (Integer) session.getAttribute("birlestirmeSayaci");
        int yeni = (mevcut == null ? 0 : mevcut) + 1;
        session.setAttribute("birlestirmeSayaci", yeni);
        return yeni;
    }

    private BenzerFirmaCifti ciftiBul(AnalizSonucu sonuc, String kimlikA, String kimlikB) {
        for (BenzerFirmaCifti cift : sonuc.getBenzerFirmaCiftleri()) {
            boolean duzSira = kimlikA.equals(cift.getId_1()) && kimlikB.equals(cift.getId_2());
            boolean tersSira = kimlikA.equals(cift.getId_2()) && kimlikB.equals(cift.getId_1());
            if (duzSira || tersSira) {
                return cift;
            }
        }
        return null;
    }
}
