package com.verikalitesi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Denetleyicilerde yakalanmayan her hatayı düzgün bir ekrana çevirir.
 *
 * <p>Bunu eklemeden önce beklenmeyen bir hata Spring'in varsayılan
 * "Whitelabel Error Page" ekranına düşüyordu: beyaz zemin, İngilizce metin,
 * hiçbir yönlendirme yok. Kullanıcı ne olduğunu anlamıyor, nereye döneceğini
 * de bilmiyordu.
 *
 * <p><strong>Hata gizlenmiyor, bastırılmıyor.</strong> Tam yığın konsola
 * yazılmaya devam ediyor; ekranda yalnızca kısa bir özet ve geri dönüş
 * bağlantıları gösteriliyor. Amaç hatayı saklamak değil, kullanıcıyı çıkmaz
 * bir ekranda bırakmamak.
 *
 * <p><strong>Sınır:</strong> şablon <em>render edilirken</em> oluşan hatalar
 * buraya düşmez, çünkü o noktada yanıt tarayıcıya akmaya başlamış oluyor.
 * Şablonların derlendiğini ayrı render testleri denetliyor.
 */
@ControllerAdvice
public class HataYakalayici {

    private static final Logger gunluk = LoggerFactory.getLogger(HataYakalayici.class);

    @ExceptionHandler(Exception.class)
    public String beklenmeyenHata(Exception hata, Model model) {
        gunluk.error("Beklenmeyen hata", hata);

        model.addAttribute("hataTipi", hata.getClass().getSimpleName());
        model.addAttribute("hataMesaji", kokNedenMesaji(hata));
        return "hata";
    }

    /**
     * Kullanıcıya gösterilecek mesaj.
     *
     * <p>Sarmalayıcı istisnaların mesajı ("ConnectionCallback; ...") işe
     * yaramıyor; asıl bilgi zincirin en dibinde. Kök nedene inip onun mesajını
     * gösteriyoruz -- veritabanı hatalarında bu doğrudan Postgres'in söylediği
     * cümle oluyor ve gerçekten açıklayıcı.
     */
    private String kokNedenMesaji(Throwable hata) {
        Throwable kok = hata;
        while (kok.getCause() != null && kok.getCause() != kok) {
            kok = kok.getCause();
        }
        String mesaj = kok.getMessage();
        if (mesaj == null || mesaj.isBlank()) {
            return kok.getClass().getSimpleName();
        }
        // Çok uzun mesajlar ekranı dağıtıyor; tamamı zaten konsolda.
        return mesaj.length() > 400 ? mesaj.substring(0, 400) + "…" : mesaj;
    }
}
