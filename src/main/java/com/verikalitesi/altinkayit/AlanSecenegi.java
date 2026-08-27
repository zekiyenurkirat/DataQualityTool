package com.verikalitesi.altinkayit;

import java.util.Collections;
import java.util.List;

/**
 * Bir kolon için kümedeki kayıtlardan gelen seçeneklerin tamamı.
 *
 * <p>Alan seviyesinde birleştirmenin (field-level survivorship) temel birimi:
 * ana kayıt, girdilerden biri değil, her kolon için ayrı seçilmiş değerlerden
 * <strong>sentezlenen</strong> yeni bir kayıttır. Bir kayıtta telefon, ötekinde
 * e-posta varsa ana kayıtta ikisi de bulunur -- kayıt seviyesinde birleştirmede
 * bunlardan biri kaybolurdu.
 */
public class AlanSecenegi {

    /**
     * Tek bir kaydın bu kolondaki değeri, ham ve onarılmış hâliyle.
     *
     * <p>{@code onarildi} bayrağı veri şeffaflığı (data provenance) için:
     * kullanıcı sessizce değişmiş bir değere güvenmez. Ekranda onarılan
     * değerin yanında rozet çıkıyor ve ham hâli de gösteriliyor.
     */
    public static class Deger {

        private final String kaynakKimlik;
        private final String hamDeger;
        private final String temizDeger;
        private final boolean onarildi;
        private final String onariciAdi;

        public Deger(String kaynakKimlik, String hamDeger, String temizDeger,
                     boolean onarildi, String onariciAdi) {
            this.kaynakKimlik = kaynakKimlik;
            this.hamDeger = hamDeger;
            this.temizDeger = temizDeger;
            this.onarildi = onarildi;
            this.onariciAdi = onariciAdi;
        }

        public String getKaynakKimlik() {
            return kaynakKimlik;
        }

        public String getHamDeger() {
            return hamDeger;
        }

        /** Ana kayda yazılacak değer. Onarım yapılmadıysa ham değerin aynısı. */
        public String getTemizDeger() {
            return temizDeger;
        }

        public boolean isOnarildi() {
            return onarildi;
        }

        /** Değeri hangi onarıcının değiştirdiği; rozetin ipucu metninde gösteriliyor. */
        public String getOnariciAdi() {
            return onariciAdi;
        }

        public boolean isBosMu() {
            return temizDeger == null || temizDeger.isBlank();
        }
    }

    private final String kolonAdi;
    private final List<Deger> degerler;
    private final int onerilenIndeks;

    public AlanSecenegi(String kolonAdi, List<Deger> degerler, int onerilenIndeks) {
        this.kolonAdi = kolonAdi;
        this.degerler = Collections.unmodifiableList(degerler);
        this.onerilenIndeks = onerilenIndeks;
    }

    public String getKolonAdi() {
        return kolonAdi;
    }

    public List<Deger> getDegerler() {
        return degerler;
    }

    /** Aracın önerdiği seçeneğin sırası; radyo düğmesi bununla işaretli geliyor. */
    public int getOnerilenIndeks() {
        return onerilenIndeks;
    }

    /** Bu kolonda kayıtlar arasında gerçekten fark var mı; yoksa seçim yapmaya gerek yok. */
    public boolean isFarkVarMi() {
        if (degerler.size() < 2) {
            return false;
        }
        String ilk = degerler.get(0).getTemizDeger();
        for (Deger deger : degerler) {
            String simdi = deger.getTemizDeger();
            if (ilk == null ? simdi != null : !ilk.equals(simdi)) {
                return true;
            }
        }
        return false;
    }

    /** Kolonda en az bir değer onarıldıysa ekranda başlığa da rozet konuyor. */
    public boolean isOnarimVarMi() {
        return degerler.stream().anyMatch(Deger::isOnarildi);
    }
}
