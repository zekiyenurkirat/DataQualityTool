package com.verikalitesi.core;

public class VeritabaniBaglantiHatasi extends Exception {
    public VeritabaniBaglantiHatasi(String mesaj, Throwable sebep) {
        super(mesaj, sebep);
    }
}