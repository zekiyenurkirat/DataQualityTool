package com.verikalitesi.temizleme;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebSitesiOnariciTest {

    private final WebSitesiOnarici onarici = new WebSitesiOnarici();

    private void degisti(String ham, String beklenen) {
        OnarimCiktisi cikti = onarici.onar(ham);
        assertEquals(OnarimCiktisi.Durum.DEGISTI, cikti.getDurum(), "onarılmalıydı: " + ham);
        assertEquals(beklenen, cikti.getYeniDeger());
    }

    private void durum(String ham, OnarimCiktisi.Durum beklenen) {
        assertEquals(beklenen, onarici.onar(ham).getDurum(), ham);
    }

    // ---------- alan adi kucultme ----------

    @Test
    void alanAdiKucultulmeli() {
        // Gercek veriden ornekler
        degisti("WWW.MPICAOTING.COM", "www.mpicaoting.com");
        degisti("WWW.FISC.INDUSTRY.GOV.IQ", "www.fisc.industry.gov.iq");
        degisti("www.royababil.IQ", "www.royababil.iq");
    }

    @Test
    void semaDaKucultulmeli() {
        degisti("HTTP://ZNC.IQ", "http://znc.iq");
        degisti("HTTPS://X.COM", "https://x.com");
    }

    // ---------- EN KRITIK: yola dokunulmamali ----------

    @Test
    void yolAynenKorunmali() {
        // Alan adi buyuk/kucuk harf duyarsiz, YOL duyarli.
        // "x.com/MyPage" ile "x.com/mypage" FARKLI sayfalardir.
        degisti("EXAMPLE.COM/MyPage", "example.com/MyPage");
        degisti("HTTP://X.COM/Belge/Rapor.PDF", "http://x.com/Belge/Rapor.PDF");
    }

    @Test
    void yoluOlanAdresSadeceAlanAdiFarkliysaDegismeli() {
        // Alan adi zaten kucuk, yol buyuk harfli -> degisecek bir sey yok.
        durum("example.com/MyPage", OnarimCiktisi.Durum.GEREK_YOK);
    }

    // ---------- port ve egik cizgi ----------

    @Test
    void varsayilanPortSilinmeli() {
        degisti("http://x.com:80", "http://x.com");
        degisti("https://x.com:443", "https://x.com");
    }

    @Test
    void varsayilanOlmayanPortKorunmali() {
        // https icin 80 varsayilan degil; silmek adresi bozar.
        durum("https://x.com:80", OnarimCiktisi.Durum.GEREK_YOK);
        durum("http://x.com:8080", OnarimCiktisi.Durum.GEREK_YOK);
    }

    @Test
    void alanAdindanSonrakiTekEgikCizgiSilinmeli() {
        degisti("www.nuboogh.com/", "www.nuboogh.com");
        degisti("http://x.com/", "http://x.com");
    }

    // ---------- dokunulmayanlar ----------

    @Test
    void zatenStandartAdreseDokunulmamali() {
        durum("www.example.com", OnarimCiktisi.Durum.GEREK_YOK);
        durum("https://example.com/yol", OnarimCiktisi.Durum.GEREK_YOK);
    }

    @Test
    void eksikSemaTamamlanmamali() {
        // http mi https mi bilinemez; uydurmak yanlis veri uretmek olur.
        assertEquals("www.example.com", onarici.onar("WWW.EXAMPLE.COM").getYeniDeger());
    }

    @Test
    void epostaAdresiOnarilmamali() {
        // Web sitesi kolonunda 673 satirda e-posta var; bunlar adres degil.
        durum("info@alzawraaelectric.com", OnarimCiktisi.Durum.ONARILAMADI);
        durum("GHADEER_ALNAWRAS@YAHOO.COM", OnarimCiktisi.Durum.ONARILAMADI);
    }

    @Test
    void serbestMetinOnarilmamali() {
        durum("ALMANSAM", OnarimCiktisi.Durum.ONARILAMADI);      // nokta yok
        durum("Khalid ALWELI", OnarimCiktisi.Durum.ONARILAMADI); // bosluk var
        durum("/", OnarimCiktisi.Durum.ONARILAMADI);
        durum("//", OnarimCiktisi.Durum.ONARILAMADI);
    }

    @Test
    void bosDegerSorunSayilmamali() {
        durum(null, OnarimCiktisi.Durum.GEREK_YOK);
        durum("", OnarimCiktisi.Durum.GEREK_YOK);
        durum("   ", OnarimCiktisi.Durum.GEREK_YOK);
    }
}
