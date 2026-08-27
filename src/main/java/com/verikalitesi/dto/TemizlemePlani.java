package com.verikalitesi.dto;

import java.util.List;

public class TemizlemePlani {

    // birden fazla kural seçerse

    private List<TemizlemeKurali> temizlemeKurallari;

    public List<TemizlemeKurali> getTemizlemeKurallari() {
        return temizlemeKurallari;
    }

    public void setTemizlemeKurallari(List<TemizlemeKurali> temizlemeKurallari) {
        this.temizlemeKurallari = temizlemeKurallari;
    }
}
