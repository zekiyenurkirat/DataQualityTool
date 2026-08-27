package com.verikalitesi.dao;

import com.verikalitesi.core.VeriTabaniYardimcisi;
import com.verikalitesi.core.VeritabaniBaglantiBilgisi;
import com.verikalitesi.dto.KolonProfili;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcKolonProfilDao implements KolonProfilDao {

    @Override
    public KolonProfili kolonuProfille(VeritabaniBaglantiBilgisi bilgi, String tabloAdi, String kolonAdi) {

        if (kolonAdi == null || kolonAdi.isBlank()) {
            return null;
        }

        JdbcTemplate jdbcTemplate = VeriTabaniYardimcisi.getJdbcTemplate(bilgi);

        String kolon = tirnakla(kolonAdi);
        String sql = "SELECT COUNT(*) AS toplam, "
                + "COUNT(" + kolon + ") AS dolu, "
                + "COUNT(DISTINCT " + kolon + ") AS tekil "
                + "FROM " + tirnakla(tabloAdi);

        return jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) {
                return new KolonProfili(kolonAdi, 0, 0, 0);
            }
            return new KolonProfili(kolonAdi,
                    rs.getInt("toplam"),
                    rs.getInt("dolu"),
                    rs.getInt("tekil"));
        });
    }

    private String tirnakla(String ad) {
        return "\"" + ad.replace("\"", "\"\"") + "\"";
    }
}
