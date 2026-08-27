package com.verikalitesi.altinkayit;

import com.verikalitesi.dto.AlanEslestirmesi;
import com.verikalitesi.rule.PlaceholderKurali;
import com.verikalitesi.temizleme.BoslukOnarici;
import com.verikalitesi.temizleme.DegerOnarici;
import com.verikalitesi.temizleme.EpostaOnarici;
import com.verikalitesi.temizleme.OnarimCiktisi;
import com.verikalitesi.temizleme.TelefonOnarici;
import com.verikalitesi.temizleme.WebSitesiOnarici;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bir kopya kümesinden ana kayıt (golden record) adayı üretir.
 *
 * <p>Üç iş yapıyor:
 *
 * <ol>
 *   <li><strong>Değerleri onarım motorundan geçiriyor.</strong> Kullanıcıya ham
 *       veri sunmak, temizlenebilir bir kirin ana kayda taşınması demektir.
 *       E-posta, telefon ve web adresi kolonları kendi onarıcılarından, diğer
 *       metin kolonları boşluk onarıcısından geçiyor.</li>
 *   <li><strong>Değişeni işaretliyor.</strong> Ham değer ile onarılmış değer
 *       farklıysa o seçenek "onarıldı" damgası alıyor ve ekranda ham hâli de
 *       gösteriliyor. Sessizce değişen veriye kimse güvenmez.</li>
 *   <li><strong>Her kolon için bir öneri seçiyor.</strong> Öneri, radyo düğmesi
 *       hazır işaretli gelsin diye; son karar kullanıcıda.</li>
 * </ol>
 *
 * <p><strong>Kolon başına seçim kuralı</strong> (kayıt seviyesindekiyle aynı
 * mantık, tek alana indirgenmiş hâli): anlamlı değer taşıyanlar öne geçer
 * (yer tutucu ve boş elenir), aralarında en uzun olan seçilir, o da eşitse
 * kimlik sırasına düşülür. Son kural bilgi taşımaz; yalnızca aynı kümenin
 * her açılışta aynı öneriyle gelmesini sağlar.
 */
public class AltinKayitSentezleyici {

    private final PlaceholderKurali placeholderKurali;
    private final AlanEslestirmesi alanEslestirmesi;
    private final DegerOnarici boslukOnarici = new BoslukOnarici();
    private final DegerOnarici epostaOnarici = new EpostaOnarici();
    private final DegerOnarici websiteOnarici = new WebSitesiOnarici();
    private final DegerOnarici telefonOnarici;

    /**
     * @param telefonHedefFormati libphonenumber format adı; ülke seçilmediyse
     *                            telefon onarıcısı hiç kurulmuyor ve o kolon da
     *                            boşluk onarıcısına düşüyor. Ülkesiz numara
     *                            ayrıştırılamadığı için onarım "yapamadığını
     *                            sessizce yanlış yapma" ilkesine takılırdı.
     */
    public AltinKayitSentezleyici(PlaceholderKurali placeholderKurali,
                                  AlanEslestirmesi alanEslestirmesi,
                                  String telefonHedefFormati) {
        this.placeholderKurali = placeholderKurali;
        this.alanEslestirmesi = alanEslestirmesi;
        String ulkeKodu = alanEslestirmesi == null ? null : alanEslestirmesi.getUlkeKodu();
        this.telefonOnarici = (ulkeKodu == null || ulkeKodu.isBlank())
                ? null
                : new TelefonOnarici(ulkeKodu, telefonHedefFormati);
    }

    /** Kümedeki her kolon için seçenekleri ve öneriyi üretir. */
    public List<AlanSecenegi> secenekleriUret(KopyaKumesi kume) {
        Set<String> kolonlar = new LinkedHashSet<>();
        for (Map<String, String> alanlar : kume.getKayitlar().values()) {
            kolonlar.addAll(alanlar.keySet());
        }

        List<AlanSecenegi> sonuc = new ArrayList<>();
        for (String kolon : kolonlar) {
            List<AlanSecenegi.Deger> degerler = new ArrayList<>();
            for (String kimlik : kume.getKimlikler()) {
                degerler.add(degerUret(kolon, kimlik, kume.getKayit(kimlik)));
            }
            sonuc.add(new AlanSecenegi(kolon, degerler, oneriSec(degerler, kume.getKimlikler())));
        }
        return sonuc;
    }

