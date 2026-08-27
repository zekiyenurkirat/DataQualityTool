package com.verikalitesi.temizleme;

import java.util.Locale;

/**
 * Web adreslerini standart biçime getirir.
 *
 * <p>Yalnızca <b>anlam taşımayan farklılıkları</b> kaldırır:
 *
 * <ul>
 *   <li>Şema ve alan adını küçük harfe çevirir — RFC 3986'ya göre ikisi de büyük/küçük
 *       harf duyarsızdır, yani {@code WWW.X.COM} ile {@code www.x.com} aynı adrestir.</li>
 *   <li>Varsayılan portu siler: {@code http://x.com:80} ile {@code http://x.com} aynı yer.</li>
 *   <li>Alan adından sonraki tek başına eğik çizgiyi siler.</li>
 * </ul>
 *
 * <p><b>Yola (path) dokunmaz.</b> Bu en kritik kural: alan adı büyük/küçük harf duyarsızken
 * yol duyarlıdır. {@code example.com/MyPage} ile {@code example.com/mypage} <b>farklı
 * sayfalardır</b>; tamamını küçültmek adresi başka bir yere çevirmek olur.
 *
 * <p>Eksik şema <b>tamamlanmaz</b>. {@code x.com} adresinin {@code http} mi {@code https} mi
 * olduğu veriden bilinemez; birini seçmek uydurma olurdu.
 */
public class WebSitesiOnarici implements DegerOnarici {

    @Override
    public OnarimCiktisi onar(String hamDeger) {
        if (hamDeger == null || hamDeger.isBlank()) {
            return OnarimCiktisi.gerekYok();
        }

        String temiz = hamDeger.trim();
        if (!webAdresiGibiMi(temiz)) {
            // E-posta, firma adı ya da serbest metin. Standartlaştırılacak bir adres yok;
            // bu bir bulgudur ama onarımı iş kararı, araç kendi başına çeviremez.
            return OnarimCiktisi.onarilamadi();
        }

        String sema = semaAyikla(temiz);
        String kalan = temiz.substring(sema.length());

        int yolBasi = kalan.indexOf('/');
        String otorite = yolBasi < 0 ? kalan : kalan.substring(0, yolBasi);
        String yol = yolBasi < 0 ? "" : kalan.substring(yolBasi);

        String yeniSema = sema.toLowerCase(Locale.ROOT);
        String yeniOtorite = varsayilanPortuSil(otorite.toLowerCase(Locale.ROOT), yeniSema);
        String yeniYol = yol.equals("/") ? "" : yol;

        String sonuc = yeniSema + yeniOtorite + yeniYol;
        return sonuc.equals(temiz) ? OnarimCiktisi.gerekYok() : OnarimCiktisi.degisti(sonuc);
    }

    /**
     * Değerin gerçekten bir web adresi olup olmadığına bakar. Web sitesi kolonunda sıkça
     * e-posta adresi, firma adı ya da serbest metin bulunuyor; onları adres sanıp
     * küçültmek yanlış olur.
     */
    private boolean webAdresiGibiMi(String deger) {
        if (deger.contains("@")) {
            return false;
        }
        for (int i = 0; i < deger.length(); i++) {
            if (Character.isWhitespace(deger.charAt(i))) {
                return false;
            }
        }
        String kucuk = deger.toLowerCase(Locale.ROOT);
        if (kucuk.startsWith("http://") || kucuk.startsWith("https://")) {
            return true;
        }
        // Şemasız değerlerde en azından bir nokta olmalı; "ALMANSAM" alan adı değildir.
        return deger.contains(".");
    }

    /** Varsa {@code http://} veya {@code https://} önekini olduğu gibi döndürür, yoksa boş. */
    private String semaAyikla(String deger) {
        String kucuk = deger.toLowerCase(Locale.ROOT);
        if (kucuk.startsWith("https://")) {
            return deger.substring(0, "https://".length());
        }
        if (kucuk.startsWith("http://")) {
            return deger.substring(0, "http://".length());
        }
        return "";
    }

    /**
     * Şemanın varsayılan portunu siler. Yalnızca eşleşen şemada: {@code https://x.com:80}
     * varsayılan değildir ve dokunulmamalıdır.
     */
    private String varsayilanPortuSil(String otorite, String sema) {
        if (sema.equals("http://") && otorite.endsWith(":80")) {
            return otorite.substring(0, otorite.length() - ":80".length());
        }
        if (sema.equals("https://") && otorite.endsWith(":443")) {
            return otorite.substring(0, otorite.length() - ":443".length());
        }
        return otorite;
    }

    @Override
    public String adi() {
        return "Web adresi standartlaştırma";
    }
}
