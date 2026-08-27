package com.verikalitesi.core;

import java.sql.Connection;

public interface VeritabaniBaglantiServisi {
    Connection baglan(VeritabaniBaglantiBilgisi bilgi) throws VeritabaniBaglantiHatasi;


}
