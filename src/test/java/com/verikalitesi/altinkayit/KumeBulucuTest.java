package com.verikalitesi.altinkayit;

import com.verikalitesi.dto.BenzerFirmaCifti;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KumeBulucuTest {

    private BenzerFirmaCifti cift(String a, String b) {
        BenzerFirmaCifti cift = new BenzerFirmaCifti();
        cift.setId_1(a);
        cift.setId_2(b);
        cift.setFirma1("Firma " + a);
        cift.setFirma2("Firma " + b);
        cift.setAlanlar1(alanlar("ad", "Firma " + a));
        cift.setAlanlar2(alanlar("ad", "Firma " + b));
        return cift;
    }

    private Map<String, String> alanlar(String... anahtarDeger) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < anahtarDeger.length; i += 2) {
            map.put(anahtarDeger[i], anahtarDeger[i + 1]);
        }
        return map;
    }

    @Test
    @DisplayName("Tek çift, iki kayıtlık bir küme verir")
    void tekCiftTekKume() {
        List<KopyaKumesi> kumeler = new KumeBulucu().kumeleriBul(List.of(cift("A", "B")));

        assertEquals(1, kumeler.size());
        assertEquals(2, kumeler.get(0).getKayitSayisi());
        assertEquals(List.of("A", "B"), kumeler.get(0).getKimlikler());
    }

    /**
     * Bu testin varlık sebebi: A~B ve B~C ayrı ayrı eşleşme olarak dönüyor ama
     * üçü tek bir firmayı gösteriyor. Çiftleri ikişer birleştirseydik üçlüyü
     * iki ayrı ana kayda bölerdik ve ikinci birleştirmede birinci ana kayıt
     * zaten pasife çekilmiş olurdu.
     */
    @Test
    @DisplayName("A~B ve B~C zinciri tek bir üç kayıtlık küme olur")
    void gecisliEslesmelerTekKumedeToplanir() {
        List<KopyaKumesi> kumeler = new KumeBulucu().kumeleriBul(
                List.of(cift("A", "B"), cift("B", "C")));

        assertEquals(1, kumeler.size());
        assertEquals(3, kumeler.get(0).getKayitSayisi());
        assertEquals(List.of("A", "B", "C"), kumeler.get(0).getKimlikler());
    }

    @Test
    @DisplayName("Dört kayıtlık zincir tek kümede toplanır")
    void dortluZincir() {
        List<KopyaKumesi> kumeler = new KumeBulucu().kumeleriBul(
                List.of(cift("A", "B"), cift("B", "C"), cift("C", "D")));

        assertEquals(1, kumeler.size());
        assertEquals(4, kumeler.get(0).getKayitSayisi());
    }

    @Test
    @DisplayName("Bağlantısız çiftler ayrı kümelerde kalır")
    void baglantisizCiftlerAyrilir() {
        List<KopyaKumesi> kumeler = new KumeBulucu().kumeleriBul(
                List.of(cift("A", "B"), cift("X", "Y")));

        assertEquals(2, kumeler.size());
        assertEquals(2, kumeler.get(0).getKayitSayisi());
        assertEquals(2, kumeler.get(1).getKayitSayisi());
    }

    @Test
    @DisplayName("Aynı çift iki kez gelse küme büyümez")
    void tekrarlananCiftKumeyiBuyutmez() {
        List<KopyaKumesi> kumeler = new KumeBulucu().kumeleriBul(
                List.of(cift("A", "B"), cift("B", "A"), cift("A", "B")));

        assertEquals(1, kumeler.size());
        assertEquals(2, kumeler.get(0).getKayitSayisi());
    }

    /**
     * Determinizm: çiftlerin geliş sırası sorgunun sıralamasına bağlı olabilir.
     * Aynı kümelerin aynı anahtarla çıkması şart, yoksa kullanıcı sayfayı
     * yenilediğinde farklı küme görürdü.
     */
    @Test
    @DisplayName("Çift sırası değişse de aynı küme aynı anahtarla çıkar")
    void ciftSirasiSonucuDegistirmez() {
        String duz = new KumeBulucu().kumeleriBul(
                List.of(cift("A", "B"), cift("B", "C"))).get(0).getAnahtar();
        String ters = new KumeBulucu().kumeleriBul(
                List.of(cift("C", "B"), cift("B", "A"))).get(0).getAnahtar();

        assertEquals(duz, ters);
        assertEquals("A~B~C", duz);
    }

    @Test
    @DisplayName("Büyük kümeler listenin başında gelir")
    void buyukKumelerOnce() {
        List<KopyaKumesi> kumeler = new KumeBulucu().kumeleriBul(
                List.of(cift("X", "Y"), cift("A", "B"), cift("B", "C")));

        assertEquals(3, kumeler.get(0).getKayitSayisi());
        assertEquals(2, kumeler.get(1).getKayitSayisi());
    }

    @Test
    @DisplayName("Ana kayıt kimliği kümeye göre deterministik ve AK- ön ekli")
    void altinKayitKimligiDeterministik() {
        KopyaKumesi bir = new KumeBulucu().kumeleriBul(List.of(cift("A", "B"))).get(0);
        KopyaKumesi iki = new KumeBulucu().kumeleriBul(List.of(cift("B", "A"))).get(0);

        assertEquals(bir.getAltinKayitKimligi(), iki.getAltinKayitKimligi());
        assertTrue(bir.getAltinKayitKimligi().startsWith("AK-"));
    }

    @Test
    @DisplayName("Boş liste boş sonuç verir, çökmez")
    void bosListeCokmez() {
        assertTrue(new KumeBulucu().kumeleriBul(List.of()).isEmpty());
        assertTrue(new KumeBulucu().kumeleriBul(null).isEmpty());
    }

    @Test
    @DisplayName("Kimliği boş olan çift yok sayılır")
    void bosKimlikliCiftAtlanir() {
        BenzerFirmaCifti bozuk = cift("", "B");
        List<KopyaKumesi> kumeler = new KumeBulucu().kumeleriBul(List.of(bozuk, cift("A", "B")));

        assertEquals(1, kumeler.size());
        assertEquals(List.of("A", "B"), kumeler.get(0).getKimlikler());
    }
}
