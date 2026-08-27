package com.verikalitesi.dto;

public class TemizlemeKurali {

    private String kolonAdi;

    /** Baştaki ve sondaki boşlukları siler. */
    private boolean bosluklariKirp;

    /**
     * Ortadaki ardışık boşlukları tek boşluğa indirir.
     *
     * <p>Kırpmadan ayrı bir seçenek: ikisi farklı sorunlardır. " Ahmet Ticaret " ile
     * "Ahmet  Ticaret" aynı şey değil ve kullanıcı yalnızca birini düzeltmek isteyebilir.
     */
    private boolean ardisikBosluklariDaralt;

    /** Silinecek karakterler; kullanıcı serbestçe yazar, örnek: {@code #@!|} */
    private String haricTutulacakKarakter;

    private boolean telefonFormatiUygulanacakMi;

    private String telefonHedefFormati;

    /**
     * Bozuk e-posta adreslerinin onarılması istenip istenmediği. Harf dönüşümünden
     * ayrı bir seçenek: biri biçimlendirme, diğeri içerik onarımı.
     */
    private boolean epostaOnarimiUygulanacakMi;

    /**
     * Web adreslerinin standart biçime getirilmesi istenip istenmediği. Harf dönüşümünden
     * ayrı olmak zorunda: genel dönüşüm değerin tamamını küçültür, oysa web adresinde
     * yalnızca alan adı küçültülebilir, yol küçültülemez.
     */
    private boolean websiteOnarimiUygulanacakMi;

    /** Metin standartlaştırma; varsayılan {@link HarfDonusumu#YOK}. */
    private HarfDonusumu harfDonusumu = HarfDonusumu.YOK;

    public String getKolonAdi() {
        return kolonAdi;
    }

    public void setKolonAdi(String kolonAdi) {
        this.kolonAdi = kolonAdi;
    }

    public boolean isBosluklariKirp() {
        return bosluklariKirp;
    }

    public void setBosluklariKirp(boolean bosluklariKirp) {
        this.bosluklariKirp = bosluklariKirp;
    }

    public boolean isArdisikBosluklariDaralt() {
        return ardisikBosluklariDaralt;
    }

    public void setArdisikBosluklariDaralt(boolean ardisikBosluklariDaralt) {
        this.ardisikBosluklariDaralt = ardisikBosluklariDaralt;
    }

    public String getHaricTutulacakKarakter() {
        return haricTutulacakKarakter;
    }

    public void setHaricTutulacakKarakter(String haricTutulacakKarakter) {
        this.haricTutulacakKarakter = haricTutulacakKarakter;
    }

    public boolean isTelefonFormatiUygulanacakMi() {
        return telefonFormatiUygulanacakMi;
    }

    public void setTelefonFormatiUygulanacakMi(boolean telefonFormatiUygulanacakMi) {
        this.telefonFormatiUygulanacakMi = telefonFormatiUygulanacakMi;
    }

    public String getTelefonHedefFormati() {
        return telefonHedefFormati;
    }

    public void setTelefonHedefFormati(String telefonHedefFormati) {
        this.telefonHedefFormati = telefonHedefFormati;
    }

    public boolean isEpostaOnarimiUygulanacakMi() {
        return epostaOnarimiUygulanacakMi;
    }

    public void setEpostaOnarimiUygulanacakMi(boolean epostaOnarimiUygulanacakMi) {
        this.epostaOnarimiUygulanacakMi = epostaOnarimiUygulanacakMi;
    }

    public boolean isWebsiteOnarimiUygulanacakMi() {
        return websiteOnarimiUygulanacakMi;
    }

    public void setWebsiteOnarimiUygulanacakMi(boolean websiteOnarimiUygulanacakMi) {
        this.websiteOnarimiUygulanacakMi = websiteOnarimiUygulanacakMi;
    }

    public HarfDonusumu getHarfDonusumu() {
        return harfDonusumu;
    }

    public void setHarfDonusumu(HarfDonusumu harfDonusumu) {
        this.harfDonusumu = harfDonusumu == null ? HarfDonusumu.YOK : harfDonusumu;
    }

    /** Bu kural hiçbir metin işlemi içermiyorsa SQL kurmaya gerek yoktur. */
    public boolean metinIslemiVarMi() {
        return bosluklariKirp || ardisikBosluklariDaralt
                || (haricTutulacakKarakter != null && !haricTutulacakKarakter.isBlank())
                || harfDonusumu.isUygulanacakMi();
    }
}
