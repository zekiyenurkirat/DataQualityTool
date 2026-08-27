package com.verikalitesi.dao;

import com.verikalitesi.core.VeriTabaniYardimcisi;
import com.verikalitesi.core.VeritabaniBaglantiBilgisi;
import com.verikalitesi.core.VeritabaniBaglantiHatasi;
import com.verikalitesi.dto.KolonBilgisi;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository   // anatasyon eklenmiş
public class JdbcSemaDao implements SemaDao {

    @Override
    public List<KolonBilgisi> kolonlariGetir(VeritabaniBaglantiBilgisi baglantiBilgisi, String semaAdi, String tabloAdi) throws VeritabaniBaglantiHatasi {

        JdbcTemplate jdbcTemplate = VeriTabaniYardimcisi.getJdbcTemplate(baglantiBilgisi);

        String sql = "SELECT column_name, data_type FROM information_schema.columns WHERE table_schema = ? AND table_name = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            KolonBilgisi kolon = new KolonBilgisi();
            kolon.setKolonAdi(rs.getString("column_name"));
            kolon.setVeriTipi(rs.getString("data_type"));
            return kolon;
        }, semaAdi, tabloAdi);
    }



    @Override
    public List<String> tablolariGetir(VeritabaniBaglantiBilgisi baglantiBilgisi, String semaAdi) throws VeritabaniBaglantiHatasi {

        JdbcTemplate jdbcTemplate = VeriTabaniYardimcisi.getJdbcTemplate(baglantiBilgisi);

        String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("table_name"), semaAdi);
    }


}