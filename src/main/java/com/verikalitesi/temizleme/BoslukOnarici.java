package com.verikalitesi.temizleme;

/**
 * Metin alanlarındaki boşluk kirini temizler: baştaki ve sondaki boşlukları
 * atar, ortadaki ardışık boşlukları teke indirir.
 *
 * <p>Diğer onarıcıların aksine hiçbir şeyi <em>yorumlamıyor</em> -- e-posta ya
 * da web adresi olup olmadığına bakmıyor, yalnızca boşluk düzenliyor. Bu yüzden
 * her metin kolonunda güvenle çalışır ve ana kayıt sentezinde, özel bir onarıcısı
 * olmayan kolonların varsayılanı bu.
 *
 * <p>Sekme ve satır sonu da boşluk sayılıyor ({@code \s}); Excel'den gelen
 * veride bunlar sık görülüyor ve gözle fark edilmiyor.
 */
public class BoslukOnarici implements DegerOnarici {

    @Override
    public OnarimCiktisi onar(String hamDeger) {
        if (hamDeger == null) {
            return OnarimCiktisi.gerekYok();
        }
        String temiz = hamDeger.replaceAll("\\s+", " ").trim();
        if (temiz.equals(hamDeger)) {
            return OnarimCiktisi.gerekYok();
        }
        // Değer tamamen boşluktan ibaretse geriye boş metin kalır; bu bir onarım
        // değil, "zaten bilgi yoktu" durumudur. Yine de değişiklik olarak
        // bildiriliyor ki kullanıcı ekranda ham hâlin boşluk olduğunu görebilsin.
        return OnarimCiktisi.degisti(temiz);
    }

    @Override
    public String adi() {
        return "Boşluk düzenleme";
    }
}
