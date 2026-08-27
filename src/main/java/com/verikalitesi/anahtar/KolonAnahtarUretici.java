package com.verikalitesi.anahtar;

import com.verikalitesi.dto.AlanEslestirmesi;
import com.verikalitesi.dto.SatirVerisi;

import java.util.Optional;

public class KolonAnahtarUretici implements AnahtarUretici {

    @Override
    public Optional<String> uret(SatirVerisi satir, AlanEslestirmesi alanEslestirmesi) {
        String mevcutKimlik = satir.getId();
        if (mevcutKimlik == null || mevcutKimlik.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(mevcutKimlik.trim());
    }
}
