package com.verikalitesi.rule;

import com.verikalitesi.dto.KimlikNoProfili;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KimlikNoKuraliTest {

    private final KimlikNoKurali kural = new KimlikNoKurali(0.8);

    private List<String> tekrarla(String deger, int adet) {
        List<String> liste = new ArrayList<>();
        for (int i = 0; i < adet; i++) {
            liste.add(deger);
        }
        return liste;
    }

    private List<String> satirNolari(int adet) {
        List<String> liste = new ArrayList<>();
        for (int i = 0; i < adet; i++) {
            liste.add("satir" + i);
        }
        return liste;
    }

    // ---------- sekil cikarma ----------

    @Test
    void ardisikRakamlarTekSembolOlmali() {
        // 9 haneli ile 10 haneli numara ayni kalipta sayilmali; uzunluk ayri bir sinyal.
        List<String> degerler = Arrays.asList("9013065305", "960450042", "10958040498");

        KimlikNoProfili profil = kural.kolonuCozumle("nit", degerler);

        assertEquals("9", profil.getBaskinSekil());
        assertEquals(3, profil.getBaskinSekilAdedi());
    }

    @Test
    void ayraclarSekildeKorunmali() {
        List<String> degerler = tekrarla("99227-3302-OOO", 10);

        KimlikNoProfili profil = kural.kolonuCozumle("registration_number", degerler);

        assertEquals("9-9-A", profil.getBaskinSekil());
    }

    @Test
    void kirilHarflerHarfSayilmali() {
        // a-z kontrolu yapilsaydi Kiril harfleri ayrac sanilirdi.
        List<String> degerler = tekrarla("99227-3302-ООО", 10);

        KimlikNoProfili profil = kural.kolonuCozumle("registration_number", degerler);

        assertEquals("9-9-A", profil.getBaskinSekil());
    }

    // ---------- baskin kalip ve koruma ----------

    @Test
    void gercekVeriKolombiyaNitTekSapmayiBulmali() {
        // 688 satir duz rakam, 1 satir kontrol haneli: 1119182679-3
        List<String> degerler = tekrarla("9013065305", 688);
        degerler.add("1119182679-3");

        KimlikNoProfili profil = kural.kolonuCozumle("nit", degerler);

        assertTrue(profil.isBaskinSekilVarMi());
        assertEquals("9", profil.getBaskinSekil());
        assertEquals(1, profil.getUymayanAdet());
    }

    @Test
    void gercekVeriKirgizistanUcFarkliKalibiAyirmali() {
        List<String> degerler = tekrarla("99227-3302-ООО", 4397);
        degerler.addAll(tekrarla("43509", 95));
        degerler.addAll(tekrarla("226879-3301-Ф-л", 507));

        KimlikNoProfili profil = kural.kolonuCozumle("registration_number", degerler);

        assertEquals("9-9-A", profil.getBaskinSekil());
        assertEquals(602, profil.getUymayanAdet());
        assertEquals(3, profil.getDagilim().size());
    }

    @Test
    void uuidKolonundaHicbirSatirIsaretlenmemeli() {
        // Her UUID farkli sekil uretir; koruma olmadan 856 satirin 856'si da hatali sayilirdi.
        List<String> degerler = Arrays.asList(
                "15ac9ce9-dbdc-4439-9aca-7c4d63f63245",
                "c0fdd379-bb3b-4104-b44c-05efc246214e",
                "9405714d-e0d4-42d2-a7ca-b9ca2836e5e6",
                "1445998e-0e88-4148-83e7-57c2bda3d3a3",
                "24a783bb-ab06-488e-9d20-003597d54737");

        KimlikNoProfili profil = kural.kolonuCozumle("registered_business_id", degerler);

        assertFalse(profil.isBaskinSekilVarMi());
        assertEquals(0, profil.getUymayanAdet());
        assertTrue(kural.kaliptanSapanlariBul(profil, "Kimlik No: ", satirNolari(5), degerler).isEmpty());
    }

    @Test
    void esikDegistiginceBaskinKalipKarariDegismeli() {
        List<String> degerler = tekrarla("12345", 7);
        degerler.addAll(tekrarla("AB-12", 3));   // %70 kapsama

        assertFalse(new KimlikNoKurali(0.8).kolonuCozumle("k", degerler).isBaskinSekilVarMi());
        assertTrue(new KimlikNoKurali(0.6).kolonuCozumle("k", degerler).isBaskinSekilVarMi());
    }

    // ---------- bulgu uretimi ----------

    @Test
    void sapanSatirlarIcinBulguUretilmeli() {
        List<String> degerler = new ArrayList<>(tekrarla("12345", 8));
        degerler.add("12345-6");
        degerler.add("12345-7");

        KimlikNoProfili profil = kural.kolonuCozumle("nit", degerler);
        List<DogrulamaSonucu> bulgular =
                kural.kaliptanSapanlariBul(profil, "Kimlik No: ", satirNolari(10), degerler);

        assertEquals(2, bulgular.size());
        assertEquals("satir8", bulgular.get(0).getSatirNo());
        assertEquals("12345-6", bulgular.get(0).getDeger());
        assertTrue(bulgular.get(0).getMesaj().contains("9-9"));
    }

    @Test
    void bosVeNullDegerlerBulguUretmemeli() {
        List<String> degerler = new ArrayList<>(tekrarla("12345", 8));
        degerler.add(null);
        degerler.add("   ");

        KimlikNoProfili profil = kural.kolonuCozumle("nit", degerler);

        assertEquals(8, profil.getToplamDeger());
        assertTrue(kural.kaliptanSapanlariBul(profil, "Kimlik No: ", satirNolari(10), degerler).isEmpty());
    }

    @Test
    void tamamenBosKolonCokmemeli() {
        KimlikNoProfili profil = kural.kolonuCozumle("nit", Arrays.asList(null, "", "  "));

        assertEquals(0, profil.getToplamDeger());
        assertFalse(profil.isBaskinSekilVarMi());
        assertEquals(0, profil.getEnKisaUzunluk());
    }

    @Test
    void uzunlukDegiskenligiIsaretlenmeli() {
        List<String> degerler = Arrays.asList("12345678", "901306530", "1095804049");

        KimlikNoProfili profil = kural.kolonuCozumle("nit", degerler);

        assertTrue(profil.isUzunlukDegiskenMi());
        assertEquals(8, profil.getEnKisaUzunluk());
        assertEquals(10, profil.getEnUzunUzunluk());
    }
}
