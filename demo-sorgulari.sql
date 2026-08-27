-- =====================================================================
--  DEMO SORGULARI
--  Sunum sırasında yazmak yerine: ilgili bloğu fareyle seç, F5'e bas.
--  Sıra, anlatım sırasıyla aynı.
-- =====================================================================


-- ---------------------------------------------------------------------
-- 1) EN ÖNEMLİSİ: kaynak tablo değişmedi
--    Birleştirme yaptıktan SONRA çalıştır. 5000 çıkmalı.
--    Söylenecek: "Hiçbir satır silinmedi, tabloya satır da eklenmedi."
-- ---------------------------------------------------------------------
SELECT count(*) AS satir_sayisi FROM tablo_irak_tasjeelmot;


-- ---------------------------------------------------------------------
-- 2) DENETİM İZİ: kim ana kayıt, kim ona bağlı, hangi gerekçeyle
--    Söylenecek: "Karar kaydedildi. Hangi kuralın hangi sayılarla karar
--    verdiği yazılı; kullanıcı aracın önerisini ezdiyse o da yazılı."
-- ---------------------------------------------------------------------
SELECT kayit_kimligi,
       altin_kayit_id,
       is_master AS ana_kayit_mi,
       gerekce,
       karar_zamani
FROM veri_kalitesi_birlestirme
ORDER BY karar_zamani DESC;


-- ---------------------------------------------------------------------
-- 3) ALAN SEVİYESİNDE SOYAĞACI (field-level lineage)
--    Kolon başına bir satır; her satır değerin hangi kayıttan geldiğini
--    de taşıyor.
--    Söylenecek: "Ana kayıt girdilerden biri değil, alan alan sentezlendi."
-- ---------------------------------------------------------------------
SELECT altin_kayit_id,
       kolon_adi,
       deger,
       kaynak_kimlik
FROM veri_kalitesi_altin_kayit
ORDER BY altin_kayit_id, kolon_adi;


-- ---------------------------------------------------------------------
-- 4) VURUCU OLAN: ana kayıt kaç farklı kayıttan beslendi?
--    2 veya daha büyük bir sayı, alan seviyesinde birleştirmenin
--    kayıt seviyesinden farkını kanıtlar.
--
--    DİKKAT: 1 çıkıyorsa bütün alanlar tek kayıttan gelmiştir.
--    Bunu görmek için gelişmiş ekranda BİLEREK farklı kayıtlardan
--    seçim yapmak gerekiyor (birinde "boş" yazan alanda diğerini seç).
--
--    Söylenecek: "Bu ana kayıt hiçbir girdinin kopyası değil."
-- ---------------------------------------------------------------------
SELECT altin_kayit_id,
       count(DISTINCT kaynak_kimlik) AS kac_farkli_kaynaktan,
       count(*)                      AS kac_alan
FROM veri_kalitesi_altin_kayit
GROUP BY altin_kayit_id
ORDER BY kac_farkli_kaynaktan DESC;


-- ---------------------------------------------------------------------
-- 5) SUNUMDAN ÖNCE TEMİZ SAYFA (isteğe bağlı)
--    Bunlar aracın kendi tabloları; ilk birleştirmede yeniden oluşur.
--    Kaynak veri tablolarına dokunmaz.
-- ---------------------------------------------------------------------
-- DROP TABLE IF EXISTS veri_kalitesi_altin_kayit;
-- DROP TABLE IF EXISTS veri_kalitesi_birlestirme;
