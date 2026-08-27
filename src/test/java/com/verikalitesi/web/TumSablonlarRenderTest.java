package com.verikalitesi.web;

import com.verikalitesi.core.UlkeKoduCozumleyici;
import com.verikalitesi.dto.HarfDonusumu;
import com.verikalitesi.dto.KalipTespiti;
import com.verikalitesi.dto.KelimeFrekansi;
import com.verikalitesi.dto.KolonBilgisi;
import com.verikalitesi.dto.KolonTemizlemeSonucu;
import com.verikalitesi.dto.TemizlemeKurali;
import com.verikalitesi.dto.TemizlemeOnizlemeSonucu;
import com.verikalitesi.dto.TemizlemeOrnek;
import com.verikalitesi.dto.TemizlemePlani;
import com.verikalitesi.dto.TemizlemeSonucu;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Kalan bütün şablonların derlendiğini denetler.
 *
 * <p>Bir Thymeleaf ifadesindeki yazım hatası derleme aşamasında görünmez;
 * sayfa ancak tarayıcıda açıldığında hata verir. Bu yüzden her şablon burada
 * bir kez, koşullu blokları da çalışacak şekilde üretiliyor.
 *
 * <p>Şablonlar gerçek denetleyici zinciri yerine yalnızca bu testte tanımlı
 * denetleyicilerle çağrılıyor; böylece veritabanı gerekmiyor.
 */
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
@AutoConfigureMockMvc
class TumSablonlarRenderTest {

    @Autowired
    private MockMvc mockMvc;

    @Controller
    static class SahteDenetleyici {

        @GetMapping("/t/tables")
        String tablolar(Model model) {
            model.addAttribute("tablolar", List.of("tablo_irak", "tablo_kirgizistan"));
            return "tables";
        }

        @GetMapping("/t/mapping")
        String eslestirme(Model model) {
            model.addAttribute("kolonlar", List.of(kolon("company_name", "text"),
                    kolon("house_number", "bigint")));
            model.addAttribute("tabloAdi", "tablo_irak");
            model.addAttribute("ulkeler", UlkeKoduCozumleyici.tumUlkeler());
            model.addAttribute("tahminEdilenUlkeKodu", "IQ");
            // Hata kutusu da denetleniyor: firma adi tip dogrulamasi bu yoldan doner.
            model.addAttribute("hata", "TEST-ESLESTIRME-HATASI");
            return "mapping";
        }

        @GetMapping("/t/kelime")
        String kelimeSecimi(Model model) {
            KelimeFrekansi kelime = new KelimeFrekansi();
            kelime.setKelime("ltd");
            kelime.setTekrarSayisi(4937);
            kelime.setYuzde(98.7);

            model.addAttribute("kelimeler", List.of(kelime));
            model.addAttribute("toplam", 5000);
            model.addAttribute("kalipKelimeler", List.of("ltd"));
            model.addAttribute("tespit", new KalipTespiti(List.of(kelime), 5.13, 2.0));
            return "kelimeSecimi";
        }

        /** Kirilma bulunamadigi dal ayri sablon blogu; o da uretilmeli. */
        @GetMapping("/t/kelime-kirilmasiz")
        String kelimeSecimiKirilmasiz(Model model) {
            model.addAttribute("kelimeler", List.of());
            model.addAttribute("toplam", 5000);
            model.addAttribute("kalipKelimeler", List.of());
            model.addAttribute("tespit", KalipTespiti.bos(2.0));
            return "kelimeSecimi";
        }

        @GetMapping("/t/temizleme-kolon")
        String temizlemeKolon(Model model) {
            model.addAttribute("kolonlar", List.of(kolon("company_name", "text")));
            model.addAttribute("onerilenKolonlar", List.of("company_name"));
            model.addAttribute("hata", "TEST-KOLON-HATASI");
            return "temizlemeKolonSecimi";
        }

        @GetMapping("/t/temizleme-kural")
        String temizlemeKural(Model model) {
            TemizlemeKurali kural = new TemizlemeKurali();
            kural.setKolonAdi("email");
            TemizlemePlani plan = new TemizlemePlani();
            plan.setTemizlemeKurallari(List.of(kural));

            model.addAttribute("plan", plan);
            // Ucu de ayni kolona esitleniyor ki e-posta, web ve telefon bloklarinin
            // ucu birden ayni sayfada uretilsin.
            model.addAttribute("epostaKolonu", "email");
            model.addAttribute("webSitesiKolonu", "email");
            model.addAttribute("telefonKolonu", "email");
            model.addAttribute("harfDonusumleri", HarfDonusumu.values());
            model.addAttribute("kimlikKolonuVar", false);
            model.addAttribute("ulkeSecildi", false);
            return "temizlemeKurallari";
        }

        @GetMapping("/t/temizleme-onizleme")
        String temizlemeOnizleme(Model model) {
            TemizlemeOnizlemeSonucu onizleme = new TemizlemeOnizlemeSonucu();
            onizleme.setKolonAdi("company_name");
            onizleme.setEtkilenecekSatirSayisi(234);
            onizleme.setOrnekler(List.of(ornek("104882", "  Alfa  ", "Alfa"),
                    ornek(null, "Beta  Ltd", "Beta Ltd")));

            model.addAttribute("onizlemeler", List.of(onizleme));
            return "temizlemeOnizleme";
        }

