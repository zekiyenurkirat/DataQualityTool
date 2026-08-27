package com.verikalitesi.core;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

public class VeriTabaniYardimcisi {

    public static JdbcTemplate getJdbcTemplate(VeritabaniBaglantiBilgisi baglantiBilgisi) {
        String url = "jdbc:postgresql://" + baglantiBilgisi.getSunucu() + ":" + baglantiBilgisi.getPort() + "/" + baglantiBilgisi.getVeriTabani();

        DataSource dataSource = new DriverManagerDataSource();
        ((DriverManagerDataSource) dataSource).setUrl(url);
        ((DriverManagerDataSource) dataSource).setUsername(baglantiBilgisi.getKullaniciAdi());
        ((DriverManagerDataSource) dataSource).setPassword(baglantiBilgisi.getSifre());

        return new JdbcTemplate(dataSource);
    }
}
