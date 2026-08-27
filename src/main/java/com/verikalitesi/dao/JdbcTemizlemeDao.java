package com.verikalitesi.dao;

import com.verikalitesi.core.VeriTabaniYardimcisi;
import com.verikalitesi.core.VeritabaniBaglantiBilgisi;
import com.verikalitesi.dto.KolonTemizlemeSonucu;
import com.verikalitesi.dto.TemizlemeKurali;
import com.verikalitesi.dto.TemizlemeOnizlemeSonucu;
import com.verikalitesi.dto.TemizlemeOrnek;
import com.verikalitesi.dto.TemizlemeSonucu;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class JdbcTemizlemeDao implements TemizlemeDao {

    @Override
    public TemizlemeOnizlemeSonucu metinKolonuOnizle(VeritabaniBaglantiBilgisi bilgi, String tabloAdi,
                                                       String idKolonu, TemizlemeKurali kural, int ornekLimiti) {

        JdbcTemplate jdbcTemplate = VeriTabaniYardimcisi.getJdbcTemplate(bilgi);

        String kolonAdi = kural.getKolonAdi();
        boolean karakterTemizligiVar = karakterTemizligiVarMi(kural);
        String karakterSinifi = karakterTemizligiVar ? karakterSinifiOlustur(kural.getHaricTutulacakKarakter()) : null;
        String ifade = temizIfadeOlustur(kural);

        String sayimSql = "SELECT COUNT(*) FROM " + tabloAdi + " WHERE " + kolonAdi + " IS NOT NULL AND " + kolonAdi + " <> " + ifade;
        boolean idKolonuVar = idKolonu != null && !idKolonu.isBlank();
        String ornekSql = "SELECT " + (idKolonuVar ? idKolonu : "NULL::text") + " AS id, " + kolonAdi + " AS eski, " + ifade + " AS yeni FROM " + tabloAdi +
                " WHERE " + kolonAdi + " IS NOT NULL AND " + kolonAdi + " <> " + ifade +
                (idKolonuVar ? " ORDER BY " + idKolonu : "") + " LIMIT ?";

        return jdbcTemplate.execute((Connection c) -> {
            int toplam;
            try (PreparedStatement ps = c.prepareStatement(sayimSql)) {
                if (karakterTemizligiVar) {
                    ps.setString(1, karakterSinifi);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    toplam = rs.getInt(1);
                }
            }

            List<TemizlemeOrnek> ornekler = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(ornekSql)) {
                int index = 1;
                if (karakterTemizligiVar) {
                    ps.setString(index++, karakterSinifi);
                    ps.setString(index++, karakterSinifi);
                }
                ps.setInt(index, ornekLimiti);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        TemizlemeOrnek ornek = new TemizlemeOrnek();
                        ornek.setId(rs.getString("id"));
                        ornek.setEskiDeger(rs.getString("eski"));
                        ornek.setYeniDeger(rs.getString("yeni"));
                        ornekler.add(ornek);
                    }
                }
            }

            TemizlemeOnizlemeSonucu sonuc = new TemizlemeOnizlemeSonucu();
            sonuc.setKolonAdi(kolonAdi);
            sonuc.setEtkilenecekSatirSayisi(toplam);
            sonuc.setOrnekler(ornekler);
            return sonuc;
        });
    }

    @Override
    public TemizlemeSonucu tumKurallariUygula(VeritabaniBaglantiBilgisi bilgi, String tabloAdi, String idKolonu,
                                              List<TemizlemeKurali> metinKurallari,
                                              Map<String, Map<String, String>> satirGuncellemeleri) {

        JdbcTemplate jdbcTemplate = VeriTabaniYardimcisi.getJdbcTemplate(bilgi);

        return jdbcTemplate.execute((Connection c) -> {
            c.setAutoCommit(false);
            List<KolonTemizlemeSonucu> sonuclar = new ArrayList<>();
            try {
                for (TemizlemeKurali kural : metinKurallari) {
                    String kolonAdi = kural.getKolonAdi();
                    boolean karakterTemizligiVar = karakterTemizligiVarMi(kural);
                    String karakterSinifi = karakterTemizligiVar ? karakterSinifiOlustur(kural.getHaricTutulacakKarakter()) : null;
                    String ifade = temizIfadeOlustur(kural);

                    String sql = "UPDATE " + tabloAdi + " SET " + kolonAdi + " = " + ifade +
                            " WHERE " + kolonAdi + " IS NOT NULL AND " + kolonAdi + " <> " + ifade;

                    try (PreparedStatement ps = c.prepareStatement(sql)) {
                        if (karakterTemizligiVar) {
                            ps.setString(1, karakterSinifi);
                            ps.setString(2, karakterSinifi);
                        }
                        int guncellenen = ps.executeUpdate();

                        KolonTemizlemeSonucu sonuc = new KolonTemizlemeSonucu();
                        sonuc.setKolonAdi(kolonAdi);
                        sonuc.setGuncellenenSatirSayisi(guncellenen);
                        sonuc.setAtlananSatirSayisi(0);
                        sonuclar.add(sonuc);
                    }
                }

                for (Map.Entry<String, Map<String, String>> girdi : satirGuncellemeleri.entrySet()) {
                    String kolonAdi = girdi.getKey();
                    Map<String, String> guncellemeler = girdi.getValue();

                    // Güncellenecek satır yoksa sorgu hiç kurulmaz: idKolonu boş olduğunda
                    // buraya zaten boş bir harita gelir ve "WHERE null = ?" üretilmemiş olur.
                    int guncellenen = 0;
                    if (!guncellemeler.isEmpty()) {
                        String sql = "UPDATE " + tabloAdi + " SET " + kolonAdi + " = ? WHERE " + idKolonu + " = ?";
                        try (PreparedStatement ps = c.prepareStatement(sql)) {
                            for (Map.Entry<String, String> id2Yeni : guncellemeler.entrySet()) {
                                ps.setString(1, id2Yeni.getValue());
                                // Kimlik parametresi TİPSİZ gönderiliyor. setString kullanılsaydı
                                // sürücü parametreyi varchar olarak damgalar ve kimlik kolonu
                                // sayısalsa Postgres "operator does not exist: bigint = character
                                // varying" hatası verir -- veritabanındaki tabloların çoğunda
                                // kimlik bigint. Types.OTHER ile tip belirtilmez, Postgres kolona
                                // bakıp kendisi çıkarır.
                                //
                                // Alternatifi "WHERE kimlik::text = ?" idi; o da çalışıyor ama
                                // kolonu dönüştürdüğü için indeksi devre dışı bırakıyor:
                                // 100.000 satırda Index Scan 0,03 ms iken Seq Scan 11,5 ms.
                                ps.setObject(2, id2Yeni.getKey(), Types.OTHER);
                                ps.addBatch();
                            }
                            guncellenen = etkilenenSatirlariTopla(ps.executeBatch());
                        }
                    }

                    KolonTemizlemeSonucu sonuc = new KolonTemizlemeSonucu();
                    sonuc.setKolonAdi(kolonAdi);
                    sonuc.setGuncellenenSatirSayisi(guncellenen);
                    sonuc.setAtlananSatirSayisi(0);
                    sonuc.setEslesmeyenSatirSayisi(Math.max(0, guncellemeler.size() - guncellenen));
                    sonuclar.add(sonuc);
                }

                c.commit();
            } catch (SQLException hata) {
                c.rollback();
                throw hata;
            } finally {
                c.setAutoCommit(true);
            }

            TemizlemeSonucu sonuc = new TemizlemeSonucu();
            sonuc.setKolonSonuclari(sonuclar);
            return sonuc;
        });
    }

    /**
     * executeBatch her komut için etkilenen satır sayısını döndürür. Sürücü satır sayısını
     * bilmiyorsa SUCCESS_NO_INFO döner; bu durumda komut başarılıdır ve WHERE koşulu tek bir
     * kimliğe baktığı için bir satır sayılır. EXECUTE_FAILED ve diğer negatif değerler sayılmaz.
     */
    private int etkilenenSatirlariTopla(int[] batchSonuclari) {
        int toplam = 0;
        for (int etkilenen : batchSonuclari) {
            if (etkilenen > 0) {
                toplam += etkilenen;
            } else if (etkilenen == Statement.SUCCESS_NO_INFO) {
                toplam++;
            }
        }
        return toplam;
    }

    private boolean karakterTemizligiVarMi(TemizlemeKurali kural) {
        return kural.getHaricTutulacakKarakter() != null && !kural.getHaricTutulacakKarakter().isBlank();
    }

    /**
     * Kuraldan tek bir SQL ifadesi kurar. <b>Sıra önemlidir:</b>
     *
     * <ol>
     *   <li><b>Karakter silme</b> — "a - b" içinden tireyi silmek "a  b" bırakır, yani yeni
     *       ardışık boşluk üretebilir; bu yüzden en başta yapılır.</li>
     *   <li><b>Ardışık boşluk daraltma</b> — bir önceki adımın artığını da toplar.</li>
     *   <li><b>Kırpma</b> — daraltmadan sonra baştaki tek boşluğu da alır.</li>
     *   <li><b>Harf dönüşümü</b> — en sonda, sonuç metnine uygulanır.</li>
     * </ol>
     *
     * <p>Hiçbir adım seçilmemişse kolon adı olduğu gibi döner; karşılaştırma
     * "kolon &lt;&gt; kolon" olacağı için hiçbir satır güncellenmez.
     */
    private String temizIfadeOlustur(TemizlemeKurali kural) {
        String ifade = kural.getKolonAdi();

        if (karakterTemizligiVarMi(kural)) {
            ifade = "regexp_replace(" + ifade + ", ?, '', 'g')";
        }
        if (kural.isArdisikBosluklariDaralt()) {
            ifade = "regexp_replace(" + ifade + ", '\\s+', ' ', 'g')";
        }
        if (kural.isBosluklariKirp()) {
            ifade = "TRIM(" + ifade + ")";
        }
        if (kural.getHarfDonusumu().isUygulanacakMi()) {
            ifade = kural.getHarfDonusumu().getSqlFonksiyonu() + "(" + ifade + ")";
        }
        return ifade;
    }

    private String karakterSinifiOlustur(String haricTutulacakKarakterler) {
        StringBuilder sinif = new StringBuilder("[");
        for (char c : haricTutulacakKarakterler.toCharArray()) {
            if (c == ']' || c == '\\' || c == '^' || c == '-') {
                sinif.append('\\');
            }
            sinif.append(c);
        }
        sinif.append(']');
        return sinif.toString();
    }
}
