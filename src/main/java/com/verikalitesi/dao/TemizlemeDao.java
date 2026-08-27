package com.verikalitesi.dao;

import com.verikalitesi.core.VeritabaniBaglantiBilgisi;
import com.verikalitesi.dto.TemizlemeKurali;
import com.verikalitesi.dto.TemizlemeOnizlemeSonucu;
import com.verikalitesi.dto.TemizlemeSonucu;

import java.util.List;
import java.util.Map;

public interface TemizlemeDao {

    /**
     * Metin kurallarının etkisini önizler. Kuralın kendisi tek nesne olarak geçiliyor;
     * her yeni seçenek için imzaya parametre eklemek zorunda kalmamak için.
     */
    TemizlemeOnizlemeSonucu metinKolonuOnizle(VeritabaniBaglantiBilgisi bilgi, String tabloAdi,
                                               String idKolonu, TemizlemeKurali kural, int ornekLimiti);

    TemizlemeSonucu tumKurallariUygula(VeritabaniBaglantiBilgisi bilgi, String tabloAdi, String idKolonu,
                                       List<TemizlemeKurali> metinKurallari,
                                       Map<String, Map<String, String>> satirGuncellemeleri);
}
