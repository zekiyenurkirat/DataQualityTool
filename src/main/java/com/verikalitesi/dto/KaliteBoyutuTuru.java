package com.verikalitesi.dto;

/**
 * DAMA'nın veri kalitesi boyutları.
 *
 * <p>Her kural tam olarak <b>bir</b> boyuta bağlıdır. Aynı bulguyu iki boyutta saymak skoru
 * şişirir ve boyutlar arası karşılaştırmayı anlamsızlaştırır; bu yüzden eşleme birebirdir.
 *
 * <p>{@code DOGRULUK} ve {@code GUNCELLIK} bu araçta ölçülemiyor. Doğruluk, verinin gerçek
 * dünyayla uyumu demektir ve karşılaştırılacak bir referans kaynak ister. Güncellik ise
 * verinin ne zaman toplandığını bilmeyi gerektirir. İkisi de elimizdeki tablolardan
 * çıkarılamaz; ölçülemeyeni tahmin etmek yerine ölçülemediğini yazıyoruz.
 */
public enum KaliteBoyutuTuru {

    TAMLIK("Tamlık", "Completeness",
            "Gerekli değerlerin ne kadarının dolu olduğu. Yer tutucu metinler (\"n/a\", \"yok\") "
                    + "dolu görünse de bilgi taşımadığı için eksik sayılır.", true),

    GECERLILIK("Geçerlilik", "Validity",
            "Değerlerin kendi alanının kurallarına uyup uymadığı: e-posta biçimi, telefon "
                    + "numarası yapısı, web adresi biçimi.", true),

    TEKLIK("Teklik", "Uniqueness",
            "Aynı kaydın birden fazla kez bulunup bulunmadığı.", true),

    TUTARLILIK("Tutarlılık", "Consistency",
            "Aynı kolondaki değerlerin birbiriyle uyumlu yazılıp yazılmadığı: kimlik numarası "
                    + "kalıbı, gereksiz boşluklar.", true),

    DOGRULUK("Doğruluk", "Accuracy",
            "Verinin gerçek dünyadaki karşılığıyla uyuşup uyuşmadığı. Ölçmek için doğruluğu "
                    + "bilinen bir referans kaynak gerekir; elimizde yok.", false),

    GUNCELLIK("Güncellik", "Timeliness",
            "Verinin ne kadar güncel olduğu. Ölçmek için kaydın ne zaman toplandığını "
                    + "gösteren güvenilir bir tarih alanı gerekir; eşleştirme ekranında böyle "
                    + "bir alan tanımlanmıyor.", false);

    private final String turkce;
    private final String ingilizce;
    private final String aciklama;
    private final boolean olculebilir;

    KaliteBoyutuTuru(String turkce, String ingilizce, String aciklama, boolean olculebilir) {
        this.turkce = turkce;
        this.ingilizce = ingilizce;
        this.aciklama = aciklama;
        this.olculebilir = olculebilir;
    }

    public String getTurkce() {
        return turkce;
    }

    public String getIngilizce() {
        return ingilizce;
    }

    public String getAciklama() {
        return aciklama;
    }

    public boolean isOlculebilir() {
        return olculebilir;
    }
}
