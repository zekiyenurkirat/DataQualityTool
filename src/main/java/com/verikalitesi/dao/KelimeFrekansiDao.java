package com.verikalitesi.dao;

import com.verikalitesi.core.VeritabaniBaglantiBilgisi;
import com.verikalitesi.core.VeritabaniBaglantiHatasi;
import com.verikalitesi.dto.KelimeFrekansi;

import java.util.List;

public interface KelimeFrekansiDao {

    List<KelimeFrekansi> kelimeleriGetir(VeritabaniBaglantiBilgisi bilgi, String tabloAdi, String kolonAdi) throws VeritabaniBaglantiHatasi;
    int toplamFirmaSayisiniGetir(VeritabaniBaglantiBilgisi bilgi, String tabloAdi, String kolonAdi) throws VeritabaniBaglantiHatasi;
}
