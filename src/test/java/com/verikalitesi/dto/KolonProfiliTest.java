package com.verikalitesi.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KolonProfiliTest {

    @Test
    void yuzdelerDogruHesaplanmali() {
        KolonProfili profil = new KolonProfili("nit", 5000, 689, 689);

        assertEquals(13.78, profil.getDolulukYuzdesi());
        assertEquals(100.0, profil.getTekillikYuzdesi());
        assertEquals(4311, profil.getBosSatir());
    }

    @Test
    void bosTabloSifiraBolmeHatasiVermemeli() {
        KolonProfili profil = new KolonProfili("kolon", 0, 0, 0);

        assertEquals(0.0, profil.getDolulukYuzdesi());
        assertEquals(0.0, profil.getTekillikYuzdesi());
        assertTrue(profil.isBos());
        assertFalse(profil.isTekil());
        assertFalse(profil.isTamamenDolu());
    }

    @Test
    void tamamenBosKolonSifiraBolmeHatasiVermemeli() {
        KolonProfili profil = new KolonProfili("id_number", 5000, 0, 0);

        assertEquals(0.0, profil.getDolulukYuzdesi());
        assertEquals(0.0, profil.getTekillikYuzdesi());
        assertTrue(profil.isBos());
        assertFalse(profil.isTekil());
    }

    @Test
    void tekilOlmayanKolonTespitEdilmeli() {
        KolonProfili profil = new KolonProfili("establishment_year", 5000, 5000, 87);

        assertTrue(profil.isTamamenDolu());
        assertFalse(profil.isTekil());
        assertEquals(1.74, profil.getTekillikYuzdesi());
    }

    @Test
    void mukemmelKimlikKolonuTespitEdilmeli() {
        KolonProfili profil = new KolonProfili("id", 5000, 5000, 5000);

        assertTrue(profil.isTekil());
        assertTrue(profil.isTamamenDolu());
        assertFalse(profil.isBos());
        assertEquals(100.0, profil.getDolulukYuzdesi());
    }

    @Test
    void kismenDoluAmaTekilKolonTespitEdilmeli() {
        KolonProfili profil = new KolonProfili("nit", 5000, 689, 689);

        assertTrue(profil.isTekil());
        assertFalse(profil.isTamamenDolu());
        assertFalse(profil.isBos());
    }
}
