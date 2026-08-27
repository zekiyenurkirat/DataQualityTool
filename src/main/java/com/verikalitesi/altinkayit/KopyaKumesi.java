package com.verikalitesi.altinkayit;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Birbirine bağlı kopya adaylarının oluşturduğu küme (match group).
 *
 * <p>Rapordaki B bölümü <strong>çift</strong> listesidir; oysa gerçekte üç, dört
 * ya da daha çok kayıt aynı firmayı gösteriyor olabilir. A~B ve B~C eşleşmesi
 * varsa A, B ve C tek bir kümedir -- ikişerli birleştirme yapmak bu üçlüyü iki
 * ayrı ana kayda bölerdi.
 *
 * <p>Kümeler {@link KumeBulucu} tarafından çift listesinden çıkarılır.
 */
public class KopyaKumesi {

    private final List<String> kimlikler;
    private final Map<String, Map<String, String>> kayitlar;
    private final String etiket;

    KopyaKumesi(List<String> kimlikler, Map<String, Map<String, String>> kayitlar, String etiket) {
        this.kimlikler = Collections.unmodifiableList(kimlikler);
        this.kayitlar = Collections.unmodifiableMap(new LinkedHashMap<>(kayitlar));
        this.etiket = etiket;
    }

    /** Kümedeki kayıtların kimlikleri, sabit sırada. */
    public List<String> getKimlikler() {
        return kimlikler;
    }

    /** Kimlik -> o kaydın eşleştirilmiş kolon değerleri. */
    public Map<String, Map<String, String>> getKayitlar() {
        return kayitlar;
    }

    public Map<String, String> getKayit(String kimlik) {
        return kayitlar.get(kimlik);
    }

    /** Kümeyi ekranda tanıtan değer; genelde ilk kaydın firma adı. */
    public String getEtiket() {
        return etiket;
    }

    public int getKayitSayisi() {
        return kimlikler.size();
    }

    /** Küme anahtarı: sıralı kimliklerin birleşimi. Formdan geri gelirken kümeyi bulmaya yarar. */
    public String getAnahtar() {
        return String.join("~", kimlikler);
    }

    /**
     * Sentezlenecek ana kaydın kimliği.
     *
     * <p>Küme anahtarının özetinden üretiliyor, yani <strong>deterministik</strong>:
     * aynı küme ikinci kez birleştirilirse aynı ana kayda yazılır, kopya bir
     * ana kayıt oluşmaz. Rastgele bir kimlik verilseydi her tıklama yeni bir
     * ana kayıt üretirdi.
     *
     * <p>Ön ek {@code AK-}: bu kimlik kaynak tablodaki hiçbir satıra karşılık
     * gelmiyor, aracın ürettiği bir kayıt. Ekranda ayırt edilebilmeli.
     */
    public String getAltinKayitKimligi() {
        try {
            java.security.MessageDigest ozetleyici = java.security.MessageDigest.getInstance("SHA-256");
            byte[] ozet = ozetleyici.digest(getAnahtar().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder onaltilik = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                onaltilik.append(String.format("%02x", ozet[i]));
            }
            return "AK-" + onaltilik;
        } catch (java.security.NoSuchAlgorithmException hata) {
            // SHA-256 her Java dagitiminda var; buraya dusulmesi mumkun degil.
            throw new IllegalStateException("SHA-256 bulunamadi", hata);
        }
    }
}
