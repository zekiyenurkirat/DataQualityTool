package com.verikalitesi.dto;

public class TemizlemeOrnek {

    // önizleme olarak hangi alanları göstereceğimiz


    private String id;

    private String eskiDeger;

    private String yeniDeger;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEskiDeger() {
        return eskiDeger;
    }

    public void setEskiDeger(String eskiDeger) {
        this.eskiDeger = eskiDeger;
    }

    public String getYeniDeger() {
        return yeniDeger;
    }

    public void setYeniDeger(String yeniDeger) {
        this.yeniDeger = yeniDeger;
    }
}
