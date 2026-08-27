package com.verikalitesi.dao;

import com.verikalitesi.core.VeritabaniBaglantiBilgisi;
import com.verikalitesi.dto.KolonProfili;

public interface KolonProfilDao {

    KolonProfili kolonuProfille(VeritabaniBaglantiBilgisi bilgi, String tabloAdi, String kolonAdi);
}
