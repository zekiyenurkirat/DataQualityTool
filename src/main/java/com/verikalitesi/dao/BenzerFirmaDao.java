package com.verikalitesi.dao;

import com.verikalitesi.core.VeritabaniBaglantiBilgisi;
import com.verikalitesi.dto.AlanEslestirmesi;
import com.verikalitesi.dto.BenzerFirmaCifti;

import java.util.List;

public interface BenzerFirmaDao {

    List<BenzerFirmaCifti>benzerFirmalariBul(VeritabaniBaglantiBilgisi baglantiBilgisi, String tabloAdi, AlanEslestirmesi alanEslestirmesi, double esikDegeri, List<String> haricTutulacakKelimeler);
}
