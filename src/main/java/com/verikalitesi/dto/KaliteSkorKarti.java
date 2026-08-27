package com.verikalitesi.dto;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Tablonun DAMA boyutlarına göre kalite skor kartı.
 *
 * <p>Boyutlar sabit sırada tutulur ({@link EnumMap}) ki aynı tablo her çalıştırmada aynı
 * kartı üretsin. Ölçülemeyen boyutlar karttan silinmez, "ölçülemedi" olarak durur --
 * neyin ölçülmediğini göstermek, ölçülenler kadar bilgi taşır.
 */
public class KaliteSkorKarti {

    private final Map<KaliteBoyutuTuru, KaliteBoyutu> boyutlar = new EnumMap<>(KaliteBoyutuTuru.class);

    public KaliteSkorKarti() {
        for (KaliteBoyutuTuru tur : KaliteBoyutuTuru.values()) {
            boyutlar.put(tur, new KaliteBoyutu(tur));
        }
    }

    public void kontrolEkle(KaliteBoyutuTuru tur) {
        boyutlar.get(tur).kontrolEkle();
    }

    public void sorunEkle(KaliteBoyutuTuru tur) {
        boyutlar.get(tur).sorunEkle();
    }

    public void topluEkle(KaliteBoyutuTuru tur, int kontrolEdilen, int sorunlu) {
        KaliteBoyutu boyut = boyutlar.get(tur);
        boyut.setKontrolEdilen(boyut.getKontrolEdilen() + kontrolEdilen);
        boyut.setSorunlu(boyut.getSorunlu() + sorunlu);
    }

    public List<KaliteBoyutu> getBoyutlar() {
        return new ArrayList<>(boyutlar.values());
    }

    public boolean isBosMu() {
        for (KaliteBoyutu boyut : boyutlar.values()) {
            if (boyut.isPuanlanabilir()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Genel puan, puanlanabilen boyutların <b>ağırlıksız</b> ortalamasıdır: her boyut eşit
     * sayılır. Kontrol sayısına göre ağırlıklandırmak, çok hücreli bir boyutun (tamlık)
     * az hücreli bir boyutu (teklik) gölgelemesine yol açardı. Ağırlıkları veriye bakmadan
     * atamak da savunulamaz bir sayı üretirdi.
     */
    public double getGenelPuan() {
        double toplam = 0;
        int sayi = 0;
        for (KaliteBoyutu boyut : boyutlar.values()) {
            if (boyut.isPuanlanabilir()) {
                toplam += boyut.getPuan();
                sayi++;
            }
        }
        if (sayi == 0) {
            return 0;
        }
        return Math.round((toplam / sayi) * 100.0) / 100.0;
    }

    public int getPuanlananBoyutSayisi() {
        int sayi = 0;
        for (KaliteBoyutu boyut : boyutlar.values()) {
            if (boyut.isPuanlanabilir()) {
                sayi++;
            }
        }
        return sayi;
    }
}
