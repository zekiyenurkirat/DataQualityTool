package com.verikalitesi.temizleme;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

/**
 * Telefon numaralarını seçilen hedef biçime çevirir.
 *
 * <p>Numara ayrıştırılamıyorsa değişiklik yapılmaz. Ayrıştırma ülke koduna bağlıdır:
 * başında {@code +} olmayan bir numaranın hangi ülkeye ait olduğu bilinmeden alan kodu
 * çözülemez, bu yüzden ülke seçilmemişse çoğu numara onarılamaz.
 */
public class TelefonOnarici implements DegerOnarici {

    private final PhoneNumberUtil util = PhoneNumberUtil.getInstance();
    private final String ulkeKodu;
    private final PhoneNumberUtil.PhoneNumberFormat hedefFormat;

    public TelefonOnarici(String ulkeKodu, String hedefFormatAdi) {
        this.ulkeKodu = ulkeKodu;
        this.hedefFormat = hedefFormatiCoz(hedefFormatAdi);
    }

    @Override
    public OnarimCiktisi onar(String hamDeger) {
        if (hamDeger == null || hamDeger.isBlank()) {
            return OnarimCiktisi.gerekYok();
        }
        try {
            Phonenumber.PhoneNumber numara = util.parse(hamDeger, ulkeKodu);
            String yeni = util.format(numara, hedefFormat);
            return yeni.equals(hamDeger) ? OnarimCiktisi.gerekYok() : OnarimCiktisi.degisti(yeni);
        } catch (NumberParseException hata) {
            return OnarimCiktisi.onarilamadi();
        }
    }

    /** Tanınmayan ya da boş biçim adı için uluslararası biçime düşülür. */
    private PhoneNumberUtil.PhoneNumberFormat hedefFormatiCoz(String hedefFormatAdi) {
        if (hedefFormatAdi == null || hedefFormatAdi.isBlank()) {
            return PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL;
        }
        try {
            return PhoneNumberUtil.PhoneNumberFormat.valueOf(hedefFormatAdi);
        } catch (IllegalArgumentException hata) {
            return PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL;
        }
    }

    @Override
    public String adi() {
        return "Telefon biçimi";
    }
}
