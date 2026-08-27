package com.verikalitesi.dto;

import java.util.ArrayList;
import java.util.List;

public class KolonTemizlemeSonucu {

    private String kolonAdi;

    private int guncellenenSatirSayisi;

    private int atlananSatirSayisi;

    /**
     * Güncellenmek üzere gönderilip veritabanında karşılığı bulunamayan satır sayısı.
     * Atlanan satırlardan farklıdır: atlananlar hiç denenmemiş, bunlar denenmiş ama
     * WHERE koşulu hiçbir satıra uymamıştır.
     */
    private int eslesmeyenSatirSayisi;

    /**
     * Önizlemede kaydedilen eski/yeni değer örnekleri.
     *
     * <p>Uygulama sırasında yeniden okunamazlar: güncelleme tamamlandığında eski değer artık
     * veritabanında yoktur. Bu yüzden önizleme adımında alınıp oturumda taşınırlar --
     * araçta geri alma olmadığı için, neyin değiştiğinin tek kaydı budur.
     */
    private List<TemizlemeOrnek> ornekler = new ArrayList<>();

    public String getKolonAdi() {
        return kolonAdi;
    }

    public void setKolonAdi(String kolonAdi) {
        this.kolonAdi = kolonAdi;
    }

    public int getGuncellenenSatirSayisi() {
        return guncellenenSatirSayisi;
    }

    public void setGuncellenenSatirSayisi(int guncellenenSatirSayisi) {
        this.guncellenenSatirSayisi = guncellenenSatirSayisi;
    }

    public int getAtlananSatirSayisi() {
        return atlananSatirSayisi;
    }

    public void setAtlananSatirSayisi(int atlananSatirSayisi) {
        this.atlananSatirSayisi = atlananSatirSayisi;
    }

    public int getEslesmeyenSatirSayisi() {
        return eslesmeyenSatirSayisi;
    }

    public void setEslesmeyenSatirSayisi(int eslesmeyenSatirSayisi) {
        this.eslesmeyenSatirSayisi = eslesmeyenSatirSayisi;
    }

    public List<TemizlemeOrnek> getOrnekler() {
        return ornekler;
    }

    public void setOrnekler(List<TemizlemeOrnek> ornekler) {
        this.ornekler = ornekler;
    }
}
