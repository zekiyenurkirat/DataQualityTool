package com.verikalitesi.temizleme;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpostaOnariciTest {

    private final EpostaOnarici onarici = new EpostaOnarici();

    private void degisti(String ham, String beklenen) {
        OnarimCiktisi cikti = onarici.onar(ham);
        assertEquals(OnarimCiktisi.Durum.DEGISTI, cikti.getDurum(), "onarılmalıydı: " + ham);
        assertEquals(beklenen, cikti.getYeniDeger());
    }

    private void durum(String ham, OnarimCiktisi.Durum beklenen) {
        assertEquals(beklenen, onarici.onar(ham).getDurum(), ham);
    }

    // ---------- onarilabilenler ----------

    @Test
    void icBoslukSilinmeli() {
        degisti("te st@sirket.com", "test@sirket.com");
        degisti("ahmet@sirket .com", "ahmet@sirket.com");
    }

    @Test
    void tekrarlayanAtIsaretiTekeInmeli() {
        degisti("test@@sirket.com", "test@sirket.com");
        degisti("test@@@sirket.com", "test@sirket.com");
    }

    @Test
    void tekrarlayanNoktaTekeInmeli() {
        degisti("test@sirket..com", "test@sirket.com");
        degisti("test@sirket...com", "test@sirket.com");
    }

    @Test
    void bastakiVeSondakiNoktalamaSilinmeli() {
        degisti(".test@sirket.com", "test@sirket.com");
        degisti("test@sirket.com.", "test@sirket.com");
        degisti(",test@sirket.com;", "test@sirket.com");
    }

    @Test
    void birdenFazlaBozuklukAyniAndaOnarilmali() {
        degisti("  .te st@@sirket..com ,", "test@sirket.com");
    }

    @Test
    void uluslararasiAdresOnarilabilmeli() {
        // Turkce, Kiril ve Arapca alan adlari dogrulayici tarafindan kabul ediliyor;
        // onarim bunlari bozmadan calismali.
        degisti("ahmet@@şirket.com.tr", "ahmet@şirket.com.tr");
        degisti("ахмет@@фирма.рф", "ахмет@фирма.рф");
        degisti("محمد@@شركة.مصر", "محمد@شركة.مصر");
    }

    // ---------- dokunulmayanlar ----------

    @Test
    void gecerliAdreseDokunulmamali() {
        durum("ahmet@sirket.com", OnarimCiktisi.Durum.GEREK_YOK);
        durum("ahmet@şirket.com.tr", OnarimCiktisi.Durum.GEREK_YOK);
    }

    @Test
    void bosDegerSorunSayilmamali() {
        durum(null, OnarimCiktisi.Durum.GEREK_YOK);
        durum("", OnarimCiktisi.Durum.GEREK_YOK);
        durum("   ", OnarimCiktisi.Durum.GEREK_YOK);
    }

    @Test
    void onarimSonucuGecerliDegilseDegistirilmemeli() {
        // Alan adi eksik; bosluk silinse bile gecerli olmaz, uydurmuyoruz.
        durum("ahmet@", OnarimCiktisi.Durum.ONARILAMADI);
        durum("ahmet", OnarimCiktisi.Durum.ONARILAMADI);
        durum("@sirket.com", OnarimCiktisi.Durum.ONARILAMADI);
    }

    @Test
    void cokluAdresOnarilmamali() {
        // Hangisinin kalacagi is karari; arac kendi basina birini secemez.
        durum("a@x.com, b@y.com", OnarimCiktisi.Durum.ONARILAMADI);
        durum("a@x.com b@y.com", OnarimCiktisi.Durum.ONARILAMADI);
    }
}
