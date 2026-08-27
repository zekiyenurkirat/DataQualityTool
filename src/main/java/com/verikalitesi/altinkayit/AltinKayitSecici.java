package com.verikalitesi.altinkayit;

import com.verikalitesi.rule.PlaceholderKurali;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * İki benzer kayıttan hangisinin ana kayıt (golden record) olacağına karar verir.
 *
 * <p><strong>Kural zinciri.</strong> Kurumsal araçlarda survivorship tek bir
 * kural değil, sıralı bir kural listesidir; ilk ayırt eden kural kazanır.
 * Buradaki sıra:
 *
 * <ol>
 *   <li><strong>Doluluk (completeness)</strong> -- daha çok anlamlı alanı dolu
 *       olan kazanır.</li>
 *   <li><strong>Karakter uzunluğu</strong> -- doluluk eşitse, anlamlı
 *       değerlerin toplam uzunluğu büyük olan kazanır.</li>
 *   <li><strong>Kimlik sırası</strong> -- ikisi de eşitse, kimliği alfabetik
 *       olarak önce gelen kazanır.</li>
 * </ol>
 *
 * <p><strong>Zincir neden sonuna kadar gidiyor:</strong> araç her durumda bir
 * karar üretmeli ve o karar <em>deterministik</em> olmalı. Aynı veri üzerinde
 * iki çalıştırma iki farklı ana kayıt seçerse birleştirme tekrarlanamaz olur;
 * yeni bir çekim (scrape) geldiğinde eski kararlarla çelişir. Rastgele seçim
 * bu yüzden yok.
 *
 * <p><strong>Ama zayıf karar saklanmıyor:</strong> hangi kuralın karar verdiği
 * sonuçla birlikte taşınıyor. Yalnızca doluluk güçlü sayılıyor; diğer ikisiyle
 * verilen kararlarda ekran kullanıcıya diğer kaydı seçme imkânı sunuyor.
 * "Yapamadığını sessizce yanlış yapma" ilkesinin buradaki karşılığı bu:
 * karar veriliyor ama neye dayandığı gizlenmiyor.
 *
 * <p><strong>Yazılmayan iki kural.</strong> Sektörde doluluğun yanında iki
 * standart kural daha vardır: güncellik (recency) ve kaynak güveni (source
 * trust). İkisi de bu veride ölçülemiyor -- tablolarda güvenilir bir "son
 * güncelleme" tarihi kolonu da, kaydın hangi kaynaktan geldiğini söyleyen bir
 * kolon da yok. Ölçemediğim bir kuralı yazmak, çalışıyormuş gibi görünüp
 * rastgele karar veren bir kural üretirdi.
 *
 * <p>Karşılaştırma <strong>yalnızca eşleştirilen kolonlar</strong> üzerinden
 * yapılır; eşleştirilmemiş bir kolonda veri olsa bile araç onu görmez.
 */
public class AltinKayitSecici {

    private final PlaceholderKurali placeholderKurali;

    public AltinKayitSecici(PlaceholderKurali placeholderKurali) {
        this.placeholderKurali = placeholderKurali;
    }

