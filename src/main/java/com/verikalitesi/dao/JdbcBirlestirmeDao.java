package com.verikalitesi.dao;

import com.verikalitesi.core.VeriTabaniYardimcisi;
import com.verikalitesi.core.VeritabaniBaglantiBilgisi;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Birleştirme kararlarını kalıcı hale getirir.
 *
 * <p><strong>Mimari karar — kaynak tabloya dokunulmuyor.</strong> Kurumsal
 * araçların yaptığı gibi kaydı fiziksel olarak silmiyoruz (hard delete yok),
 * ama kaynak tabloya {@code is_master} / {@code master_id} kolonları da
 * <strong>eklemiyoruz.</strong> Karar bir <em>çapraz referans tablosunda</em>
 * (cross-reference / XREF) tutuluyor. Gerekçesi:
 *
 * <ul>
 *   <li>Bir doğrulama aracı kullanıcının iş tablosunun <strong>yapısını</strong>
 *       değiştirmemeli. Kimlik üretme yöntemi de aynı gerekçeyle seçilmiştir:
 *       {@code ALTER TABLE ... ADD COLUMN} yaklaşımı kapsam dışı bırakıldı.</li>
 *   <li>Araç salt okunur bir hesapla bağlanmış olabilir; o durumda kaynak
 *       tabloya yazan bir tasarım hiç çalışmaz.</li>
 *   <li>Kararın kendisi bir <strong>veri kalitesi çıktısıdır</strong>, iş
 *       verisinin parçası değil. Ayrı durması, kaynak tablo yeniden
 *       kazındığında (scrape) kararların kaybolmamasını da sağlar.</li>
 * </ul>
 *
 * <p>Informatica MDM de aynı deseni kullanır: ana kayıtlar bir nesnede,
 * hangi kaynak kaydın hangi ana kayda bağlandığı ayrı bir XREF tablosunda durur.
 *
 * <p><strong>Bilinen sınır:</strong> kimlik araç tarafından içerikten
 * üretilmişse (tabloda ID kolonu yoksa) bu tablo kaynak satıra SQL ile
 * bağlanamaz, çünkü o kimlik veritabanında hiçbir yerde yazılı değil. Kayıt
 * yine tutulur ve denetim izi görevini görür, ama {@code JOIN} yapılamaz.
 */
@Repository
public class JdbcBirlestirmeDao implements BirlestirmeDao {

    /** Aracın kendi tablosu; adı ön ekle işaretli ki iş tablolarıyla karışmasın. */
    static final String TABLO = "veri_kalitesi_birlestirme";

    private static final String OLUSTUR = """
            CREATE TABLE IF NOT EXISTS %s (
                id             bigserial PRIMARY KEY,
                tablo_adi      text        NOT NULL,
                kayit_kimligi  text        NOT NULL,
                altin_kayit_id text        NOT NULL,
                is_master      boolean     NOT NULL,
                gerekce        text,
                karar_zamani   timestamptz NOT NULL DEFAULT now(),
                CONSTRAINT uq_vk_birlestirme UNIQUE (tablo_adi, kayit_kimligi)
            )""".formatted(TABLO);

    /*
     * ON CONFLICT ... DO UPDATE: aynı kayıt için ikinci bir karar verilirse
     * hata fırlatmak yerine kararı güncelliyoruz. Kullanıcı yanlış tıklayıp
     * geri dönebilmeli; çakışmada hata fırlatmak kullanıcıya bir şey kazandırmaz.
     * karar_zamani da yenileniyor, yani denetim izi son kararı gösteriyor.
     */
    private static final String YAZ = """
            INSERT INTO %s (tablo_adi, kayit_kimligi, altin_kayit_id, is_master, gerekce)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT ON CONSTRAINT uq_vk_birlestirme DO UPDATE SET
                altin_kayit_id = EXCLUDED.altin_kayit_id,
                is_master      = EXCLUDED.is_master,
                gerekce        = EXCLUDED.gerekce,
                karar_zamani   = now()""".formatted(TABLO);

    /** Sentezlenen ana kaydın alanlarını tutan tablo; kaynak tabloya hiç dokunulmuyor. */
    static final String ALTIN_TABLO = "veri_kalitesi_altin_kayit";

    private static final String ALTIN_OLUSTUR = """
            CREATE TABLE IF NOT EXISTS %s (
                id             bigserial PRIMARY KEY,
                tablo_adi      text        NOT NULL,
                altin_kayit_id text        NOT NULL,
                kolon_adi      text        NOT NULL,
                deger          text,
                kaynak_kimlik  text,
                olusturma      timestamptz NOT NULL DEFAULT now(),
                CONSTRAINT uq_vk_altin UNIQUE (tablo_adi, altin_kayit_id, kolon_adi)
            )""".formatted(ALTIN_TABLO);

    private static final String ALTIN_YAZ = """
            INSERT INTO %s (tablo_adi, altin_kayit_id, kolon_adi, deger, kaynak_kimlik)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT ON CONSTRAINT uq_vk_altin DO UPDATE SET
                deger         = EXCLUDED.deger,
                kaynak_kimlik = EXCLUDED.kaynak_kimlik,
                olusturma     = now()""".formatted(ALTIN_TABLO);

