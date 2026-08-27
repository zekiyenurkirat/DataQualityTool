package com.verikalitesi.altinkayit;

/**
 * İki kayıttan hangisinin ana kayıt (golden record) olacağına dair karar.
 *
 * <p>Karar bir <strong>kural zinciri</strong> sonucunda veriliyor ve hangi
 * kuralın karar verdiği kararın kendisiyle birlikte taşınıyor. Bunun sebebi
 * şu: zincirin sonundaki kurallar öndekiler kadar güçlü değil. Doluluk gerçek
 * bir bilgi ölçüsüdür; karakter uzunluğu zayıf bir vekildir; kimlik sırası ise
 * bilgi taşımaz, yalnızca sonucun her çalıştırmada aynı olmasını sağlar.
 *
 * <p>Araç her durumda bir kazanan üretir (determinizm), ama zayıf bir kuralla
 * karar verdiğinde bunu <strong>saklamaz</strong> -- ekranda hangi kuralın
 * karar verdiği yazar ve kullanıcıya diğer kaydı seçme imkânı sunulur.
 */
public class AltinKayitKarari {

    /** Zincirdeki kurallar, uygulanma sırasıyla. */
    public enum Kural {

        /**
         * Daha çok <em>anlamlı</em> alanı dolu olan kazanır. Yer tutucular
         * ("-", "N/A") dolu sayılmaz. Zincirdeki tek güçlü kural budur.
         */
        DOLULUK("Doluluk", true),

        /**
         * Doluluk eşitse: anlamlı değerlerin toplam karakter uzunluğu büyük
         * olan kazanır. <strong>Zayıf bir ölçü</strong> -- uzun olan her zaman
         * daha bilgili değildir, dolgu ve tekrar da uzunluğu artırır. Yine de
         * "Alfa Ticaret Limited Şirketi" ile "Alfa Tic." arasında doğru tarafa
         * eğilim gösterir.
         */
        KARAKTER_UZUNLUGU("Karakter uzunluğu", false),

        /**
         * İkisi de eşitse: kimliği alfabetik olarak önce gelen kazanır.
         * <strong>Bu kural bilgi taşımaz.</strong> Tek işlevi, aynı veri
         * üzerinde aracın her çalıştırmada aynı kararı vermesini sağlamak.
         * Rastgele seçim yapılsaydı iki çalıştırma iki farklı ana kayıt
         * üretir ve karar tekrarlanamaz olurdu.
         */
        KIMLIK_SIRASI("Kimlik sırası", false);

        private final String etiket;
        private final boolean guclu;

        Kural(String etiket, boolean guclu) {
            this.etiket = etiket;
            this.guclu = guclu;
        }

        public String getEtiket() {
            return etiket;
        }

        /** Kararın veriye dayanıp dayanmadığı. Zayıf kurallarda kullanıcı onayı istenir. */
        public boolean isGuclu() {
            return guclu;
        }
    }

    private final String kazananKimlik;
    private final String kaybedenKimlik;
    private final Kural kural;
    private final int birinciDoluluk;
    private final int ikinciDoluluk;
    private final int toplamAlan;
    private final int birinciUzunluk;
    private final int ikinciUzunluk;

    public AltinKayitKarari(String kazananKimlik, String kaybedenKimlik, Kural kural,
                            int birinciDoluluk, int ikinciDoluluk, int toplamAlan,
                            int birinciUzunluk, int ikinciUzunluk) {
        this.kazananKimlik = kazananKimlik;
        this.kaybedenKimlik = kaybedenKimlik;
        this.kural = kural;
        this.birinciDoluluk = birinciDoluluk;
        this.ikinciDoluluk = ikinciDoluluk;
        this.toplamAlan = toplamAlan;
        this.birinciUzunluk = birinciUzunluk;
        this.ikinciUzunluk = ikinciUzunluk;
    }

    public String getKazananKimlik() {
        return kazananKimlik;
    }

    public String getKaybedenKimlik() {
        return kaybedenKimlik;
    }

    public Kural getKural() {
        return kural;
    }

    /** Kararı veren kural güçlü mü; şablon bunu kullanıp kullanıcı onayı isteyip istemeyeceğine karar veriyor. */
    public boolean isGuvenilirKarar() {
        return kural.isGuclu();
    }

    public int getBirinciDoluluk() {
        return birinciDoluluk;
    }

    public int getIkinciDoluluk() {
        return ikinciDoluluk;
    }

    public int getToplamAlan() {
        return toplamAlan;
    }

    public int getBirinciUzunluk() {
        return birinciUzunluk;
    }

    public int getIkinciUzunluk() {
        return ikinciUzunluk;
    }

    /**
     * Kararın insan tarafından okunabilir gerekçesi. Denetim izine de bu metin
     * yazılıyor: altı ay sonra "bu kayıt neden pasife çekilmiş" sorusunun
     * cevabı, hangi kuralın hangi sayılarla karar verdiğiyle birlikte kayıtta
     * durmalı.
     */
    public String getGerekce() {
        return switch (kural) {
            case DOLULUK -> "Doluluk kuralı (completeness): " + Math.max(birinciDoluluk, ikinciDoluluk)
                    + "/" + toplamAlan + " dolu alan, diğerinde "
                    + Math.min(birinciDoluluk, ikinciDoluluk) + "/" + toplamAlan + ".";
            case KARAKTER_UZUNLUGU -> "Doluluk eşitti (" + birinciDoluluk + "/" + toplamAlan
                    + "). Karakter uzunluğuna bakıldı: " + Math.max(birinciUzunluk, ikinciUzunluk)
                    + " karakter, diğerinde " + Math.min(birinciUzunluk, ikinciUzunluk) + ".";
            case KIMLIK_SIRASI -> "Doluluk ve karakter uzunluğu eşitti. Sonucun her çalıştırmada "
                    + "aynı olması için kimlik sırasına göre seçildi; bu karar veriye dayanmıyor.";
        };
    }
}
