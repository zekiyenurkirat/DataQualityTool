package com.verikalitesi.web;

import com.verikalitesi.altinkayit.AltinKayitSentezleyici;
import com.verikalitesi.altinkayit.KopyaKumesi;
import com.verikalitesi.altinkayit.KumeBulucu;
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
 * Alan seviyesinde birleştirme ekranının derlendiğini denetler.
 *
 * <p>Bu ekranın sütun sayısı sabit değil -- kümede kaç kayıt varsa o kadar
 * sütun üretiliyor. Üç kayıtlık bir küme kuruluyor ve şu üç şey aranıyor:
 * üç sütunun da çıkması, onarılan değerin rozetle ve ham hâliyle görünmesi,
 * radyo düğmelerinin kolon başına aynı ada sahip olması (yoksa aynı satırda
 * birden çok seçim yapılabilirdi).
 */
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
@AutoConfigureMockMvc
class GelismisBirlestirmeRenderTest {

    @Autowired
    private MockMvc mockMvc;

    @Controller
    static class SahteDenetleyici {
        @GetMapping("/test-gelismis")
        String ekran(Model model) {
            KopyaKumesi kume = ucKayitlikKume();
            AltinKayitSentezleyici sentezleyici = new AltinKayitSentezleyici(
                    new PlaceholderKurali(List.of("n/a")), null, "E164");
            model.addAttribute("kume", kume);
            model.addAttribute("secenekler", sentezleyici.secenekleriUret(kume));
            return "gelismisBirlestirme";
        }
    }

    @TestConfiguration
    static class Ayar {
        @Bean
        SahteDenetleyici sahteDenetleyici() {
            return new SahteDenetleyici();
        }
    }

    private static Map<String, String> alanlar(String... anahtarDeger) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < anahtarDeger.length; i += 2) {
            map.put(anahtarDeger[i], anahtarDeger[i + 1]);
        }
        return map;
    }

    /** A~B ve B~C zinciri: tek küme, üç kayıt. */
    private static KopyaKumesi ucKayitlikKume() {
        List<Map<String, String>> kayitlar = List.of(
                alanlar("ad", "  Alfa   Trade  LLC ", "telefon", "07701234567", "eposta", null),
                alanlar("ad", "Alfa Trade", "telefon", null, "eposta", "info@alfa.iq"),
                alanlar("ad", "Alfa Treyd OOO", "telefon", null, "eposta", null));

        List<BenzerFirmaCifti> ciftler = new ArrayList<>();
        for (int i = 1; i < kayitlar.size(); i++) {
            BenzerFirmaCifti cift = new BenzerFirmaCifti();
            cift.setId_1("KAYIT-" + (i - 1));
            cift.setId_2("KAYIT-" + i);
            cift.setAlanlar1(kayitlar.get(i - 1));
            cift.setAlanlar2(kayitlar.get(i));
            cift.setFirma1("Alfa Trade LLC");
            cift.setFirma2("Alfa Trade");
            ciftler.add(cift);
        }
        return new KumeBulucu().kumeleriBul(ciftler).get(0);
    }

    @Test
    @DisplayName("Gelişmiş birleştirme ekranı N sütunla hatasız derleniyor")
    void ekranDerleniyor() throws Exception {
        mockMvc.perform(get("/test-gelismis"))
                .andExpect(status().isOk())
                // Kümedeki üç kaydın da sütunu var
                .andExpect(content().string(Matchers.containsString("KAYIT-0")))
                .andExpect(content().string(Matchers.containsString("KAYIT-1")))
                .andExpect(content().string(Matchers.containsString("KAYIT-2")))
                // Veri şeffaflığı: onarılan değer rozetli ve ham hâli görünüyor
                .andExpect(content().string(Matchers.containsString("✨ Onarıldı")))
                .andExpect(content().string(Matchers.containsString("Alfa   Trade  LLC")))
                // Radyo düğmeleri kolon başına tek grup
                .andExpect(content().string(Matchers.containsString("name=\"secim_ad\"")))
                .andExpect(content().string(Matchers.containsString("name=\"secim_telefon\"")))
                .andExpect(content().string(Matchers.containsString("name=\"secim_eposta\"")))
                // Sentezlenen ana kaydın kimliği ekranda
                .andExpect(content().string(Matchers.containsString("AK-")))
                // Form doğru uca gidiyor
                .andExpect(content().string(Matchers.containsString("action=\"/birlestir/gelismis\"")));
    }
}
