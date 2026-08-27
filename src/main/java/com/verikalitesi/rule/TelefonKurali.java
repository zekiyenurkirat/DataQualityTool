package com.verikalitesi.rule;

import com.verikalitesi.dto.DuzeltmeEylemi;

import com.verikalitesi.dto.KaliteBoyutuTuru;

import java.util.Optional;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

public class TelefonKurali implements Kural{

// cunstrocter tanımladık hangi ülke kuralına göre yapacağını cunstrocterdan alacak
    private final String ulkeKodu;
    public TelefonKurali (String ulkeKodu){
        this.ulkeKodu = ulkeKodu;
    }

    @Override
    public Optional<DogrulamaSonucu> kontrolEt(String satirNo, String alanAdi, String deger) {

        if (deger == null|| deger.trim().isEmpty()){
            return Optional.empty();
        }

        String temiz = deger.trim();

        if (birdenFazlaNumaraVarMi(temiz)) {
            return sonucOlustur(satirNo, alanAdi, deger, "Hücrede birden fazla telefon numarası var",
                    DuzeltmeEylemi.INCELEME,
                    "İkinci numaranın nereye yazılacağı iş kararı.");
        }

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        try{
            Phonenumber.PhoneNumber numara = phoneUtil.parse(temiz, ulkeKodu);
            if(!phoneUtil.isValidNumber(numara)){
                return sonucOlustur(satirNo, alanAdi, deger, "Geçersiz telefon formatı.",
                        DuzeltmeEylemi.INCELEME,
                        "Numaranın kendisi hatalı; biçim düzeltmesi geçerli hale getirmez.");
            }
        }
        catch (NumberParseException e){
            return sonucOlustur(satirNo, alanAdi, deger, "Telefon numarası ayrıştırılamadı.",
                    DuzeltmeEylemi.INCELEME,
                    "Ülke kodu çözülemedi. Doğru ülke seçiliyse numara gerçekten bozuk demektir.");
        }

        return Optional.empty();
    }

    private boolean birdenFazlaNumaraVarMi(String deger) {
        String[] parcalar = deger.split("[,;/]|\\s+-\\s+");
        int numaraBenzeriSayisi = 0;
        for (String parca : parcalar) {
            if (rakamSayisi(parca) >= 5) {
                numaraBenzeriSayisi++;
            }
        }
        return numaraBenzeriSayisi > 1;
    }

    private int rakamSayisi(String metin) {
        int sayi = 0;
        for (int i = 0; i < metin.length(); i++) {
            if (Character.isDigit(metin.charAt(i))) {
                sayi++;
            }
        }
        return sayi;
    }

    private Optional<DogrulamaSonucu> sonucOlustur(String satirNo, String alanAdi, String deger, String mesaj,
                                                  DuzeltmeEylemi eylem, String eylemNotu) {
        DogrulamaSonucu sonuc = new DogrulamaSonucu();
        sonuc.setSatirNo(satirNo);
        sonuc.setAlanAdi(alanAdi);
        sonuc.setMesaj(mesaj);
        sonuc.setDeger(deger);
        sonuc.setEylem(eylem);
        sonuc.setEylemNotu(eylemNotu);
        return Optional.of(sonuc);
    }

    @Override
    public KaliteBoyutuTuru boyut() {
        return KaliteBoyutuTuru.GECERLILIK;
    }
}
