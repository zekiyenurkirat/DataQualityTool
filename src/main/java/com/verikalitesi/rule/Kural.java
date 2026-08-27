package com.verikalitesi.rule;

import com.verikalitesi.dto.KaliteBoyutuTuru;

import java.util.Optional;

public interface Kural {


    Optional<DogrulamaSonucu> kontrolEt(String satirNo, String alanAdi,String deger);

    /**
     * Kuralın hangi veri kalitesi boyutunu ölçtüğü. Skor kartı, hangi bulgunun hangi başlık
     * altında sayılacağını buradan öğrenir; kural eklendiğinde boyutunu da söylemek zorunda
     * kalır, böylece hiçbir bulgu sınıflandırılmadan kalmaz.
     */
    KaliteBoyutuTuru boyut();
}
