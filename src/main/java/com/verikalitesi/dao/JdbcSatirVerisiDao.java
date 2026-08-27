package com.verikalitesi.dao;


import com.verikalitesi.core.VeriTabaniYardimcisi;
import com.verikalitesi.core.VeritabaniBaglantiBilgisi;
import com.verikalitesi.dto.SatirVerisi;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Repository
public class JdbcSatirVerisiDao implements SatirVerisiDao{

    @Override
    public List<SatirVerisi> satirlariGetir(VeritabaniBaglantiBilgisi bilgi, String tabloAdi, List<String> kolonlar,String idKolonu) {  // kolonlar için ayrı dto (tür oluşturmaya gerek yok çünkü string türü işimizi görüyor (yazı gibi bişey tutucaz ya zaten)

        JdbcTemplate jdbcTemplate = VeriTabaniYardimcisi.getJdbcTemplate(bilgi);

        boolean idKolonuVar = idKolonu != null && !idKolonu.isBlank();

        StringBuilder secilenler = new StringBuilder();
        secilenler.append(idKolonuVar ? idKolonu : "NULL::text").append(" AS id");
        for (String kolon : kolonlar) {
            secilenler.append(", ").append(kolon);
        }

        String sql = "SELECT " + secilenler + " FROM " + tabloAdi
                + (idKolonuVar ? " ORDER BY id" : "");  //satırdaki kolon  bilgisini getirmesi için sql sorgusu küçükten büyüğe satır numaralarını sıralar

        return jdbcTemplate.query(sql, (rs, rowNum) -> {   // eşleştirme
            SatirVerisi satir = new SatirVerisi();
            satir.setId(rs.getString("id"));

            Map<String, String> alanlar = new HashMap<>();
            for (String kolon : kolonlar) {
                alanlar.put(kolon, rs.getString(kolon));
            }
            satir.setAlanlar(alanlar);

            return satir;
        });


    }



}
