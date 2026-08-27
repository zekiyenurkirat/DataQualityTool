package com.verikalitesi.dao;

import com.verikalitesi.anahtar.AnahtarZinciri;
import com.verikalitesi.core.VeriTabaniYardimcisi;
import com.verikalitesi.core.VeritabaniBaglantiBilgisi;
import com.verikalitesi.dto.AlanEslestirmesi;
import com.verikalitesi.dto.BenzerFirmaCifti;
import com.verikalitesi.dto.SatirVerisi;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


@Repository
public class JdbcBenzerFirmaDao implements BenzerFirmaDao {

    private static final int POSTGRES_ISIM_SINIRI = 63;
    private static final int IMZA_UZUNLUGU = 8;
    private static final String ON_EK = "idx_";
    private static final String SON_EK = "_trgm";
    private static final String REGEX_OZEL_KARAKTERLER = "\\.^$*+?()[]{}|";

    private final AnahtarZinciri anahtarZinciri = AnahtarZinciri.varsayilan();

    @Override
    public List<BenzerFirmaCifti> benzerFirmalariBul(VeritabaniBaglantiBilgisi baglantiBilgisi, String tabloAdi, AlanEslestirmesi alanEslestirmesi, double esikDegeri, List<String> haricTutulacakKelimeler) {

        JdbcTemplate jdbcTemplate = VeriTabaniYardimcisi.getJdbcTemplate(baglantiBilgisi);

        String kolonAdi = alanEslestirmesi.getFirmaAdiKolonu();
        String idKolonu = alanEslestirmesi.getIdKolonu();
        List<String> okunacakKolonlar = okunacakKolonlariTopla(alanEslestirmesi);

        String kelimeDeseni = null;
        if (haricTutulacakKelimeler != null && !haricTutulacakKelimeler.isEmpty()) {
            List<String> siraliKelimeler = new ArrayList<>(haricTutulacakKelimeler);
            Collections.sort(siraliKelimeler);
            List<String> kacisliKelimeler = new ArrayList<>();
            for (String kelime : siraliKelimeler) {
                kacisliKelimeler.add(regexKacisla(kelime));
            }
            // \y = kelime sınırı. Parantez şart: "\yA|B\y" deseni "(\yA)|(B\y)" diye okunur,
            // yani sınır yalnızca ilk ve son seçeneğe uygulanır. Sınır olmadan kalıp kelime
            // başka bir kelimenin içinden de silinir: "والمحدودة" -> "و".
            kelimeDeseni = sqlMetniKacisla("\\y(" + String.join("|", kacisliKelimeler) + ")\\y");
        }

        String temizA = kelimeDeseni == null ? "a." + kolonAdi : "regexp_replace(a." + kolonAdi + ", '" + kelimeDeseni + "', '', 'gi')";
        String temizB = kelimeDeseni == null ? "b." + kolonAdi : "regexp_replace(b." + kolonAdi + ", '" + kelimeDeseni + "', '', 'gi')";
        String indexIfadesi = kelimeDeseni == null ? kolonAdi : "regexp_replace(" + kolonAdi + ", '" + kelimeDeseni + "', '', 'gi')";
        String indexAdi = indexAdiUret(tabloAdi, kolonAdi, indexIfadesi);

        boolean idKolonuVar = idKolonu != null && !idKolonu.isBlank();

        StringBuilder secilenler = new StringBuilder();
        secilenler.append(idKolonuVar ? "a." + idKolonu : "NULL::text").append(" AS id_a, ");
        secilenler.append(idKolonuVar ? "b." + idKolonu : "NULL::text").append(" AS id_b");
        for (int i = 0; i < okunacakKolonlar.size(); i++) {
            secilenler.append(", a.").append(okunacakKolonlar.get(i)).append(" AS a_").append(i);
            secilenler.append(", b.").append(okunacakKolonlar.get(i)).append(" AS b_").append(i);
        }

        // Adres puanı yalnızca SELECT'e ekleniyor, JOIN'e ASLA girmiyor. JOIN koşulu firma adı
        // üzerindeki GIN indeksiyle eşleşmek zorunda; oraya ikinci bir karşılaştırma koymak
        // indeksi devre dışı bırakır ve sorgu O(n²)'ye döner. Adres, aday çifti bulunduktan
        // sonra o çifti değerlendirmek için kullanılıyor.
        List<String> adresKolonlari = alanEslestirmesi.getSiraliAdresKolonlari();
        boolean adresVar = !adresKolonlari.isEmpty();
        String adresPuani = adresVar
                ? "similarity(" + adresIfadesi("a", adresKolonlari) + ", "
                                + adresIfadesi("b", adresKolonlari) + ")"
                : "NULL::real";

        String sql = "SELECT " + secilenler + ", " +
                "similarity(" + temizA + ", " + temizB + ") AS benzerlik_orani, " +
                adresPuani + " AS adres_benzerligi " +
                "FROM " + tabloAdi + " a " +
                "JOIN " + tabloAdi + " b ON " + temizA + " % " + temizB + " AND a.ctid < b.ctid " +
                // İkincil sıralama şart: kalıp kelimeler elendikten sonra çiftlerin çoğu 1.0 ad
                // puanı alıyor, o yüzden tek başına ad puanına göre sıralamak listeyi rastgele
                // bırakıyor. Adres puanı eşitliği bozarak hem adı hem adresi tutan çiftleri
                // en üste taşıyor -- incelenmesi gereken asıl adaylar onlar.
                "ORDER BY benzerlik_orani DESC, adres_benzerligi DESC NULLS LAST";

        return jdbcTemplate.execute((Connection connection) -> {
            try (Statement esikStatement = connection.createStatement()) {
                esikStatement.execute("SET pg_trgm.similarity_threshold = " + esikDegeri);
            }

            try (Statement indexStatement = connection.createStatement()) {
                String indexSql = "CREATE INDEX IF NOT EXISTS " + indexAdi + " " +
                        "ON " + tabloAdi + " USING gin ((" + indexIfadesi + ") gin_trgm_ops)";
                indexStatement.execute(indexSql);
            }

            List<BenzerFirmaCifti> sonuc = new ArrayList<>();
            try (PreparedStatement ps = connection.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SatirVerisi satirA = satirOku(rs, "id_a", "a_", okunacakKolonlar);
                    SatirVerisi satirB = satirOku(rs, "id_b", "b_", okunacakKolonlar);

                    BenzerFirmaCifti cift = new BenzerFirmaCifti();
                    cift.setId_1(anahtarZinciri.anahtarUret(satirA, alanEslestirmesi).orElse(""));
                    cift.setId_2(anahtarZinciri.anahtarUret(satirB, alanEslestirmesi).orElse(""));
                    cift.setFirma1(satirA.getAlanlar().get(kolonAdi));
                    cift.setFirma2(satirB.getAlanlar().get(kolonAdi));
                    // Alan haritalari zaten okunmustu; ana kayit secimi doluluk saymak
                    // zorunda oldugu icin atilmak yerine cifte tasiniyor. Ek sorgu yok.
                    cift.setAlanlar1(satirA.getAlanlar());
                    cift.setAlanlar2(satirB.getAlanlar());
                    cift.setBenzerlikOrani(rs.getDouble("benzerlik_orani"));

                    if (adresVar) {
                        cift.setAdres1(adresiGoster(satirA.getAlanlar(), adresKolonlari));
                        cift.setAdres2(adresiGoster(satirB.getAlanlar(), adresKolonlari));
                        double adresPuaniDegeri = rs.getDouble("adres_benzerligi");
                        // wasNull(): SQL NULL, getDouble'dan 0.0 olarak döner. Ayırmazsak
                        // "adres yok" ile "adresler hiç benzemiyor" aynı görünürdü.
                        cift.setAdresBenzerligi(rs.wasNull() ? -1 : adresPuaniDegeri);
                    }

                    sonuc.add(cift);
                }
            }
            return sonuc;
        });
    }

    /**
     * Kelimeyi düzenli ifade deseninin içine güvenle koyar. Kaçış yapılmazsa kelimedeki
     * özel karakterler desen olarak yorumlanır: "b.v" içindeki nokta "herhangi bir karakter"
     * anlamına gelir ve "bav", "b7v" gibi ilgisiz metinler de silinir.
     */
    private String regexKacisla(String kelime) {
        StringBuilder kacisli = new StringBuilder();
        for (char karakter : kelime.toCharArray()) {
            if (REGEX_OZEL_KARAKTERLER.indexOf(karakter) >= 0) {
                kacisli.append('\\');
            }
            kacisli.append(karakter);
        }
        return kacisli.toString();
    }

    /** Deseni SQL metin sabitinin içine koyabilmek için tek tırnakları ikiler. */
    private String sqlMetniKacisla(String metin) {
        return metin.replace("'", "''");
    }

    /**
     * PostgreSQL nesne adlarını 63 bayta kadar kabul eder, uzun olanı hata vermeden keser.
     * Kesilen kısım isim sonundaki imza olursa iki farklı ifade aynı ada sahip olur;
     * CREATE INDEX IF NOT EXISTS ikincisini atlar ve sorgu, ifadesi tutmayan bir indeksle
     * karşılaşıp indeksi hiç kullanamaz. Bu yüzden imzaya yer ayrılır, kesme yalnızca
     * okunurluk için tutulan kısımda yapılır.
     */
    private String indexAdiUret(String tabloAdi, String kolonAdi, String indexIfadesi) {
        String imza = String.format("%08x", (tabloAdi + "|" + indexIfadesi).hashCode());
        int sabitUzunluk = ON_EK.length() + 1 + IMZA_UZUNLUGU + SON_EK.length();
        String okunur = adiSadelestir(tabloAdi + "_" + kolonAdi);
        int okunurPay = POSTGRES_ISIM_SINIRI - sabitUzunluk;
        if (okunur.length() > okunurPay) {
            okunur = okunur.substring(0, okunurPay);
        }
        return ON_EK + okunur + "_" + imza + SON_EK;
    }

    /**
     * Adı ASCII harf ve rakama indirger. Türkçe karakterler UTF-8'de iki bayt tuttuğu için
     * karakter sayısı ile bayt sayısı ayrışır; sadeleştirme ikisini eşitleyerek 63 baytlık
     * sınırın karakter sayarak güvenle hesaplanmasını sağlar.
     */
    private String adiSadelestir(String ad) {
        StringBuilder sade = new StringBuilder();
        for (char karakter : ad.toLowerCase(Locale.ROOT).toCharArray()) {
            boolean kabulEdilir = (karakter >= 'a' && karakter <= 'z') || (karakter >= '0' && karakter <= '9');
            sade.append(kabulEdilir ? karakter : '_');
        }
        return sade.toString();
    }

    private List<String> okunacakKolonlariTopla(AlanEslestirmesi alanEslestirmesi) {
        List<String> kolonlar = new ArrayList<>();
        ekleVarsa(kolonlar, alanEslestirmesi.getFirmaAdiKolonu());
        for (String adresKolonu : alanEslestirmesi.getSiraliAdresKolonlari()) {
            ekleVarsa(kolonlar, adresKolonu);
        }
        ekleVarsa(kolonlar, alanEslestirmesi.getTelefonKolonu());
        ekleVarsa(kolonlar, alanEslestirmesi.getePostaKolonu());
        ekleVarsa(kolonlar, alanEslestirmesi.getWebSitesiKolonu());
        return kolonlar;
    }

    /**
     * Adres kolonlarını tek bir SQL ifadesinde birleştirir. Boş kolonlar {@code COALESCE}
     * ile boş metne çevrilir; aksi halde bir kolon NULL olduğunda tüm birleşim NULL olur ve
     * kısmen dolu adresler hiç karşılaştırılamazdı.
     */
    String adresIfadesi(String takmaAd, List<String> adresKolonlari) {
        StringBuilder ifade = new StringBuilder();
        for (String kolon : adresKolonlari) {
            if (ifade.length() > 0) {
                ifade.append(" || ' ' || ");
            }
            // ::text ZORUNLU. Adres parçaları her zaman metin değil: kapı numarası,
            // daire numarası gibi kolonlar sayısal tipte saklanmış olabiliyor
            // (pandas boş değer içeren sayısal kolonu float'a çeviriyor). Cast
            // olmadan COALESCE(a.kapi_no, '') yazıldığında Postgres boş metni
            // sayıya çevirmeye çalışıyor ve sorgu
            // "invalid input syntax for type bigint" ile düşüyor.
            ifade.append("COALESCE(").append(takmaAd).append('.').append(kolon).append("::text, '')");
        }
        // NULLIF: birleşim tamamen boşsa NULL'a çevrilir. similarity NULL girdide NULL döner,
        // biz de bunu "—" olarak gösteririz. Olmasaydı similarity('', 'Bağdat') = 0 çıkar ve
        // ekranda "hiç benzemiyor" gibi okunurdu; oysa doğru bilgi "adres yok, bilmiyoruz".
        return "NULLIF(TRIM(" + ifade + "), '')";
    }

    /** Raporda gösterilecek okunur adres; boş parçalar atlanır. */
    private String adresiGoster(Map<String, String> alanlar, List<String> adresKolonlari) {
        StringBuilder gosterim = new StringBuilder();
        for (String kolon : adresKolonlari) {
            String deger = alanlar.get(kolon);
            if (deger == null || deger.isBlank()) {
                continue;
            }
            if (gosterim.length() > 0) {
                gosterim.append(" · ");
            }
            gosterim.append(deger.trim());
        }
        return gosterim.length() == 0 ? null : gosterim.toString();
    }

    private void ekleVarsa(List<String> kolonlar, String kolonAdi) {
        if (kolonAdi != null && !kolonAdi.isBlank() && !kolonlar.contains(kolonAdi)) {
            kolonlar.add(kolonAdi);
        }
    }

    private SatirVerisi satirOku(ResultSet rs, String idTakmaAdi, String onEk, List<String> kolonlar) throws java.sql.SQLException {
        SatirVerisi satir = new SatirVerisi();
        satir.setId(rs.getString(idTakmaAdi));
        Map<String, String> alanlar = new HashMap<>();
        for (int i = 0; i < kolonlar.size(); i++) {
            alanlar.put(kolonlar.get(i), rs.getString(onEk + i));
        }
        satir.setAlanlar(alanlar);
        return satir;
    }
}
