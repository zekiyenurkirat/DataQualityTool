package com.verikalitesi.dto;

import java.util.Map;

public class SatirVerisi {

    private String id;
    private Map<String, String> alanlar;  //kolon adları ve o satırdaki değer

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getAlanlar() {
        return alanlar;
    }

    public void setAlanlar(Map<String, String> alanlar) {
        this.alanlar = alanlar;
    }
}
