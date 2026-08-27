package com.verikalitesi.temizleme;

import org.apache.commons.validator.routines.EmailValidator;

/**
 * Bozuk e-posta adreslerini onarır.
 *
 * <p>Yalnızca <b>fazlalığı kaldırır</b>, eksik olanı asla tamamlamaz: iç boşluk, tekrarlayan
 * {@code @} ve nokta, baştaki ve sondaki noktalama. Eksik bir alan adı ya da eksik {@code @}
 * uydurulmaz -- o bir tahmin olurdu ve yanlış veri üretirdi.
 *
 * <p><b>En kritik kural:</b> onarım sonucu geçerli bir e-posta değilse değişiklik uygulanmaz.
 * "temizlenmiş ama hâlâ bozuk" bir değer üretmek, bozuk değeri olduğu gibi bırakmaktan
 * kötüdür: kullanıcı onarıldığını sanır, üstelik orijinal kayıt da geri alınamaz.
 *
 * <p>Küçük harfe çevirme burada yapılmaz; o ayrı bir seçenek olarak zaten var ve
 * kullanıcının açıp kapatabilmesi gerekiyor.
 */
public class EpostaOnarici implements DegerOnarici {

    private static final String BASTA_SONDA_NOKTALAMA = "^[.,;:_\\-]+|[.,;:_\\-]+$";

    private final EmailValidator dogrulayici = EmailValidator.getInstance();

    @Override
    public OnarimCiktisi onar(String hamDeger) {
        if (hamDeger == null || hamDeger.isBlank()) {
            return OnarimCiktisi.gerekYok();
        }

        String mevcut = hamDeger.trim();

        // Zaten geçerliyse dokunmuyoruz. Geçerli bir adresi "düzeltmek" ancak bozabilir.
        if (dogrulayici.isValid(mevcut)) {
            return OnarimCiktisi.gerekYok();
        }

        // Birden fazla adres varsa onarım yapılmaz: hangisinin kalacağı iş kararıdır,
        // araç kendi başına birini seçemez.
        if (birdenFazlaAdresVarMi(mevcut)) {
            return OnarimCiktisi.onarilamadi();
        }

        String onarilmis = mevcut
                .replaceAll("\\s+", "")
                .replaceAll("@{2,}", "@")
                .replaceAll("\\.{2,}", ".")
                .replaceAll(BASTA_SONDA_NOKTALAMA, "");

        if (onarilmis.equals(mevcut) || !dogrulayici.isValid(onarilmis)) {
            return OnarimCiktisi.onarilamadi();
        }
        return OnarimCiktisi.degisti(onarilmis);
    }

    /**
     * {@code @} işareti tekrarlayan bir yazım hatası da olabilir ({@code a@@b.com}), iki ayrı
     * adres de ({@code a@x.com b@y.com}). Ayırt etmek için boşluk/noktalı virgül/virgülle
     * bölüp kaç parçanın adres gibi göründüğüne bakıyoruz.
     */
    private boolean birdenFazlaAdresVarMi(String deger) {
        String[] parcalar = deger.split("[;,\\s]+");
        int adresBenzeri = 0;
        for (String parca : parcalar) {
            if (parca.contains("@")) {
                adresBenzeri++;
            }
        }
        return adresBenzeri > 1;
    }

    @Override
    public String adi() {
        return "E-posta onarımı";
    }
}
