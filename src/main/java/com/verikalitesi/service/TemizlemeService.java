package com.verikalitesi.service;

import com.verikalitesi.core.VeritabaniBaglantiBilgisi;
import com.verikalitesi.dao.SatirVerisiDao;
import com.verikalitesi.dao.TemizlemeDao;
import com.verikalitesi.dto.KolonTemizlemeSonucu;
import com.verikalitesi.dto.SatirVerisi;
import com.verikalitesi.dto.TemizlemeKurali;
import com.verikalitesi.dto.TemizlemeOnizlemeSonucu;
import com.verikalitesi.dto.TemizlemeOrnek;
import com.verikalitesi.dto.TemizlemeSonucu;
import com.verikalitesi.temizleme.DegerOnarici;
import com.verikalitesi.temizleme.EpostaOnarici;
import com.verikalitesi.temizleme.OnarimCiktisi;
import com.verikalitesi.temizleme.TelefonOnarici;
import com.verikalitesi.temizleme.WebSitesiOnarici;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TemizlemeService {

    private static final int ORNEK_LIMITI = 10;

    private final TemizlemeDao temizlemeDao;
    private final SatirVerisiDao satirVerisiDao;

    public TemizlemeService(TemizlemeDao temizlemeDao, SatirVerisiDao satirVerisiDao) {
        this.temizlemeDao = temizlemeDao;
        this.satirVerisiDao = satirVerisiDao;
    }

    public List<TemizlemeOnizlemeSonucu> onizlemeOlustur(VeritabaniBaglantiBilgisi bilgi, String tabloAdi,
                                                           String idKolonu, String ulkeKodu,
                                                           List<TemizlemeKurali> kurallar) {

        if (kurallar == null) {
            return new ArrayList<>();
        }

        List<TemizlemeOnizlemeSonucu> sonuclar = new ArrayList<>();
        for (TemizlemeKurali kural : kurallar) {
            Optional<DegerOnarici> onarici = onariciSec(kural, ulkeKodu);
            if (onarici.isPresent()) {
                sonuclar.add(onariciOnizlemesi(bilgi, tabloAdi, idKolonu, kural, onarici.get()));
            } else {
                sonuclar.add(temizlemeDao.metinKolonuOnizle(bilgi, tabloAdi, idKolonu, kural, ORNEK_LIMITI));
            }
        }
        return sonuclar;
    }

    public TemizlemeSonucu temizlemeyiUygula(VeritabaniBaglantiBilgisi bilgi, String tabloAdi, String idKolonu,
                                              String ulkeKodu, List<TemizlemeKurali> kurallar) {

        if (kurallar == null) {
            kurallar = new ArrayList<>();
        }

        List<TemizlemeKurali> metinKurallari = new ArrayList<>();
        Map<String, Map<String, String>> satirGuncellemeleri = new HashMap<>();
        Map<String, Integer> atlananSayilari = new HashMap<>();

        for (TemizlemeKurali kural : kurallar) {
            Optional<DegerOnarici> onarici = onariciSec(kural, ulkeKodu);
            if (onarici.isEmpty()) {
                metinKurallari.add(kural);
                continue;
            }

            OnarimSonucu hesaplama = degisiklikleriHesapla(bilgi, tabloAdi, idKolonu,
                    kural.getKolonAdi(), onarici.get());

            Map<String, String> guncellemeler = new HashMap<>();
            for (TemizlemeOrnek degisen : hesaplama.degisenler) {
                guncellemeler.put(degisen.getId(), degisen.getYeniDeger());
            }

            satirGuncellemeleri.put(kural.getKolonAdi(), guncellemeler);
            atlananSayilari.put(kural.getKolonAdi(), hesaplama.atlanan);
        }

        TemizlemeSonucu sonuc = temizlemeDao.tumKurallariUygula(bilgi, tabloAdi, idKolonu,
                metinKurallari, satirGuncellemeleri);

        for (KolonTemizlemeSonucu kolonSonucu : sonuc.getKolonSonuclari()) {
            Integer atlanan = atlananSayilari.get(kolonSonucu.getKolonAdi());
            if (atlanan != null) {
                kolonSonucu.setAtlananSatirSayisi(atlanan);
            }
        }

        return sonuc;
    }

    /**
     * Kuralın hangi onarıcıya karşılık geldiğini bulur. Onarıcı gerektirmeyen kurallar
     * (boşluk, karakter silme, küçük harf) SQL ile toplu çalıştığı için burada boş döner.
     *
     * <p>Aynı kolonda hem telefon hem e-posta onarımı seçilemez; ekranda ikisi farklı
     * kolonlara bağlı olduğu için pratikte çakışmaz, yine de telefon önce değerlendirilir.
     */
    private Optional<DegerOnarici> onariciSec(TemizlemeKurali kural, String ulkeKodu) {
        if (kural.isTelefonFormatiUygulanacakMi()) {
            return Optional.of(new TelefonOnarici(ulkeKodu, kural.getTelefonHedefFormati()));
        }
        if (kural.isEpostaOnarimiUygulanacakMi()) {
            return Optional.of(new EpostaOnarici());
        }
        if (kural.isWebsiteOnarimiUygulanacakMi()) {
            return Optional.of(new WebSitesiOnarici());
        }
        return Optional.empty();
    }

    private TemizlemeOnizlemeSonucu onariciOnizlemesi(VeritabaniBaglantiBilgisi bilgi, String tabloAdi,
                                                        String idKolonu, TemizlemeKurali kural,
                                                        DegerOnarici onarici) {

        OnarimSonucu hesaplama = degisiklikleriHesapla(bilgi, tabloAdi, idKolonu, kural.getKolonAdi(), onarici);

        List<TemizlemeOrnek> ornekler = hesaplama.degisenler.size() > ORNEK_LIMITI
                ? new ArrayList<>(hesaplama.degisenler.subList(0, ORNEK_LIMITI))
                : hesaplama.degisenler;

        TemizlemeOnizlemeSonucu sonuc = new TemizlemeOnizlemeSonucu();
        sonuc.setKolonAdi(kural.getKolonAdi());
        sonuc.setEtkilenecekSatirSayisi(hesaplama.degisenler.size());
        sonuc.setOrnekler(ornekler);
        return sonuc;
    }

    /**
     * Kolonu satır satır gezip onarıcının önerdiği değişiklikleri toplar.
     *
     * <p>Kimliği olmayan satır <b>atlanan</b> sayılır: güncelleme {@code WHERE kimlik = ?} ile
     * yapıldığı için araç tarafından üretilmiş kimlikler veritabanında bulunamaz. Değeri boş
     * olan satır ise atlanan sayılmaz -- onarılacak bir şey yoktur, bu bir eksiklik değildir.
     */
    private OnarimSonucu degisiklikleriHesapla(VeritabaniBaglantiBilgisi bilgi, String tabloAdi,
                                                 String idKolonu, String kolonAdi, DegerOnarici onarici) {

        List<SatirVerisi> satirlar = satirVerisiDao.satirlariGetir(bilgi, tabloAdi, List.of(kolonAdi), idKolonu);

        List<TemizlemeOrnek> degisenler = new ArrayList<>();
        int atlanan = 0;

        for (SatirVerisi satir : satirlar) {
            if (satir.getId() == null || satir.getId().isBlank()) {
                atlanan++;
                continue;
            }
            String eski = satir.getAlanlar().get(kolonAdi);
            if (eski == null || eski.isBlank()) {
                continue;
            }

            OnarimCiktisi cikti = onarici.onar(eski);
            if (cikti.getDurum() == OnarimCiktisi.Durum.ONARILAMADI) {
                atlanan++;
                continue;
            }
            if (cikti.getDurum() == OnarimCiktisi.Durum.GEREK_YOK) {
                // Zaten doğru; ne değişiklik ne de sorun.
                continue;
            }

            TemizlemeOrnek ornek = new TemizlemeOrnek();
            ornek.setId(satir.getId());
            ornek.setEskiDeger(eski);
            ornek.setYeniDeger(cikti.getYeniDeger());
            degisenler.add(ornek);
        }

        return new OnarimSonucu(degisenler, atlanan);
    }

    private static class OnarimSonucu {
        private final List<TemizlemeOrnek> degisenler;
        private final int atlanan;

        private OnarimSonucu(List<TemizlemeOrnek> degisenler, int atlanan) {
            this.degisenler = degisenler;
            this.atlanan = atlanan;
        }
    }
}
