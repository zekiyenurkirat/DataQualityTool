package com.verikalitesi.anahtar;

import com.verikalitesi.dto.AlanEslestirmesi;
import com.verikalitesi.dto.SatirVerisi;

import java.util.List;
import java.util.Optional;

public class AnahtarZinciri {

    private final List<AnahtarUretici> ureticiler;

    public AnahtarZinciri(List<AnahtarUretici> ureticiler) {
        this.ureticiler = ureticiler;
    }

    public static AnahtarZinciri varsayilan() {
        return new AnahtarZinciri(List.of(new KolonAnahtarUretici(), new HashAnahtarUretici()));
    }

    public Optional<String> anahtarUret(SatirVerisi satir, AlanEslestirmesi alanEslestirmesi) {
        for (AnahtarUretici uretici : ureticiler) {
            Optional<String> anahtar = uretici.uret(satir, alanEslestirmesi);
            if (anahtar.isPresent()) {
                return anahtar;
            }
        }
        return Optional.empty();
    }
}