    /**
     * @param kimlik1  birinci kaydın kimliği
     * @param alanlar1 birinci kaydın eşleştirilmiş kolon değerleri
     * @param kimlik2  ikinci kaydın kimliği
     * @param alanlar2 ikinci kaydın eşleştirilmiş kolon değerleri
     */
    public AltinKayitKarari sec(String kimlik1, Map<String, String> alanlar1,
                                String kimlik2, Map<String, String> alanlar2) {

        // İki kayıtta da geçen kolonların birleşimi üzerinden sayıyoruz. Yalnızca
        // birinde bulunan bir kolon, diğerinde "boş" sayılmalı -- paydayı tek
        // tarafa göre almak o tarafı haksız kazandırırdı.
        Set<String> kolonlar = new LinkedHashSet<>();
        if (alanlar1 != null) kolonlar.addAll(alanlar1.keySet());
        if (alanlar2 != null) kolonlar.addAll(alanlar2.keySet());

        int doluluk1 = 0, doluluk2 = 0, uzunluk1 = 0, uzunluk2 = 0;
        for (String kolon : kolonlar) {
            String deger1 = anlamliDeger(kolon, alanlar1);
            String deger2 = anlamliDeger(kolon, alanlar2);
            if (deger1 != null) {
                doluluk1++;
                uzunluk1 += deger1.length();
            }
            if (deger2 != null) {
                doluluk2++;
                uzunluk2 += deger2.length();
            }
        }
        int toplam = kolonlar.size();

        // 1. kural: doluluk
        if (doluluk1 != doluluk2) {
            return karar(doluluk1 > doluluk2, kimlik1, kimlik2, AltinKayitKarari.Kural.DOLULUK,
                    doluluk1, doluluk2, toplam, uzunluk1, uzunluk2);
        }

        // 2. kural: anlamlı değerlerin toplam karakter uzunluğu
        if (uzunluk1 != uzunluk2) {
            return karar(uzunluk1 > uzunluk2, kimlik1, kimlik2, AltinKayitKarari.Kural.KARAKTER_UZUNLUGU,
                    doluluk1, doluluk2, toplam, uzunluk1, uzunluk2);
        }

        // 3. kural: kimlik sırası. Bilgi taşımaz, yalnızca determinizm sağlar.
        // compareTo < 0 ise birinci kimlik alfabetik olarak önce geliyor demektir.
        boolean birinciKazanir = kimlikSirasi(kimlik1, kimlik2) <= 0;
        return karar(birinciKazanir, kimlik1, kimlik2, AltinKayitKarari.Kural.KIMLIK_SIRASI,
                doluluk1, doluluk2, toplam, uzunluk1, uzunluk2);
    }

    private int kimlikSirasi(String kimlik1, String kimlik2) {
        String a = kimlik1 == null ? "" : kimlik1;
        String b = kimlik2 == null ? "" : kimlik2;
        return a.compareTo(b);
    }

    private AltinKayitKarari karar(boolean birinciKazanir, String kimlik1, String kimlik2,
                                   AltinKayitKarari.Kural kural, int doluluk1, int doluluk2,
                                   int toplam, int uzunluk1, int uzunluk2) {
        String kazanan = birinciKazanir ? kimlik1 : kimlik2;
        String kaybeden = birinciKazanir ? kimlik2 : kimlik1;
        return new AltinKayitKarari(kazanan, kaybeden, kural,
                doluluk1, doluluk2, toplam, uzunluk1, uzunluk2);
    }

    /**
     * Bir kolonun o kayıttaki anlamlı değeri; bilgi taşımıyorsa {@code null}.
     *
     * <p>Bir değer şu üç durumda anlamsızdır: yok, yalnızca boşluktan ibaret,
     * ya da yer tutucu. Yer tutucu kontrolü için doğrulama kurallarının
     * kullandığı sınıfın aynısı çağrılıyor; böylece "-" ve "N/A" burada da boş
     * kabul ediliyor ve "boş" tanımı aracın iki yerinde ayrışmıyor.
     *
     * <p>Uzunluk kırpılmış değer üzerinden sayılıyor: baştaki ve sondaki
     * boşluklar bilgi değil, sayılsalardı kirli kayıt temiz kayda karşı
     * haksız avantaj kazanırdı.
     */
    private String anlamliDeger(String kolon, Map<String, String> alanlar) {
        if (alanlar == null) {
            return null;
        }
        String deger = alanlar.get(kolon);
        if (deger == null || deger.isBlank()) {
            return null;
        }
        if (placeholderKurali.kontrolEt("", kolon, deger).isPresent()) {
            return null;
        }
        return deger.trim();
    }
}
