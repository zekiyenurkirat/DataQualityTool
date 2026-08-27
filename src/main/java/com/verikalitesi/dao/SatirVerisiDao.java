package com.verikalitesi.dao;

import com.verikalitesi.core.VeritabaniBaglantiBilgisi;
import com.verikalitesi.dto.SatirVerisi;

import java.util.List;

public interface SatirVerisiDao {


    List<SatirVerisi> satirlariGetir (VeritabaniBaglantiBilgisi bilgi, String tabloAdi, List<String>kolonlar, String idKolonu);
}
