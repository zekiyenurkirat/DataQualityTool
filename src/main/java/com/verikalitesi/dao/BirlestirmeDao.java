package com.verikalitesi.dao;

import com.verikalitesi.core.VeritabaniBaglantiBilgisi;
import com.verikalitesi.core.VeritabaniBaglantiHatasi;

import java.util.List;
import java.util.Map;

public interface BirlestirmeDao {

    /**
     * İki kayıtlık basit birleştirme: biri ana kayıt olur, diğeri ona bağlanır.
     *
     * <p>Kaybeden satır <strong>silinmez</strong>; ayrı bir çapraz referans
     * tablosunda pasife çekilip kazanan kaydın kimliğine bağlanır.
     *
     * @return yazılan satır sayısı (kazanan + kaybeden = 2)
     */
    int birlestirmeyiKaydet(VeritabaniBaglantiBilgisi bilgi, String tabloAdi,
                            String altinKayitKimligi, String pasifKayitKimligi,
                            String gerekce) throws VeritabaniBaglantiHatasi;

    /**
     * Alan seviyesinde birleştirme: N kayıttan sentezlenmiş ana kaydı yazar ve
     * kümedeki bütün kayıtları ona bağlar.
     *
     * <p>Ana kayıt <strong>kaynak tabloya eklenmiyor.</strong> Aracın kendi
     * tablosunda, kolon başına bir satır olarak saklanıyor; her satır o değerin
     * hangi kaynak kayıttan geldiğini de taşıyor (alan seviyesinde veri
     * soyağacı).
     *
     * @param altinKayitId   sentezlenen ana kaydın kimliği
     * @param degerler       kolon adı -> ana kayda yazılan değer
     * @param kaynaklar      kolon adı -> değerin alındığı kaynak kaydın kimliği
     * @param uyeKimlikler   kümedeki bütün kayıtların kimlikleri
     * @return yazılan satır sayısı
     */
    int altinKaydiSentezle(VeritabaniBaglantiBilgisi bilgi, String tabloAdi,
                           String altinKayitId, Map<String, String> degerler,
                           Map<String, String> kaynaklar, List<String> uyeKimlikler,
                           String gerekce) throws VeritabaniBaglantiHatasi;
}
