# Data Quality & Master Data Management (MDM) Tool

PostgreSQL tablolarındaki veri kalitesi sorunlarını kural tabanlı olarak tespit eden,
raporlayan ve kullanıcı onayıyla düzelten bir web uygulaması.

Araç herhangi bir PostgreSQL şemasına çalışma anında bağlanır, kolonların iş anlamını
kullanıcıdan öğrenir, tabloyu **DAMA veri kalitesi çerçevesine** göre puanlar, kopya ve
şüpheli kayıtları bulur ve — istenirse — kopya kayıtları tek bir **ana kayıt (golden
record)** altında birleştirir.

**Java 21 · Spring Boot 3.3 · PostgreSQL · Thymeleaf · Bootstrap 5**
**78 sınıf · 219 birim testi**

---

## İçindekiler

- [Ne yapar](#ne-yapar)
- [Ekran akışı](#ekran-akışı)
- [Kurulum](#kurulum)
- [Mimari](#mimari)
- [Öne çıkan teknik kararlar](#öne-çıkan-teknik-kararlar)
- [Ölçülmüş sonuçlar](#ölçülmüş-sonuçlar)
- [Testler](#testler)
- [Bilinen eksikler](#bilinen-eksikler)
- [Yol haritası](#yol-haritası)

---

## Ne yapar

### 1. Veri profilleme (Data Profiling)

Seçilen tabloyu tek taramada profiller: satır sayısı, kolon doluluk oranı, tekil değer
sayısı. Kimlik adayı kolonlar için doluluk ve tekillik yüzdesi ayrı gösterilir.

### 2. Kural tabanlı doğrulama

Altı kural çalışır ve her biri hangi DAMA boyutunu ölçtüğünü bildirmek zorundadır:

| Kural | Ne denetler | DAMA boyutu |
|---|---|---|
| `EksikAlanKurali` | Zorunlu alan boş mu | Tamlık |
| `PlaceholderKurali` | `N/A`, `-`, `yok` gibi sahte doluluk | Tamlık |
| `EmailRule` | E-posta biçimi, hücrede birden fazla adres | Geçerlilik |
| `TelefonKurali` | Numara yapısı (libphonenumber), çoklu numara | Geçerlilik |
| `WebSitesiKurali` | URL biçimi, beş ayrı hata sınıfı | Geçerlilik |
| `BoslukKurali` | Baştaki/sondaki ve ardışık boşluklar | Tutarlılık |
| `KimlikNoKurali` | Kimlik numarası kalıbından sapma | Tutarlılık |

### 3. Kalite skor kartı (DAMA Scorecard)

Altı boyut ayrı ayrı puanlanır: **Tamlık · Geçerlilik · Teklik · Tutarlılık · Doğruluk ·
Güncellik.** Ölçülemeyen boyutlar karttan silinmez, `—` ile durur ve genel puana dahil
edilmez.

### 4. Eylem haritası (Remediation Routing)

Her bulgu üç kovadan birine düşer:

- **Otomatik düzeltilebilir** — tek adımda giderilir
- **İnceleme gerekir** — insan kararı ister
- **Bilerek düzeltilmiyor** — düzeltilebilir ama otomatik düzeltmek veri kaybına yol açar

### 5. Kopya ve benzer kayıt tespiti (Entity Resolution)

- **Birebir kopyalar** kimliğe göre gruplanır — `O(n)`, sıfır ek sorgu
- **Benzer kayıtlar** PostgreSQL `pg_trgm` trigram benzerliğiyle bulunur
- Firma adı ve adres için **iki ayrı puan** gösterilir, tek skorda birleştirilmez

### 6. Veri temizleme (Data Remediation)

Boşluk düzeltme, serbest karakter silme, harf dönüşümü, e-posta / telefon / web adresi
onarımı. **Hiçbir değişiklik önizleme ve onay olmadan yazılmaz.**

### 7. Ana kayıt üretimi (Golden Record & Survivorship)

İki kipte çalışır:

- **Kayıt seviyesinde** — sıralı kural zinciri (doluluk → karakter uzunluğu → kimlik
  sırası) kaynak kayıtlardan birini ana kayıt seçer
- **Alan seviyesinde (Field-level Survivorship)** — kullanıcı her kolonu ayrı seçer,
  girdilerin hiçbiri olmayan **yeni bir kayıt sentezlenir**

Kaynak tablo hiç değişmez. Kararlar ayrı bir **çapraz referans (XREF)** tablosunda,
gerekçesi ve zaman damgasıyla saklanır.

---

## Ekran akışı

```
1 Bağlantı → 2 Tablo → 3 Eşleştirme → 4 Kelime → 5 Rapor
                                                    ↓
                        6 Kolon → 7 Kural → 8 Önizleme → 9 Sonuç
```

Dokuz adımın tamamı sunucuda üretilir. **Uygulamada JavaScript çalışmaz.**

---

## Kurulum

### Gereksinimler

- Java 21+
- Maven 3.9+
- PostgreSQL 13+

### 1. `pg_trgm` eklentisini etkinleştir

Benzer kayıt tespiti bu eklentiye bağlıdır. İnceleyeceğin veritabanında **bir kez**
çalıştır:

```sql
CREATE EXTENSION IF NOT EXISTS pg_trgm;
```

### 2. Projeyi çalıştır

```bash
git clone https://github.com/zekiyenurkirat/DataQualityTool.git
cd DataQualityTool
mvn spring-boot:run
```

Tarayıcıda: **http://localhost:8080/baglan**

### 3. (İsteğe bağlı) Bağlantı formunu önceden doldur

Geliştirme sırasında her seferinde form doldurmamak için
`src/main/resources/application-dev.properties` dosyasını oluştur:

```properties
dev.sunucu=localhost
dev.port=5432
dev.veriTabani=veritabani_adi
dev.kullaniciAdi=kullanici
dev.sifre=sifre
```

> Bu dosya `.gitignore` içindedir ve **depoya girmez.** Oluşturmazsan uygulama yine
> çalışır, form yalnızca boş gelir.

### Ayarlanabilir eşikler

`src/main/resources/application.properties` içinde, her değerin yanında onu haklı çıkaran
ölçüm yazılıdır:

| Ayar | Varsayılan | Ne yapar |
|---|---|---|
| `veri.benzerlikEsigi` | `0.7` | İki adın benzer sayılması için gereken en düşük trigram puanı |
| `veri.kalipKelimeEnAzDususOrani` | `2.0` | Kalıp kelime tespitinde aranan en küçük frekans düşüşü |
| `veri.kimlikNoBaskinKalipOrani` | `0.8` | Bir kalıbın "baskın" sayılması için gereken kapsama oranı |
| `veri.yerTutucular` | liste | Dolu görünen ama bilgi taşımayan değerler |

---

## Mimari

```
com.verikalitesi
├── controller   Spring MVC uçları — 5 denetleyici + global hata yakalayıcı
├── service      Doğrulama ve temizleme iş akışları
├── rule         Kural motoru — her kural DAMA boyutunu bildirir
├── temizleme    Değer onarıcıları (e-posta, telefon, web adresi, boşluk)
├── anahtar      Kimlik üretme zinciri (kolon → içerikten SHA-256)
├── altinkayit   Ana kayıt seçimi, kümeleme, alan seviyesinde sentez
├── dao          JDBC erişim katmanı — arayüz + PostgreSQL uygulaması
├── dto          Veri taşıma nesneleri
└── core         Bağlantı yönetimi, ülke kodu çözümleme, kalıp kelime tespiti
```

**Katmanlı mimari:** Denetleyici → Servis → DAO. Her DAO'nun arayüzü ayrıdır; testler
sahte uygulamalarla çalışır, veritabanı gerektirmez.

**JPA/Hibernate kullanılmadı.** Araç hangi tabloyla çalışacağını çalışma anında öğreniyor;
önceden tanımlı varlık sınıfı çıkarılamaz. Bu yüzden `JdbcTemplate` ile dinamik SQL
tercih edildi.

---

## Öne çıkan teknik kararlar

### Kimlik, satırın kendi içeriğinden üretilir

Kaynak tabloların çoğunda kullanılabilir bir birincil anahtar yok. Tabloya kolon eklemek
yerine kimlik, eşleştirilen alanların **SHA-256 özetinden** türetiliyor. Rastgele atanan
bir anahtar yeni bir veri çekiminde eşleşmez; içerikten üretilen anahtar aynı şirketi
çekimler arasında eşleştirir.

### Kalıp kelime eleme: sabit eşik yerine dirsek yöntemi

Firma adlarının neredeyse tamamında geçen hukuki kalıplar (`LTD`, `B.V.`,
`жоопкерчилиги чектелген коому`) firmaları birbirinden ayırmaz, yalnızca benzerlik
puanını şişirir. Sabit bir yüzde eşiği yerine **frekans listesindeki en büyük düşüş**
aranır; düşüş belirlenen oranın altındaysa hiçbir kelime silinmez.

Dört kolonda ölçüldü: **5,13 · 14,03 · 3,42 · 1,47 kat.** Sonuncusu bir kişi adı
kolonuydu ve sabit eşik orada üç gerçek ismi silecekti.

### Kaynak tabloya asla yazılmaz

Ana kayıt kararları ayrı bir çapraz referans tablosunda tutulur. Gerekçesi: bir doğrulama
aracı incelediği verinin yapısını değiştirmemeli, salt okunur bir hesapla bağlanmış
olabilir, ve tabloya yazılan sentez satırı bir sonraki çalıştırmada kendi kaynaklarının
kopyası olarak görünürdü.

### Ölçülemeyen puanlanmaz

Denetlenmemiş bir hücre skor kartının paydasına girmez. Onları "sorunsuz" saymak puanı
yükseltirdi — ve o şekilde yükselen bir puan yanıltıcı olurdu.

### Yapamadığını sessizce yanlış yapma

Ülke seçilmediyse telefon kontrolü **hiç çalıştırılmaz** ve raporda "yapılmadı" diye
yazar. Kalıp kelime tespiti bir kırılma bulamazsa hiçbir kelimeyi silmez ve nedenini
sayıyla açıklar. Ana kayıt seçimi zayıf bir kurala düştüğünde bunu gizlemez.

### Uluslararası veri

Veri Arapça, Kiril ve Latin alfabeleri içeriyor. Küçük harfe çevirme `Locale.ROOT` ile
yapılır (Türkçe dil ayarında `NIL` → noktasız `nıl` olur ve karşılaştırma bozulur), içerik
kontrolü `a-z` aralığı yerine `Character.isLetterOrDigit` ile yapılır, özet alınırken
kodlama `UTF-8` olarak açıkça verilir.

---

## Ölçülmüş sonuçlar

5.000 satırlık gerçek bir ticaret sicili tablosunda:

| Ölçüm | Sonuç |
|---|---|
| Kopya grubu | **999 grup**, 3.987 fazlalık satır |
| Bağımsız doğrulama | Üretilen kimlikle gruplama, tablonun kendi sicil dosya numarasıyla **birebir aynı** bölümlemeyi verdi — sıfır çapraz uyuşmazlık |
| Kalıp kelime elenmezse | Sorgu **8,9 sn → 110,6 sn**; aday sayısı 52 → 288 |
| Kimlik parametresi tipi | İndeksli okuma **0,031 ms**, tam tablo taraması 11,5 ms |
| Kimlik kalıbı sapması | 4.999 değerin **602'si** kalıp dışı; 496'sı şube kaydı, 1'i kesik veri |

---

## Testler

```bash
mvn test
```

**219 test, tamamı geçiyor.**

Kural motoru, kimlik zinciri, birleştirme kural zinciri ve kümeleme doğrudan sınıf
seviyesinde test edilir — kırılan test kuralı gösterir, bağlantıyı değil.

Ayrıca **her Thymeleaf şablonu için üretim testi** vardır. Şablon ifade hataları derleme
aşamasında görünmez; sayfa ancak tarayıcıda açıldığında hata verir.

Bazı testler tamamen koruma amaçlıdır:

- `buyukIHarfiTurkceDildeSorunCikarmamali` — `Locale.ROOT` kaldırılırsa kırılır
- `latinDisiAlfabelerYerTutucuSanilmamali` — Arapça/Kiril veriyi korur
- `alanlarinYeriKorunmali` — kimlik özetinde alan kaymasını engeller
- `herKolonMetneCevriliyor` — sayısal adres kolonlarında sorgunun düşmesini engeller

---

## Bilinen eksikler

Bilinçli olarak kapsam dışı bırakılan on beş madde, her birinin sektördeki adı ve neden
yapılmadığının gerekçesiyle birlikte [`Bilinen_Eksiklikler.md`](Bilinen_Eksiklikler.md)
dosyasında listelenmiştir.

Öne çıkanlar:

- **Geri alma (undo) yok** — temizleme ve birleştirme geri alınamaz; ikisi de önizleme ve
  onay arkasındadır
- **Birebir kopya sadeleştirme yok** — içerikten üretilen kimliğe göre gruplanan satırlar
  araç açısından birbirinden ayırt edilemez; birincil anahtar olmadan hiçbirine fiziksel
  olarak işaret edilemez
- **Güncellik ve kaynak güveni kuralları yok** — bu veride ölçülemiyor, güvenilir bir
  tarih ya da kaynak kolonu bulunmuyor

---

## Yol haritası

- Birleştirmeyi geri alma (unmerge) — kaynak veriye dokunulmadığı için düşük maliyetli
- Denetim izinin yalnızca eklemeli (append-only) hale getirilmesi
- Bulguların CSV / Excel olarak dışa aktarımı
- Büyük tablolar için ön bölümleme (blocking) ile ölçeklenme

---

## Lisans

Bu depo bir portföy çalışmasıdır. Kullanım için lütfen iletişime geçin.
