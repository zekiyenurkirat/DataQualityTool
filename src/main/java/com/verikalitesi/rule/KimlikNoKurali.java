package com.verikalitesi.rule;

import com.verikalitesi.dto.DuzeltmeEylemi;
import com.verikalitesi.dto.KimlikNoProfili;
import com.verikalitesi.dto.SekilDagilimi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Kimlik numarası kolonunu kalıp tutarlılığı açısından denetler.
 *
 * <p>Diğer kurallardan farklı olarak tek satıra bakarak karar veremez. "Bu e-posta geçerli mi?"
 * sorusu satırın kendisinden cevaplanır; "bu kimlik numarası kalıba uyuyor mu?" sorusu ise önce
 * kalıbın ne olduğunu bilmeyi gerektirir, o da kolonun tamamından çıkar. Bu yüzden iki aşamalıdır:
 * önce kolon profillenir ({@link #kolonuCozumle}), sonra satırlar profile karşı denetlenir
 * ({@link #kaliptanSapanlariBul}).
 *
 * <p><b>Bu sınıf hiçbir değeri değiştirmez.</b> Kimlik numaralarında otomatik normalizasyon
 * yapmak tehlikelidir: {@code 226879-3301-Ф-л} kaydındaki "Ф-л" şube demektir, silinirse şube
 * ile merkez tek kayda düşer ve geri dönüşü olmaz. Araç yalnızca işaretler, kararı insana bırakır.
 */
@Component
public class KimlikNoKurali {

    private static final int DAGILIMDA_GOSTERILECEK_SEKIL = 6;

    private final double baskinKalipOrani;

    public KimlikNoKurali(@Value("${veri.kimlikNoBaskinKalipOrani:0.8}") double baskinKalipOrani) {
        this.baskinKalipOrani = baskinKalipOrani;
    }

    /**
     * Kolonu profiller. Baskın kalıp yalnızca değerlerin {@code baskinKalipOrani} kadarını
     * kapsıyorsa belirlenir; kapsamıyorsa kolonda kalıp yok sayılır ve hiçbir satır işaretlenmez.
     */
    public KimlikNoProfili kolonuCozumle(String kolonAdi, List<String> degerler) {
        KimlikNoProfili profil = new KimlikNoProfili();
        profil.setKolonAdi(kolonAdi);

        Map<String, SekilDagilimi> sekiller = new LinkedHashMap<>();
        int toplam = 0;
        int enKisa = Integer.MAX_VALUE;
        int enUzun = 0;

        for (String deger : degerler) {
            if (deger == null || deger.isBlank()) {
                continue;
            }
            String temiz = deger.trim();
            toplam++;
            enKisa = Math.min(enKisa, temiz.length());
            enUzun = Math.max(enUzun, temiz.length());

            String sekil = sekilCikar(temiz);
            sekiller.computeIfAbsent(sekil, s -> new SekilDagilimi(s, temiz)).arttir();
        }

        profil.setToplamDeger(toplam);
        profil.setEnKisaUzunluk(toplam == 0 ? 0 : enKisa);
        profil.setEnUzunUzunluk(enUzun);

        if (toplam == 0) {
            return profil;
        }

        List<SekilDagilimi> dagilim = new ArrayList<>(sekiller.values());
        dagilim.sort((a, b) -> Integer.compare(b.getAdet(), a.getAdet()));

        SekilDagilimi enSik = dagilim.get(0);
        double kapsama = (double) enSik.getAdet() / toplam;
        profil.setKapsamaOrani(kapsama);
        profil.setDagilim(dagilim.size() > DAGILIMDA_GOSTERILECEK_SEKIL
                ? new ArrayList<>(dagilim.subList(0, DAGILIMDA_GOSTERILECEK_SEKIL))
                : dagilim);

        // Koruma: baskın kalıp yoksa kolonda "kalıba uymayan satır" kavramı da yoktur.
        // Bu olmadan UUID kolonunda her satır hatalı sayılırdı.
        if (kapsama >= baskinKalipOrani) {
            profil.setBaskinSekil(enSik.getSekil());
            profil.setBaskinSekilAdedi(enSik.getAdet());
        }

        return profil;
    }

    /**
     * Profildeki baskın kalıba uymayan satırlar için bulgu üretir.
     *
     * @param satirNolari bulgunun bağlanacağı satır kimlikleri
     * @param degerler    aynı sıradaki ham kimlik numarası değerleri
     */
    public List<DogrulamaSonucu> kaliptanSapanlariBul(KimlikNoProfili profil, String alanAdi,
                                                       List<String> satirNolari, List<String> degerler) {
        List<DogrulamaSonucu> sonuclar = new ArrayList<>();
        if (profil == null || !profil.isBaskinSekilVarMi()) {
            return sonuclar;
        }

        int sayi = Math.min(satirNolari.size(), degerler.size());
        for (int i = 0; i < sayi; i++) {
            String deger = degerler.get(i);
            if (deger == null || deger.isBlank()) {
                continue;
            }
            String sekil = sekilCikar(deger.trim());
            if (sekil.equals(profil.getBaskinSekil())) {
                continue;
            }

            DogrulamaSonucu sonuc = new DogrulamaSonucu();
            sonuc.setSatirNo(satirNolari.get(i));
            sonuc.setAlanAdi(alanAdi);
            sonuc.setDeger(deger);
            sonuc.setEylem(DuzeltmeEylemi.DUZELTILMIYOR);
            sonuc.setEylemNotu("Kalıp dışı ek bir şube kodu olabilir; silmek iki ayrı tüzel kişiliği tek kayda düşürürdü.");
            sonuc.setMesaj("Kimlik numarası kolondaki baskın kalıba uymuyor"
                    + " (beklenen " + profil.getBaskinSekil() + ", bulunan " + sekil + ")."
                    + " Ek bir bölüm içeriyor olabilir (şube, kontrol hanesi);"
                    + " otomatik birleştirilmedi, inceleme gerekir.");
            sonuclar.add(sonuc);
        }
        return sonuclar;
    }

    /**
     * Değeri karakter sınıflarına indirger: ardışık rakamlar tek {@code 9}, ardışık harfler
     * tek {@code A}, ayraçlar olduğu gibi kalır.
     *
     * <p>Ardışık olanların tek sembole inmesi şart: aksi halde 9 haneli ve 10 haneli numaralar
     * farklı kalıp sayılırdı. Uzunluk farkı ayrı bir sinyal, kalıp farkı ayrı.
     *
     * <p>{@code Character.isLetter} kullanılıyor çünkü kolon Kiril ({@code ООО}) veya Arapça
     * harf içerebiliyor; {@code a-z} kontrolü bunları ayraç sanardı.
     */
    private String sekilCikar(String deger) {
        StringBuilder sekil = new StringBuilder();
        char oncekiSinif = 0;
        for (int i = 0; i < deger.length(); i++) {
            char karakter = deger.charAt(i);
            char sinif;
            if (Character.isDigit(karakter)) {
                sinif = '9';
            } else if (Character.isLetter(karakter)) {
                sinif = 'A';
            } else {
                sinif = karakter;
            }

            boolean ardisikAyniSinif = (sinif == '9' || sinif == 'A') && sinif == oncekiSinif;
            if (!ardisikAyniSinif) {
                sekil.append(sinif);
            }
            oncekiSinif = sinif;
        }
        return sekil.toString();
    }
}