    @Override
    public int altinKaydiSentezle(VeritabaniBaglantiBilgisi bilgi, String tabloAdi,
                                  String altinKayitId, java.util.Map<String, String> degerler,
                                  java.util.Map<String, String> kaynaklar,
                                  java.util.List<String> uyeKimlikler, String gerekce) {

        JdbcTemplate jdbcTemplate = VeriTabaniYardimcisi.getJdbcTemplate(bilgi);

        return jdbcTemplate.execute((Connection c) -> {
            boolean eskiOtomatikOnay = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                try (Statement st = c.createStatement()) {
                    st.execute(OLUSTUR);
                    st.execute(ALTIN_OLUSTUR);
                }

                int yazilan = 0;

                // 1) Sentezlenen ana kaydın alanları. Her satır değerin hangi
                //    kaynak kayıttan geldiğini de taşıyor -- alan seviyesinde soyağacı.
                try (PreparedStatement ps = c.prepareStatement(ALTIN_YAZ)) {
                    for (java.util.Map.Entry<String, String> alan : degerler.entrySet()) {
                        ps.setString(1, tabloAdi);
                        ps.setString(2, altinKayitId);
                        ps.setString(3, alan.getKey());
                        ps.setString(4, alan.getValue());
                        ps.setString(5, kaynaklar.get(alan.getKey()));
                        ps.addBatch();
                    }
                    yazilan += etkilenenSatirlariTopla(ps.executeBatch());
                }

                // 2) Ana kayıt kendini işaretliyor, kümedeki bütün kayıtlar ona bağlanıyor.
                //    Kaynak satırların hiçbiri silinmiyor.
                try (PreparedStatement ps = c.prepareStatement(YAZ)) {
                    satirEkle(ps, tabloAdi, altinKayitId, altinKayitId, true, gerekce);
                    for (String uye : uyeKimlikler) {
                        satirEkle(ps, tabloAdi, uye, altinKayitId, false, gerekce);
                    }
                    yazilan += etkilenenSatirlariTopla(ps.executeBatch());
                }

                c.commit();
                return yazilan;
            } catch (RuntimeException | java.sql.SQLException hata) {
                c.rollback();
                throw hata;
            } finally {
                c.setAutoCommit(eskiOtomatikOnay);
            }
        });
    }

    @Override
    public int birlestirmeyiKaydet(VeritabaniBaglantiBilgisi bilgi, String tabloAdi,
                                   String altinKayitKimligi, String pasifKayitKimligi,
                                   String gerekce) {

        JdbcTemplate jdbcTemplate = VeriTabaniYardimcisi.getJdbcTemplate(bilgi);

        return jdbcTemplate.execute((Connection c) -> {
            boolean eskiOtomatikOnay = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                try (Statement st = c.createStatement()) {
                    st.execute(OLUSTUR);
                }

                int yazilan;
                try (PreparedStatement ps = c.prepareStatement(YAZ)) {
                    // Kazanan: kendi kendisinin ana kaydı.
                    satirEkle(ps, tabloAdi, altinKayitKimligi, altinKayitKimligi, true, gerekce);
                    // Kaybeden: pasife çekiliyor ve kazanana bağlanıyor.
                    satirEkle(ps, tabloAdi, pasifKayitKimligi, altinKayitKimligi, false, gerekce);
                    int[] sonuclar = ps.executeBatch();
                    yazilan = etkilenenSatirlariTopla(sonuclar);
                }

                c.commit();
                return yazilan;
            } catch (RuntimeException | java.sql.SQLException hata) {
                c.rollback();
                throw hata;
            } finally {
                // Bağlantı havuza geri dönüyor; ayarı bıraktığımız gibi bırakmazsak
                // sonraki kullanan otomatik onayın kapalı olduğunu bilmez.
                c.setAutoCommit(eskiOtomatikOnay);
            }
        });
    }

    private void satirEkle(PreparedStatement ps, String tabloAdi, String kimlik,
                           String altinKayitId, boolean anaKayitMi, String gerekce)
            throws java.sql.SQLException {
        ps.setString(1, tabloAdi);
        ps.setString(2, kimlik);
        ps.setString(3, altinKayitId);
        ps.setBoolean(4, anaKayitMi);
        ps.setString(5, gerekce);
        ps.addBatch();
    }

    /**
     * Toplu yazımın gerçekten kaç satırı etkilediğini sayar.
     *
     * <p>Niyeti değil sonucu okuyoruz -- temizleme modülünde bunu okumamak
     * ekranda gerçek olmayan bir sayı gösterilmesine yol açmıştı.
     * {@code SUCCESS_NO_INFO} sürücünün "oldu ama kaç satır bilmiyorum"
     * demesidir; tek satır hedefleyen bir komutta bu 1 sayılır.
     */
    private int etkilenenSatirlariTopla(int[] sonuclar) {
        int toplam = 0;
        for (int sonuc : sonuclar) {
            if (sonuc > 0) {
                toplam += sonuc;
            } else if (sonuc == Statement.SUCCESS_NO_INFO) {
                toplam += 1;
            }
        }
        return toplam;
    }
}
