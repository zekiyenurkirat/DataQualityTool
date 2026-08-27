package com.verikalitesi.controller;

import com.verikalitesi.core.VeritabaniBaglantiBilgisi;
import com.verikalitesi.core.VeritabaniBaglantiHatasi;
import com.verikalitesi.dao.SemaDao;
import com.verikalitesi.dto.AlanEslestirmesi;
import com.verikalitesi.dto.HarfDonusumu;
import com.verikalitesi.dto.KolonBilgisi;
import com.verikalitesi.dto.KolonTemizlemeSonucu;
import com.verikalitesi.dto.TemizlemeKurali;
import com.verikalitesi.dto.TemizlemeOnizlemeSonucu;
import com.verikalitesi.dto.TemizlemePlani;
import com.verikalitesi.dto.TemizlemeSonucu;
import com.verikalitesi.service.TemizlemeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class TemizlemeController {

    private final SemaDao semaDao;
    private final TemizlemeService temizlemeService;

    public TemizlemeController(SemaDao semaDao, TemizlemeService temizlemeService) {
        this.semaDao = semaDao;
        this.temizlemeService = temizlemeService;
    }

    @GetMapping("/temizleme")
    public String kolonSecimFormuGoster(HttpSession session, Model model) throws VeritabaniBaglantiHatasi {
        VeritabaniBaglantiBilgisi bilgi = (VeritabaniBaglantiBilgisi) session.getAttribute("baglantiBilgisi");
        String tabloAdi = (String) session.getAttribute("tabloAdi");
        AlanEslestirmesi alanEslestirmesi = (AlanEslestirmesi) session.getAttribute("alanEslestirmesi");

        if (bilgi == null || tabloAdi == null) {
            return "redirect:/tablolar";
        }

        List<KolonBilgisi> kolonlar = metinTipindeKolonlariFiltrele(semaDao.kolonlariGetir(bilgi, "public", tabloAdi));

        model.addAttribute("kolonlar", kolonlar);
        model.addAttribute("onerilenKolonlar", onerilenKolonlariBul(alanEslestirmesi));
        return "temizlemeKolonSecimi";
    }

    @PostMapping("/temizleme")
    public String kolonSeciminiIsle(@RequestParam(required = false) List<String> secilenKolonlar,
                                     HttpSession session, Model model) throws VeritabaniBaglantiHatasi {

        if (secilenKolonlar == null || secilenKolonlar.isEmpty()) {
            VeritabaniBaglantiBilgisi bilgi = (VeritabaniBaglantiBilgisi) session.getAttribute("baglantiBilgisi");
            String tabloAdi = (String) session.getAttribute("tabloAdi");
            AlanEslestirmesi alanEslestirmesi = (AlanEslestirmesi) session.getAttribute("alanEslestirmesi");

            if (bilgi == null || tabloAdi == null) {
                return "redirect:/tablolar";
            }

            List<KolonBilgisi> kolonlar = metinTipindeKolonlariFiltrele(semaDao.kolonlariGetir(bilgi, "public", tabloAdi));
            model.addAttribute("kolonlar", kolonlar);
            model.addAttribute("onerilenKolonlar", onerilenKolonlariBul(alanEslestirmesi));
            model.addAttribute("hata", "En az bir kolon seçmeniz gerekiyor.");
            return "temizlemeKolonSecimi";
        }

        session.setAttribute("secilenTemizlemeKolonlari", secilenKolonlar);
        return "redirect:/temizleme/kurallar";
    }

    @GetMapping("/temizleme/kurallar")
    public String kuralFormuGoster(HttpSession session, Model model) {
        List<String> secilenKolonlar = (List<String>) session.getAttribute("secilenTemizlemeKolonlari");
        if (secilenKolonlar == null || secilenKolonlar.isEmpty()) {
            return "redirect:/temizleme";
        }

        AlanEslestirmesi alanEslestirmesi = (AlanEslestirmesi) session.getAttribute("alanEslestirmesi");

        List<TemizlemeKurali> kurallar = new ArrayList<>();
        for (String kolonAdi : secilenKolonlar) {
            TemizlemeKurali kural = new TemizlemeKurali();
            kural.setKolonAdi(kolonAdi);
            kural.setBosluklariKirp(true);
            kural.setArdisikBosluklariDaralt(true);
            kurallar.add(kural);
        }

        TemizlemePlani plan = new TemizlemePlani();
        plan.setTemizlemeKurallari(kurallar);

        model.addAttribute("plan", plan);
        model.addAttribute("telefonKolonu", alanEslestirmesi != null ? alanEslestirmesi.getTelefonKolonu() : null);
        model.addAttribute("epostaKolonu", alanEslestirmesi != null ? alanEslestirmesi.getePostaKolonu() : null);
        model.addAttribute("webSitesiKolonu", alanEslestirmesi != null ? alanEslestirmesi.getWebSitesiKolonu() : null);
        model.addAttribute("harfDonusumleri", HarfDonusumu.values());

        boolean kimlikKolonuVar = alanEslestirmesi != null
                && alanEslestirmesi.getIdKolonu() != null
                && !alanEslestirmesi.getIdKolonu().isBlank();
        model.addAttribute("kimlikKolonuVar", kimlikKolonuVar);

        boolean ulkeSecildi = alanEslestirmesi != null
                && alanEslestirmesi.getUlkeKodu() != null
                && !alanEslestirmesi.getUlkeKodu().isBlank();
        model.addAttribute("ulkeSecildi", ulkeSecildi);

        return "temizlemeKurallari";
    }

    @PostMapping("/temizleme/kurallar")
    public String onizlemeOlustur(@ModelAttribute("plan") TemizlemePlani plan, HttpSession session, Model model) {
        VeritabaniBaglantiBilgisi bilgi = (VeritabaniBaglantiBilgisi) session.getAttribute("baglantiBilgisi");
        String tabloAdi = (String) session.getAttribute("tabloAdi");
        AlanEslestirmesi alanEslestirmesi = (AlanEslestirmesi) session.getAttribute("alanEslestirmesi");

        if (bilgi == null || tabloAdi == null || alanEslestirmesi == null) {
            return "redirect:/tablolar";
        }

        List<TemizlemeOnizlemeSonucu> onizlemeler = temizlemeService.onizlemeOlustur(bilgi, tabloAdi,
                alanEslestirmesi.getIdKolonu(), alanEslestirmesi.getUlkeKodu(), plan.getTemizlemeKurallari());

        // Örnekler burada saklanmazsa bir daha elde edilemez: güncelleme bittiğinde eski değer
        // veritabanında kalmıyor ve araçta geri alma yok. Sonuç ekranı bu kayda dayanıyor.
        session.setAttribute("temizlemePlani", plan);
        session.setAttribute("temizlemeOnizlemeleri", onizlemeler);
        model.addAttribute("onizlemeler", onizlemeler);
        return "temizlemeOnizleme";
    }

    @PostMapping("/temizleme/uygula")
    public String uygula(HttpSession session, Model model) {
        VeritabaniBaglantiBilgisi bilgi = (VeritabaniBaglantiBilgisi) session.getAttribute("baglantiBilgisi");
        String tabloAdi = (String) session.getAttribute("tabloAdi");
        AlanEslestirmesi alanEslestirmesi = (AlanEslestirmesi) session.getAttribute("alanEslestirmesi");
        TemizlemePlani plan = (TemizlemePlani) session.getAttribute("temizlemePlani");

        if (bilgi == null || tabloAdi == null || alanEslestirmesi == null || plan == null) {
            return "redirect:/tablolar";
        }

        @SuppressWarnings("unchecked")
        List<TemizlemeOnizlemeSonucu> onizlemeler =
                (List<TemizlemeOnizlemeSonucu>) session.getAttribute("temizlemeOnizlemeleri");

        TemizlemeSonucu sonuc = temizlemeService.temizlemeyiUygula(bilgi, tabloAdi, alanEslestirmesi.getIdKolonu(),
                alanEslestirmesi.getUlkeKodu(), plan.getTemizlemeKurallari());

        ornekleriSonucaTasi(sonuc, onizlemeler);

        session.removeAttribute("secilenTemizlemeKolonlari");
        session.removeAttribute("temizlemePlani");
        session.removeAttribute("temizlemeOnizlemeleri");

        model.addAttribute("sonuc", sonuc);
        model.addAttribute("toplamGuncellenen", toplamGuncellenen(sonuc));
        return "temizlemeSonucu";
    }

    /**
     * Önizlemede alınan eski/yeni örnekleri, aynı kolonun uygulama sonucuna bağlar.
     * Kolon adı üzerinden eşleştirilir; önizleme yoksa sonuç örneksiz kalır ama boş liste
     * taşıdığı için ekran yine de sorunsuz çizilir.
     */
    private void ornekleriSonucaTasi(TemizlemeSonucu sonuc, List<TemizlemeOnizlemeSonucu> onizlemeler) {
        if (onizlemeler == null || sonuc.getKolonSonuclari() == null) {
            return;
        }
        for (KolonTemizlemeSonucu kolonSonucu : sonuc.getKolonSonuclari()) {
            for (TemizlemeOnizlemeSonucu onizleme : onizlemeler) {
                if (onizleme.getKolonAdi() != null && onizleme.getKolonAdi().equals(kolonSonucu.getKolonAdi())) {
                    kolonSonucu.setOrnekler(onizleme.getOrnekler() != null
                            ? onizleme.getOrnekler() : new ArrayList<>());
                    break;
                }
            }
        }
    }

    private int toplamGuncellenen(TemizlemeSonucu sonuc) {
        int toplam = 0;
        if (sonuc.getKolonSonuclari() != null) {
            for (KolonTemizlemeSonucu kolonSonucu : sonuc.getKolonSonuclari()) {
                toplam += kolonSonucu.getGuncellenenSatirSayisi();
            }
        }
        return toplam;
    }

    private List<KolonBilgisi> metinTipindeKolonlariFiltrele(List<KolonBilgisi> kolonlar) {
        List<KolonBilgisi> sonuc = new ArrayList<>();
        for (KolonBilgisi kolon : kolonlar) {
            String veriTipi = kolon.getVeriTipi() != null ? kolon.getVeriTipi().toLowerCase(java.util.Locale.ROOT) : "";
            if (veriTipi.contains("char") || veriTipi.contains("text")) {
                sonuc.add(kolon);
            }
        }
        return sonuc;
    }

    private List<String> onerilenKolonlariBul(AlanEslestirmesi alanEslestirmesi) {
        List<String> oneriler = new ArrayList<>();
        if (alanEslestirmesi == null) {
            return oneriler;
        }
        if (alanEslestirmesi.getFirmaAdiKolonu() != null) {
            oneriler.add(alanEslestirmesi.getFirmaAdiKolonu());
        }
        if (alanEslestirmesi.getePostaKolonu() != null) {
            oneriler.add(alanEslestirmesi.getePostaKolonu());
        }
        if (alanEslestirmesi.getTelefonKolonu() != null) {
            oneriler.add(alanEslestirmesi.getTelefonKolonu());
        }
        oneriler.addAll(alanEslestirmesi.getAdresKolonlari());
        if (alanEslestirmesi.getWebSitesiKolonu() != null) {
            oneriler.add(alanEslestirmesi.getWebSitesiKolonu());
        }
        return oneriler;
    }
}
