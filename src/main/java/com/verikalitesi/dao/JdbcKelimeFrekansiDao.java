package com.verikalitesi.dao;

import com.verikalitesi.core.VeriTabaniYardimcisi;
import com.verikalitesi.core.VeritabaniBaglantiBilgisi;
import com.verikalitesi.core.VeritabaniBaglantiHatasi;
import com.verikalitesi.dto.KelimeFrekansi;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public class JdbcKelimeFrekansiDao  implements KelimeFrekansiDao{
    @Override
    public List<KelimeFrekansi> kelimeleriGetir(VeritabaniBaglantiBilgisi bilgi, String tabloAdi, String kolonAdi) throws VeritabaniBaglantiHatasi {

        JdbcTemplate jdbcTemplate = VeriTabaniYardimcisi.getJdbcTemplate(bilgi);

        String sql = "SELECT kelime, COUNT(*) AS tekrar_sayisi " +
                "FROM (SELECT LOWER(regexp_replace(regexp_split_to_table(" + kolonAdi + ", '\\s+'), '^[^[:alpha:]]+|[^[:alpha:]]+$', '', 'g')) AS kelime FROM " + tabloAdi + ") t " +
                "WHERE LENGTH(kelime) > 2 " +
                "GROUP BY kelime " +
                "ORDER BY tekrar_sayisi DESC " +
                "LIMIT 15";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            KelimeFrekansi kf = new KelimeFrekansi();
            kf.setKelime(rs.getString("kelime"));
            kf.setTekrarSayisi(rs.getInt("tekrar_sayisi"));
            return kf;
        });


    }

    @Override
    public int toplamFirmaSayisiniGetir(VeritabaniBaglantiBilgisi bilgi, String tabloAdi, String kolonAdi) throws VeritabaniBaglantiHatasi {

        JdbcTemplate jdbcTemplate = VeriTabaniYardimcisi.getJdbcTemplate(bilgi);

        String sql = "SELECT COUNT(*) FROM " + tabloAdi + " WHERE " + kolonAdi + " IS NOT NULL AND " + kolonAdi + " <> ''";

        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
}
