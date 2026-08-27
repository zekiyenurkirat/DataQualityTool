package com.verikalitesi.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Şablonların gerçekten derlenip derlenmediğini denetler.
 *
 * <p>Diğer testlerin hepsi saf birim testi; hiçbiri Spring bağlamını ayağa
 * kaldırmaz. Bu yüzden bir Thymeleaf ifadesindeki yazım hatası derleme
 * aşamasında değil, ancak sayfa tarayıcıda açıldığında ortaya çıkar.
 * Ekranların tamamı ortak parçalar (üst çubuk, adım serisi, alt bilgi)
 * üzerine kurulduğu için tek bir bozuk parça dokuz sayfayı birden düşürür.
 *
 * <p>{@code /baglan} veritabanı istemeyen tek ekran, dolayısıyla ortak
 * parça katmanını sınamanın en ucuz yolu. Sayfa 200 dönüyorsa
 * {@code parcalar/duzen} içindeki dört parça da çözülmüş, adım serisinin
 * parametreli ifadesi de çalışmış demektir.
 *
 * <p>Veri kaynağı otomatik yapılandırması kapatıldı: bu test için bir
 * veritabanına ihtiyaç yok, açık bırakılsaydı bağlam sürücü bulamadığı
 * için hiç ayağa kalkmazdı.
 */
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
@AutoConfigureMockMvc
class SablonRenderTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Bağlantı ekranı ortak parçalarla birlikte hatasız derleniyor")
    void baglantiEkraniDerleniyor() throws Exception {
        mockMvc.perform(get("/baglan"))
                .andExpect(status().isOk())
                // Üst çubuk parçası geldi mi
                .andExpect(content().string(org.hamcrest.Matchers.containsString("tc-marka")))
                // Adım serisi parçası geldi ve ilk adım aktif işaretlendi mi
                .andExpect(content().string(org.hamcrest.Matchers.containsString("tc-adim aktif")))
                // Tema stili sayfaya bağlandı mı
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/css/tema.css")))
                // Alt bilgi parçası geldi mi
                .andExpect(content().string(org.hamcrest.Matchers.containsString("tc-altbilgi")));
    }
}
