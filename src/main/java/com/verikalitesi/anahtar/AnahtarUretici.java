package com.verikalitesi.anahtar;

import com.verikalitesi.dto.AlanEslestirmesi;
import com.verikalitesi.dto.SatirVerisi;

import java.util.Optional;

public interface AnahtarUretici {

    Optional<String> uret(SatirVerisi satir, AlanEslestirmesi alanEslestirmesi);
}
