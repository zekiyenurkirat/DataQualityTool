package com.verikalitesi.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Kalıp kelime tespitinin sonucu ve <b>gerekçesi</b>.
 *
 * <p>Yalnızca bulunan kelimeleri döndürmek yetmiyordu: hiçbir kelime seçilmediğinde kullanıcı
 * ekranda yüksek oranlı kelimeler görüp neden işaretlenmediklerini anlamıyordu. Ölçülen düşüş
 * oranı ve eşik burada taşınıyor ki karar ekranda açıklanabilsin.
 */
public class KalipTespiti {

    private final List<KelimeFrekansi> kalipKelimeler;
    private final double enBuyukDusus;
    private final double esik;

    public KalipTespiti(List<KelimeFrekansi> kalipKelimeler, double enBuyukDusus, double esik) {
        this.kalipKelimeler = kalipKelimeler == null ? new ArrayList<>() : kalipKelimeler;
        this.enBuyukDusus = enBuyukDusus;
        this.esik = esik;
    }

    public static KalipTespiti bos(double esik) {
        return new KalipTespiti(new ArrayList<>(), 0, esik);
    }

    /** Frekans listesinde eşiği aşan bir kırılma bulunup bulunmadığı. */
    public boolean isKirilmaBulundu() {
        return !kalipKelimeler.isEmpty();
    }

    public List<KelimeFrekansi> getKalipKelimeler() {
        return kalipKelimeler;
    }

    public List<String> getKelimeAdlari() {
        List<String> adlar = new ArrayList<>();
        for (KelimeFrekansi kelime : kalipKelimeler) {
            adlar.add(kelime.getKelime());
        }
        return adlar;
    }

    public double getEnBuyukDusus() {
        return enBuyukDusus;
    }

    public double getEsik() {
        return esik;
    }
}
