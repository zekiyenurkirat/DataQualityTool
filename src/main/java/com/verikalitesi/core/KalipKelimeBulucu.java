package com.verikalitesi.core;

import com.verikalitesi.dto.KalipTespiti;
import com.verikalitesi.dto.KelimeFrekansi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Firma adlarında sık geçen kalıp kelimeleri (hukuki unvan, faaliyet tanımı) bulur.
 * Bunlar firmayı ayırt etmedikleri için benzerlik karşılaştırmasında gürültü yaratır.
 *
 * <p>Sabit bir yüzde eşiği yerine frekans listesindeki en büyük düşüşü arar. Gerçek kalıp
 * kelimeler ile sıradan kelimeler arasında keskin bir uçurum bulunur; kalıp kelime içermeyen
 * bir kolonda ise düşüş yumuşaktır. {@code enAzDususOrani} bu ikisini ayırır: uçurum yeterince
 * derin değilse hiçbir kelime hariç tutulmaz, çünkü hariç tutulacak kalıp yok demektir.
 */
@Component
public class KalipKelimeBulucu {

    private final double enAzDususOrani;

    public KalipKelimeBulucu(@Value("${veri.kalipKelimeEnAzDususOrani:2.0}") double enAzDususOrani) {
        this.enAzDususOrani = enAzDususOrani;
    }

    /**
     * @param kelimeler frekans listesi; sıralı gelmesi gerekmez
     * @return bulunan kalıp kelimeler ve kararın gerekçesi (ölçülen en büyük düşüş, eşik).
     *         Uçurum bulunamazsa kelime listesi boş döner ama ölçüm yine taşınır --
     *         kullanıcı neden hiçbir şey seçilmediğini görebilmeli.
     */
    public KalipTespiti kalipKelimeleriBul(List<KelimeFrekansi> kelimeler) {
        // Tek kelimeyle düşüş oranı hesaplanamaz; karşılaştıracak ikinci nokta yok.
        if (kelimeler == null || kelimeler.size() < 2) {
            return KalipTespiti.bos(enAzDususOrani);
        }

        List<KelimeFrekansi> sirali = new ArrayList<>(kelimeler);
        sirali.sort((a, b) -> Integer.compare(b.getTekrarSayisi(), a.getTekrarSayisi()));

        int dirsekIndeksi = -1;
        double enBuyukDusus = 0;
        for (int i = 0; i < sirali.size() - 1; i++) {
            int sonraki = sirali.get(i + 1).getTekrarSayisi();
            if (sonraki <= 0) {
                continue;
            }
            double dusus = (double) sirali.get(i).getTekrarSayisi() / sonraki;
            // Eşitlikte ilk sıra korunur; böylece aynı veri her zaman aynı sonucu verir.
            if (dusus > enBuyukDusus) {
                enBuyukDusus = dusus;
                dirsekIndeksi = i;
            }
        }

        if (dirsekIndeksi < 0 || enBuyukDusus < enAzDususOrani) {
            return new KalipTespiti(new ArrayList<>(), enBuyukDusus, enAzDususOrani);
        }

        return new KalipTespiti(new ArrayList<>(sirali.subList(0, dirsekIndeksi + 1)),
                enBuyukDusus, enAzDususOrani);
    }

    public double getEnAzDususOrani() {
        return enAzDususOrani;
    }
}
