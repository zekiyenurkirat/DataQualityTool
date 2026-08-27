package com.verikalitesi.core;

import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Service
public class JdbcVeritabaniBaglantiServisi implements VeritabaniBaglantiServisi {

    private static final String JDBC_URL_SABLONU = "jdbc:postgresql://%s:%d/%s";

    @Override
    public Connection baglan(VeritabaniBaglantiBilgisi bilgi) throws VeritabaniBaglantiHatasi {
        String url = String.format(JDBC_URL_SABLONU, bilgi.getSunucu(), bilgi.getPort(), bilgi.getVeriTabani());

        try {
            return DriverManager.getConnection(url, bilgi.getKullaniciAdi(), bilgi.getSifre());  // buradaki drivemanagerı diyor.
        } catch (SQLException e) {
            throw new VeritabaniBaglantiHatasi(
                    "PostgreSQL bağlantısı kurulamadı. Sunucu/port/veritabanı adı/kullanıcı/şifre bilgilerini kontrol edin. Detay: " + e.getMessage(),
                    e
            );
        }
    }
}