    /**
     * Kullanıcının seçtiği indekslerden ana kaydı kurar.
     *
     * @param secimler kolon adı -> seçilen seçeneğin sırası
     * @return kolon adı -> ana kayda yazılacak değer (sıra korunur)
     */
    public Map<String, String> altinKayitKur(List<AlanSecenegi> secenekler,
                                             Map<String, Integer> secimler) {
        Map<String, String> altinKayit = new LinkedHashMap<>();
        for (AlanSecenegi secenek : secenekler) {
            Integer indeks = secimler.get(secenek.getKolonAdi());
            // Formdan gelmeyen ya da geçersiz bir indekste aracın önerisine düşüyoruz;
            // eksik bir form alanı yüzünden kolon boş kalmamalı.
            int secilen = (indeks == null || indeks < 0 || indeks >= secenek.getDegerler().size())
                    ? secenek.getOnerilenIndeks()
                    : indeks;
            altinKayit.put(secenek.getKolonAdi(), secenek.getDegerler().get(secilen).getTemizDeger());
        }
        return altinKayit;
    }

    private AlanSecenegi.Deger degerUret(String kolon, String kimlik, Map<String, String> alanlar) {
        String ham = alanlar == null ? null : alanlar.get(kolon);
        DegerOnarici onarici = onariciSec(kolon);

        OnarimCiktisi ciktisi = onarici.onar(ham);
        if (ciktisi.getDurum() == OnarimCiktisi.Durum.DEGISTI) {
            return new AlanSecenegi.Deger(kimlik, ham, ciktisi.getYeniDeger(), true, onarici.adi());
        }
        // GEREK_YOK ve ONARILAMADI aynı sonuca çıkıyor: değer olduğu gibi kalıyor.
        // İkisini ayırmak kullanıcıya bir şey söylemiyor -- ekranda görünen şey
        // "değişmedi", sebebi değil.
        return new AlanSecenegi.Deger(kimlik, ham, ham, false, null);
    }

    private DegerOnarici onariciSec(String kolon) {
        if (alanEslestirmesi == null) {
            return boslukOnarici;
        }
        if (kolon.equals(alanEslestirmesi.getePostaKolonu())) {
            return epostaOnarici;
        }
        if (kolon.equals(alanEslestirmesi.getWebSitesiKolonu())) {
            return websiteOnarici;
        }
        if (kolon.equals(alanEslestirmesi.getTelefonKolonu()) && telefonOnarici != null) {
            return telefonOnarici;
        }
        return boslukOnarici;
    }

    /**
     * Kolon için önerilen seçeneğin sırası.
     *
     * <p>Anlamlı değer taşıyanlar önce; aralarında en uzun; eşitlikte kimlik
     * sırası. Hiçbiri anlamlı değilse ilk seçenek öneriliyor -- o durumda
     * hangisi seçilirse seçilsin kolon boş kalacak.
     */
    private int oneriSec(List<AlanSecenegi.Deger> degerler, List<String> kimlikler) {
        int enIyi = -1;
        int enIyiUzunluk = -1;
        for (int i = 0; i < degerler.size(); i++) {
            AlanSecenegi.Deger deger = degerler.get(i);
            if (!anlamliMi(deger.getTemizDeger())) {
                continue;
            }
            int uzunluk = deger.getTemizDeger().trim().length();
            if (uzunluk > enIyiUzunluk) {
                enIyi = i;
                enIyiUzunluk = uzunluk;
            }
            // Eşitlikte ilk gelen kalıyor; kimlikler zaten sıralı geldiği için
            // bu "kimlik sırasına göre seç" demek.
        }
        return enIyi >= 0 ? enIyi : 0;
    }

    private boolean anlamliMi(String deger) {
        if (deger == null || deger.isBlank()) {
            return false;
        }
        return placeholderKurali.kontrolEt("", "", deger).isEmpty();
    }
}
