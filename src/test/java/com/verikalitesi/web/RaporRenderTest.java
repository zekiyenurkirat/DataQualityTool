package com.verikalitesi.web;

import com.verikalitesi.altinkayit.AltinKayitSecici;
import com.verikalitesi.dto.AnalizSonucu;
import com.verikalitesi.dto.BenzerFirmaCifti;
import com.verikalitesi.rule.PlaceholderKurali;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Rapor ekranının gerçekten derlendiğini denetler.
 *
 * <p>Rapor, uygulamanın en karmaşık şablonu. İçindeki bir Thymeleaf yazım
 * hatası derleme aşamasında görünmez; sayfa yalnızca tarayıcıda açıldığında
 * hata verir. Bu test şablonu temsilî bir sonuç nesnesiyle gerçek şablon
 * motorundan geçiriyor.
 *
 * <p>Özellikle <strong>ana kayıt (golden record) sütunu</strong> denetleniyor:
 * kural zincirinin üç dalı da -- doluluk, karakter uzunluğu ve kimlik sırası --
 * ayrı ayrı üretiliyor ve ekranda beklenen karşılığı aranıyor. Bu dallar ancak
 * listede uygun bir çift varken çalıştığı için temsilî veri gerekiyor.
 */
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
@AutoConfigureMockMvc
class RaporRenderTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Yalnızca bu testte var olan bir denetleyici. Rapor şablonunu gerçek
     * denetleyici zincirini (ve veritabanını) çalıştırmadan render edebilmek
     * için gerekiyor.
     */
    @Controller
    static class SahteRaporDenetleyici {

        @GetMapping("/test-rapor")
        String rapor(Model model) {
            model.addAttribute("sonuc", ornekSonuc());
            model.addAttribute("birlestirmeMesaji", "TEST-BIRLESTIRME-MESAJI");
            return "rapor";
        }
    }

    @TestConfiguration
    static class Ayar {
        @Bean
        SahteRaporDenetleyici sahteRaporDenetleyici() {
            return new SahteRaporDenetleyici();
        }
    }

    private static Map<String, String> alanlar(String... anahtarDeger) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < anahtarDeger.length; i += 2) {
            map.put(anahtarDeger[i], anahtarDeger[i + 1]);
        }
        return map;
    }

    private static AnalizSonucu ornekSonuc() {
        AltinKayitSecici secici = new AltinKayitSecici(new PlaceholderKurali(List.of("n/a")));

        // 1) Doluluk kuralı net bir kazanan buluyor
        BenzerFirmaCifti kazananVar = new BenzerFirmaCifti();
        kazananVar.setId_1("KIMLIK-A");
        kazananVar.setId_2("KIMLIK-B");
        kazananVar.setFirma1("Alfa Trade LLC");
        kazananVar.setFirma2("Alfa Treyd OOO");
        kazananVar.setBenzerlikOrani(0.912);
        kazananVar.setAlanlar1(alanlar("ad", "Alfa Trade LLC", "telefon", "07701234567"));
        kazananVar.setAlanlar2(alanlar("ad", "Alfa Treyd OOO", "telefon", null));
        kazananVar.setAltinKayitKarari(secici.sec("KIMLIK-A", kazananVar.getAlanlar1(),
                "KIMLIK-B", kazananVar.getAlanlar2()));

        // 2) Doluluk esit -- karari karakter uzunlugu veriyor (zayif kural)
        BenzerFirmaCifti berabere = new BenzerFirmaCifti();
        berabere.setId_1("KIMLIK-C");
        berabere.setId_2("KIMLIK-D");
        berabere.setFirma1("Beta Logistics");
        berabere.setFirma2("Beta Logistic");
        berabere.setBenzerlikOrani(0.845);
        berabere.setAlanlar1(alanlar("ad", "Beta Logistics Company"));
        berabere.setAlanlar2(alanlar("ad", "Beta Log."));
        berabere.setAltinKayitKarari(secici.sec("KIMLIK-C", berabere.getAlanlar1(),
                "KIMLIK-D", berabere.getAlanlar2()));

        // 3) Doluluk ve uzunluk esit -- karari kimlik sirasi veriyor (en zayif kural)
        BenzerFirmaCifti kimlikSirasi = new BenzerFirmaCifti();
        kimlikSirasi.setId_1("KIMLIK-E");
        kimlikSirasi.setId_2("KIMLIK-F");
        kimlikSirasi.setFirma1("Gamma Import");
        kimlikSirasi.setFirma2("Gamma Export");
        kimlikSirasi.setBenzerlikOrani(0.798);
        kimlikSirasi.setAlanlar1(alanlar("ad", "Gamma Import"));
        kimlikSirasi.setAlanlar2(alanlar("ad", "Gamma Export"));
        kimlikSirasi.setAltinKayitKarari(secici.sec("KIMLIK-E", kimlikSirasi.getAlanlar1(),
                "KIMLIK-F", kimlikSirasi.getAlanlar2()));

        // Uretimdeki gibi DOLU bir sonuc: skor karti, ozet, kopya gruplari ve
        // haric tutulan kelimeler bos birakilirsa sablonun o bloklari hic
        // calismaz ve oradaki bir hata testten kacar.
        com.verikalitesi.dto.KaliteSkorKarti skorKarti = new com.verikalitesi.dto.KaliteSkorKarti();
        skorKarti.topluEkle(com.verikalitesi.dto.KaliteBoyutuTuru.TAMLIK, 30000, 96);
        skorKarti.topluEkle(com.verikalitesi.dto.KaliteBoyutuTuru.GECERLILIK, 5972, 1540);
        skorKarti.topluEkle(com.verikalitesi.dto.KaliteBoyutuTuru.TEKLIK, 6039, 3987);
        skorKarti.topluEkle(com.verikalitesi.dto.KaliteBoyutuTuru.TUTARLILIK, 12000, 234);

        com.verikalitesi.dto.MesajOzeti ozet1 =
                new com.verikalitesi.dto.MesajOzeti("Web Sitesi: ", "Protokol eksik");
        ozet1.arttir();
        ozet1.setEylem(com.verikalitesi.dto.DuzeltmeEylemi.OTOMATIK);
        ozet1.setEylemNotu("Sema ve alan adi kucultulur.");

        com.verikalitesi.dto.KopyaGrubu grup = new com.verikalitesi.dto.KopyaGrubu();
        grup.setKimlik("GRUP-1");
        grup.setOrnekDeger("Alfa Trade LLC");
        grup.arttir();
        grup.arttir();

        com.verikalitesi.dto.KelimeFrekansi kelime = new com.verikalitesi.dto.KelimeFrekansi();
        kelime.setKelime("ltd");
        kelime.setTekrarSayisi(4937);
        kelime.setYuzde(98.7);
        kelime.setOtomatikSecildi(true);

        AnalizSonucu sonuc = new AnalizSonucu();
        sonuc.setSkorKarti(skorKarti);
        sonuc.setOzet(List.of(ozet1));
        sonuc.setKopyaGruplari(List.of(grup));
        sonuc.setToplamFazlalik(3987);
        sonuc.setHaricTutulanKelimeler(List.of(kelime));
        sonuc.setDogrulamaSonuclari(new ArrayList<>());
        sonuc.setBenzerFirmaCiftleri(List.of(kazananVar, berabere, kimlikSirasi));
        sonuc.setHamBenzerCiftSayisi(3);
        return sonuc;
    }

    @Test
    @DisplayName("Rapor ekranı ana kayıt sütunuyla birlikte hatasız derleniyor")
    void raporDerleniyor() throws Exception {
        mockMvc.perform(get("/test-rapor"))
                .andExpect(status().isOk())
                // Yeşil birleştirme bildirimi
                .andExpect(content().string(Matchers.containsString("TEST-BIRLESTIRME-MESAJI")))
                // Kural bir kazanan bulduğunda tek "Birleştir" butonu
                .andExpect(content().string(Matchers.containsString("Birleştir<")))
                .andExpect(content().string(Matchers.containsString("KIMLIK-A")))
                // Zayif kuralla karar verildiginde kural adi ekranda yaziyor
                .andExpect(content().string(Matchers.containsString("Karakter uzunluğu")))
                .andExpect(content().string(Matchers.containsString("Kimlik sırası")))
                // ve kullaniciya iki secenek de sunuluyor
                .andExpect(content().string(Matchers.containsString("1. kalsın")))
                .andExpect(content().string(Matchers.containsString("2. kalsın")))
                // Formlar doğru uca gidiyor
                .andExpect(content().string(Matchers.containsString("action=\"/birlestir\"")));
    }
}
