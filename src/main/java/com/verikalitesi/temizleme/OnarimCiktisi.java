package com.verikalitesi.temizleme;

/**
 * Bir onarım denemesinin sonucu.
 *
 * <p>Üç durumu ayırmak şart: "zaten doğruydu" ile "onaramadım" aynı şey değildir. İkisini
 * tek bir boş sonuçta birleştirirsek, hâlihazırda düzgün olan binlerce satır raporda
 * "atlandı" diye görünür ve kullanıcı olmayan bir sorun sanır.
 */
public final class OnarimCiktisi {

    public enum Durum {
        /** Değer onarıldı; {@link #getYeniDeger()} dolu. */
        DEGISTI,
        /** Değer zaten doğruydu, yapılacak bir şey yok. Sorun sayılmaz. */
        GEREK_YOK,
        /** Değer bozuk ama güvenle onarılamadı. Atlanan olarak raporlanır. */
        ONARILAMADI
    }

    private static final OnarimCiktisi GEREK_YOK = new OnarimCiktisi(Durum.GEREK_YOK, null);
    private static final OnarimCiktisi ONARILAMADI = new OnarimCiktisi(Durum.ONARILAMADI, null);

    private final Durum durum;
    private final String yeniDeger;

    private OnarimCiktisi(Durum durum, String yeniDeger) {
        this.durum = durum;
        this.yeniDeger = yeniDeger;
    }

    public static OnarimCiktisi degisti(String yeniDeger) {
        return new OnarimCiktisi(Durum.DEGISTI, yeniDeger);
    }

    public static OnarimCiktisi gerekYok() {
        return GEREK_YOK;
    }

    public static OnarimCiktisi onarilamadi() {
        return ONARILAMADI;
    }

    public Durum getDurum() {
        return durum;
    }

    public String getYeniDeger() {
        return yeniDeger;
    }
}
