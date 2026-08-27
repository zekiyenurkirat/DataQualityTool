package com.verikalitesi.dao;

import com.verikalitesi.dto.KolonBilgisi;

import com.verikalitesi.core.VeritabaniBaglantiBilgisi;
import com.verikalitesi.core.VeritabaniBaglantiHatasi;

import java.util.List;

public interface SemaDao {

    List <KolonBilgisi> kolonlariGetir(VeritabaniBaglantiBilgisi baglantiBilgisi, String semaAdi, String tabloAdi) throws VeritabaniBaglantiHatasi;
    List<String> tablolariGetir(VeritabaniBaglantiBilgisi baglantiBilgisi, String semaAdi) throws VeritabaniBaglantiHatasi;
}
