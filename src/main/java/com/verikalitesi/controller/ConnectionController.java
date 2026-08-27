package com.verikalitesi.controller;



import com.verikalitesi.core.VeritabaniBaglantiBilgisi;
import com.verikalitesi.core.VeritabaniBaglantiHatasi;
import com.verikalitesi.core.VeritabaniBaglantiServisi;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.sql.Connection;
import java.sql.SQLException;

@Controller
public class ConnectionController {

    private final VeritabaniBaglantiServisi baglantiServisi;

    @Value("${dev.sunucu:}")
    private String devSunucu;

    @Value("${dev.port:0}")
    private int devPort;

    @Value("${dev.veriTabani:}")
    private String devVeriTabani;

    @Value("${dev.kullaniciAdi:}")
    private String devKullaniciAdi;

    @Value("${dev.sifre:}")
    private String devSifre;

    public ConnectionController(VeritabaniBaglantiServisi baglantiServisi) {
        this.baglantiServisi = baglantiServisi;


    }
    @GetMapping("/baglan")
    public String baglantiFormunuGoster(Model model) {
        VeritabaniBaglantiBilgisi varsayilan = new VeritabaniBaglantiBilgisi();
        varsayilan.setSunucu(devSunucu);
        varsayilan.setPort(devPort);
        varsayilan.setVeriTabani(devVeriTabani);
        varsayilan.setKullaniciAdi(devKullaniciAdi);
        varsayilan.setSifre(devSifre);

        model.addAttribute("bilgi", varsayilan);
        return "connect";
    }

    @PostMapping("/baglan")
    public String baglantiyiTestEt(VeritabaniBaglantiBilgisi bilgi, HttpSession session, Model model) {
        try {
            Connection baglanti = baglantiServisi.baglan(bilgi);
            baglanti.close();
            session.setAttribute("baglantiBilgisi", bilgi);
            return "redirect:/tablolar";
        } catch (VeritabaniBaglantiHatasi hata) {
            model.addAttribute("hata", hata.getMessage());
            model.addAttribute("bilgi", bilgi);
            return "connect";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