        @GetMapping("/t/temizleme-sonuc")
        String temizlemeSonuc(Model model) {
            KolonTemizlemeSonucu kolonSonucu = new KolonTemizlemeSonucu();
            kolonSonucu.setKolonAdi("company_name");
            kolonSonucu.setGuncellenenSatirSayisi(230);
            kolonSonucu.setAtlananSatirSayisi(3);
            kolonSonucu.setEslesmeyenSatirSayisi(1);
            kolonSonucu.setOrnekler(List.of(ornek("104882", "  Alfa  ", "Alfa")));

            KolonTemizlemeSonucu orneksiz = new KolonTemizlemeSonucu();
            orneksiz.setKolonAdi("website");
            orneksiz.setOrnekler(List.of());

            TemizlemeSonucu sonuc = new TemizlemeSonucu();
            sonuc.setKolonSonuclari(List.of(kolonSonucu, orneksiz));

            model.addAttribute("sonuc", sonuc);
            model.addAttribute("toplamGuncellenen", 230);
            return "temizlemeSonucu";
        }

        @GetMapping("/t/hata")
        String hata(Model model) {
            model.addAttribute("hataTipi", "PSQLException");
            model.addAttribute("hataMesaji", "TEST-HATA-MESAJI");
            return "hata";
        }

        private static KolonBilgisi kolon(String ad, String tip) {
            KolonBilgisi kolon = new KolonBilgisi();
            kolon.setKolonAdi(ad);
            kolon.setVeriTipi(tip);
            return kolon;
        }

        private static TemizlemeOrnek ornek(String id, String eski, String yeni) {
            TemizlemeOrnek ornek = new TemizlemeOrnek();
            ornek.setId(id);
            ornek.setEskiDeger(eski);
            ornek.setYeniDeger(yeni);
            return ornek;
        }
    }

    @TestConfiguration
    static class Ayar {
        @Bean
        SahteDenetleyici sahteDenetleyici() {
            return new SahteDenetleyici();
        }
    }

    @Test
    @DisplayName("Tablo seçimi ekranı derleniyor")
    void tablolar() throws Exception {
        mockMvc.perform(get("/t/tables")).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("tablo_kirgizistan")));
    }

    @Test
    @DisplayName("Eşleştirme ekranı hata kutusuyla birlikte derleniyor")
    void eslestirme() throws Exception {
        mockMvc.perform(get("/t/mapping")).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("TEST-ESLESTIRME-HATASI")))
                .andExpect(content().string(Matchers.containsString("adresKolonlari")));
    }

    @Test
    @DisplayName("Kelime seçimi ekranı, kırılma bulunduğunda derleniyor")
    void kelimeSecimi() throws Exception {
        mockMvc.perform(get("/t/kelime")).andExpect(status().isOk())
                // Ondalik ayraci JVM diline gore degisiyor (5,13 / 5.13); sayinin
                // kendisi yerine bloğun uretildigini denetliyoruz.
                .andExpect(content().string(Matchers.containsString("Frekans listesinde")))
                .andExpect(content().string(Matchers.containsString("kalıp")));
    }

    @Test
    @DisplayName("Kelime seçimi ekranı, kırılma bulunamadığında da derleniyor")
    void kelimeSecimiKirilmasiz() throws Exception {
        mockMvc.perform(get("/t/kelime-kirilmasiz")).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("otomatik seçilmedi")));
    }

    @Test
    @DisplayName("Temizlenecek kolon ekranı derleniyor")
    void temizlemeKolonSecimi() throws Exception {
        mockMvc.perform(get("/t/temizleme-kolon")).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("TEST-KOLON-HATASI")))
                .andExpect(content().string(Matchers.containsString("secilenKolonlar")));
    }

    @Test
    @DisplayName("Temizleme kuralları ekranı üç özel blokla birlikte derleniyor")
    void temizlemeKurallari() throws Exception {
        mockMvc.perform(get("/t/temizleme-kural")).andExpect(status().isOk())
                // Uyari kutulari
                .andExpect(content().string(Matchers.containsString("Telefon ve e-posta onarımı")))
                .andExpect(content().string(Matchers.containsString("ülke seçilmeden")))
                // Form alanlarinin adlari bozulmamis olmali
                .andExpect(content().string(Matchers.containsString("temizlemeKurallari[0].bosluklariKirp")))
                .andExpect(content().string(Matchers.containsString("temizlemeKurallari[0].epostaOnarimiUygulanacakMi")))
                .andExpect(content().string(Matchers.containsString("temizlemeKurallari[0].websiteOnarimiUygulanacakMi")))
                .andExpect(content().string(Matchers.containsString("temizlemeKurallari[0].telefonHedefFormati")))
                // Turkce buyuk harf uyarisi
                .andExpect(content().string(Matchers.containsString("ıŞıK")));
    }

    @Test
    @DisplayName("Temizleme önizleme ekranı, kimliksiz satırla birlikte derleniyor")
    void temizlemeOnizleme() throws Exception {
        mockMvc.perform(get("/t/temizleme-onizleme")).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("234")))
                // Kimligi olmayan satir "—" ile gosteriliyor
                .andExpect(content().string(Matchers.containsString("—")));
    }

    @Test
    @DisplayName("Temizleme sonuç ekranı, örneksiz kolonla birlikte derleniyor")
    void temizlemeSonucu() throws Exception {
        mockMvc.perform(get("/t/temizleme-sonuc")).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("geri alınamaz")))
                .andExpect(content().string(Matchers.containsString("Bu kolon için örnek kaydedilmedi")));
    }

    /**
     * Hata ekranının kendisi de bir şablon. Patlarsa güvenlik ağı çalışmaz ve
     * kullanıcı yine beyaz sayfayla karşılaşır -- bu yüzden o da denetleniyor.
     */
    @Test
    @DisplayName("Hata ekranı derleniyor")
    void hataEkrani() throws Exception {
        mockMvc.perform(get("/t/hata")).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("TEST-HATA-MESAJI")))
                .andExpect(content().string(Matchers.containsString("Yeniden bağlan")));
    }
}
