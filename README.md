# Data Quality & Master Data Management (MDM) Tool

**Geliştirici:** Zekiye Nur Kırat
**Teknoloji yığını:** Java 21 · Spring Boot 3.3 · PostgreSQL · Thymeleaf · Bootstrap 5

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

Araç iki aşamada çalışır: önce **salt okunur analiz**, sonra **kullanıcı onayına bağlı
düzeltme**. Analiz aşamasında veriye tek bir yazma işlemi yapılmaz.

---

### A · Analiz — salt okunur

#### 1. Veri profilleme (Data Profiling)

Seçilen tabloyu tek taramada profiller: satır sayısı, kolon doluluk oranı, tekil değer
sayısı. Kimlik adayı kolon için doluluk ve tekillik yüzdesi ayrı gösterilir ve kolonun
kimlik olarak kullanılmaya uygun olup olmadığı raporda değerlendirilir.

#### 2. Kimlik üretme zinciri (Identity Resolution)

Tabloda kullanılabilir bir birincil anahtar olmayabilir. Araç sorumluluk zinciri deseniyle
çalışan bir kimlik üreticisi kullanır:

1. Kimlik kolonu seçildiyse ve doluysa onu kullanır
2. Değilse eşleştirilen alanlardan **SHA-256 özeti** üretir

Üretilen kimlikler raporda işaretlenir ve veritabanında aranamayacakları belirtilir. Kimlik
hem satır listesinde hem kopya tablosunda **aynı değeri** taşır.

#### 3. Kalıp kelime tespiti — dirsek yöntemi (Stop-word Detection)

Firma adlarının neredeyse tamamında geçen hukuki kalıplar firmaları birbirinden ayırmaz,
yalnızca benzerlik puanını şişirir. Araç kelime frekans listesindeki **en büyük düşüşü**
arar ve o noktaya kadarki kelimeleri karşılaştırma dışı bırakır.

Düşüş eşiğin altındaysa **hiçbir kelime silinmez** ve ekranda nedeni sayıyla açıklanır:
kararı kullanıcı verir. Seçilen kelimeler raporda ayrı bir tabloda, otomatik mi kullanıcı
seçimi mi olduğu belirtilerek listelenir.

#### 4. Kural tabanlı doğrulama

Yedi kural çalışır; her biri hangi DAMA boyutunu ölçtüğünü `Kural` arayüzü üzerinden
bildirmek zorundadır:

| Kural | Ne denetler | DAMA boyutu |
|---|---|---|
| `EksikAlanKurali` | Zorunlu alan boş mu | Tamlık |
| `PlaceholderKurali` | `N/A`, `-`, `yok` gibi sahte doluluk | Tamlık |
| `EmailRule` | E-posta biçimi, hücrede birden fazla adres | Geçerlilik |
| `TelefonKurali` | Numara yapısı (libphonenumber), çoklu numara | Geçerlilik |
| `WebSitesiKurali` | URL biçimi — beş ayrı hata sınıfı | Geçerlilik |
| `BoslukKurali` | Baştaki/sondaki ve ardışık boşluklar | Tutarlılık |
| `KimlikNoKurali` | Kimlik numarası kalıbından sapma | Tutarlılık |

Bir alanda yer tutucu bulunduğunda o alanın diğer kuralları **hiç çalıştırılmaz**: olmayan
bir bilginin biçimini sormak bulgu değil gürültü üretir.

#### 5. Kimlik numarası kalıp analizi (Pattern Analysis)

Kimlik numarası kolonunu iki aşamada çözer: önce baskın şekli keşfeder (ardışık rakam,
ardışık harf ve ayraçlardan bir maske çıkarır), sonra satırları o şekle karşı denetler.
Kalıp dağılımı raporda örnek değerlerle birlikte tablo hâlinde gösterilir.

Baskın kalıp değerlerin belirlenen oranın altını kapsıyorsa hiçbir satır işaretlenmez —
UUID gibi rastgele kimliklerde sapılacak bir kalıp yoktur.

**Hiçbir kimlik numarası değiştirilmez.** Kalıp dışı bir ek (şube kodu, kontrol hanesi)
silinirse iki ayrı tüzel kişilik tek kayda düşebilir.

#### 6. Kopya ve benzer kayıt tespiti (Entity Resolution)

- **Birebir kopyalar** kimliğe göre gruplanır — `O(n)`, sıfır ek sorgu. Kaç kayıt, kaç
  fazlalık olduğu grup grup gösterilir.
- **Benzer kayıtlar** PostgreSQL `pg_trgm` trigram benzerliğiyle bulunur; sorgu çalışmadan
  önce ilgili ifade üzerinde GIN indeksi kurulur.
- Firma adı ve adres için **iki ayrı puan** gösterilir, tek skorda birleştirilmez.
- **Çoklu adres kolonu** desteklenir: adres şehir / mahalle / sokak / kapı no diye ayrılmış
  tablolarda hepsi seçilip birlikte karşılaştırılabilir.
- Ölçülemeyen adres puanı sıfır değil tire ile gösterilir; "hiç benzemiyor" ile "bilmiyoruz"
  ayrı bilgilerdir.

#### 7. Kalite skor kartı (DAMA Scorecard)

Altı boyut ayrı ayrı puanlanır: **Tamlık, Geçerlilik, Teklik, Tutarlılık, Doğruluk,
Güncellik.** Ölçülemeyen boyutlar karttan silinmez, tire ile durur ve genel puana dahil
edilmez.

Puanlamada **bulgu değil hücre** sayılır ve denetlenmemiş hücre paydaya hiç girmez.

#### 8. Eylem haritası (Remediation Routing)

Her bulgu üç kovadan birine düşer:

- **Otomatik düzeltilebilir** — tek adımda giderilir
- **İnceleme gerekir** — insan kararı (data steward) ister
- **Bilerek düzeltilmiyor** — düzeltilebilir ama otomatik düzeltmek veri kaybına yol açar
  (*non-destructive cleansing*)

#### 9. Doğrulama özeti ve yapılmayan kontroller

Binlerce satırlık bulgu listesi tek bir mesaj türüyle dolabildiği için bulgular alan ve
mesaj ikilisine göre gruplanıp adede göre sıralanır.

Ayrıca araç **yapamadığı kontrolleri de raporlar**: ülke seçilmediği için telefon
denetlenmediyse, firma adı eşleştirilmediği için kopya taraması yapılmadıysa ya da bazı
satırlar için kimlik üretilemediyse bunlar ayrı bir uyarı bloğunda yazar. Boş bir tablonun
"sorun yok" gibi okunmasını engeller.

---

### B · Düzeltme — kullanıcı onayına bağlı

#### 10. Veri temizleme (Data Remediation)

Kolon başına ayrı kural belirlenir: baştaki ve sondaki boşlukları kırpma, ardışık boşlukları
teke indirme, serbest metinle karakter silme, harf dönüşümü.

Akış üç ekrana bölünmüştür — **kolon seç, kural belirle, önizle ve onayla.** Kullanıcı eski
ve yeni değerleri birebir görmeden hiçbir şey yazılmaz. İşlem sonrası ekranda güncellenen,
atlanan ve eşleşmeyen satır sayıları ayrı ayrı raporlanır.

#### 11. Değer onarım motoru (Repair Engine)

Ortak bir arayüz altında dört onarıcı çalışır:

| Onarıcı | Ne yapar |
|---|---|
| `EpostaOnarici` | İç boşluk, tekrarlayan işaretler ve baş/son noktalama temizler. Yalnızca fazlalığı kaldırır, eksik alan adı uydurmaz. Sonuç geçerli değilse değer değiştirilmez. |
| `WebSitesiOnarici` | Şema ve alan adını küçültür, varsayılan portu ve sondaki tek eğik çizgiyi siler. **Yola dokunmaz** — RFC 3986'ya göre alan adı büyük/küçük harf duyarsız, yol duyarlıdır. Eksik şema tamamlanmaz. |
| `TelefonOnarici` | libphonenumber ile E164, INTERNATIONAL veya NATIONAL biçimine çevirir. |
| `BoslukOnarici` | Boşluk düzenler, hiçbir şeyi yorumlamaz; özel onarıcısı olmayan metin kolonlarının varsayılanıdır. |

Sonuç **üç durumludur**: değişti, gerek yoktu, onarılamadı. İki durumlu olsaydı zaten
geçerli biçimdeki kayıtlar kullanıcıya "atlandı" görünürdü.

#### 12. Ana kayıt üretimi (Golden Record & Survivorship)

Birleştirmeden önce adaylar **birleştir-bul (union-find)** ile kümelenir: A ile B, B ile C
eşleşiyorsa üçü tek kümedir. İkişerli birleştirme bu üçlüyü iki ayrı ana kayda bölerdi.

İki kip:

- **Kayıt seviyesinde** — sıralı kural zinciri (doluluk, karakter uzunluğu, kimlik sırası)
  kaynak kayıtlardan birini ana kayıt seçer. Zincir her zaman bir karara ulaşır çünkü sonuç
  deterministik olmalıdır; ama **zayıf bir kuralla verilen karar gizlenmez** — hangi kuralın
  karar verdiği ekranda yazar ve kullanıcıya diğer kaydı seçme imkânı sunulur.
- **Alan seviyesinde (Field-level Survivorship)** — kullanıcı her kolonu ayrı seçer,
  girdilerin hiçbiri olmayan **yeni bir kayıt sentezlenir.** Bir satırdaki telefon ile
  ötekindeki e-posta birlikte yaşar.

#### 13. Denetim izi ve veri soyağacı (Audit Trail & Lineage)

Hiçbir satır silinmez, kaynak tabloya satır da eklenmez. Kararlar aracın kendi tablolarında
saklanır:

- `veri_kalitesi_birlestirme` — kim ana kayıt, kim ona bağlı, **hangi kuralın hangi
  sayılarla** karar verdiği, karar zamanı. Kullanıcı aracın önerisini ezdiyse o da yazılır.
- `veri_kalitesi_altin_kayit` — sentezlenen ana kaydın **kolon başına bir satırı**, her satır
  o değerin hangi kaynak kayıttan geldiğini taşır (alan seviyesinde soyağacı).

#### 14. Veri şeffaflığı (Data Provenance)

Birleştirme ekranında değerler ham hâliyle değil onarım motorundan geçmiş hâliyle sunulur.
Bir değer değiştiyse yanında onarıldığını belirten bir rozet ve altında **ham hâli** görünür.
Sessizce değiştirilmiş veriye güvenilmez.

---

### C · Genel

#### 15. Hata yönetimi

Yakalanmayan istisnalar, kök nedeni yazan ve geri dönüş yolu sunan bir ekrana dönüşür; tam
yığın günlüğe yazılmaya devam eder. Oturum zaman aşımına uğradığında kullanıcı hata almaz,
bağlantı ekranına yönlendirilir.

#### 16. Uluslararası veri desteği

Arapça, Kiril ve Latin alfabeleriyle çalışır. Küçük harfe çevirme `Locale.ROOT` ile yapılır,
içerik kontrolü `Character.isLetterOrDigit` ile, özet alınırken kodlama UTF-8 olarak açıkça
verilir. Ülke kodu tablo adından tahmin edilir ama karar kullanıcıya bırakılır; ülke listesi
`Locale.getISOCountries()` üzerinden üretilir, elle yazılmış bir harita yoktur.

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

| | Sürüm | Not |
|---|---|---|
| **Java** | 21 veya üstü | `java -version` ile kontrol et |
| **Maven** | 3.9+ | IntelliJ / Eclipse kullanacaksan gerekmez, IDE'ler kendi Maven'ını taşır |
| **PostgreSQL** | 13+ | İncelenecek veritabanı |

### 1. `pg_trgm` eklentisini etkinleştir

Benzer kayıt tespiti bu eklentiye bağlıdır ve uygulama tarafından kurulmaz. İnceleyeceğin
veritabanında **bir kez** çalıştır:

```sql
CREATE EXTENSION IF NOT EXISTS pg_trgm;
```

### 2. Projeyi al

```bash
git clone https://github.com/zekiyenurkirat/DataQualityTool.git
cd DataQualityTool
```

### 3. Çalıştır — üç yoldan biri

**A. IDE ile (en kolay)**

IntelliJ IDEA ya da Eclipse'te **File → Open** ile klasörü aç. Maven bağımlılıkları
kendiliğinden indirilir (ilk seferde birkaç dakika sürebilir). Sonra
`src/main/java/com/verikalitesi/VeriKalitesiApplication.java` dosyasını açıp
`main` metodunun yanındaki ▶ düğmesine bas.

**B. Terminalden**

```bash
mvn spring-boot:run
```

**C. Çalıştırılabilir jar olarak**

```bash
mvn package
java -jar target/veri-kalite-analizi-1.0-SNAPSHOT.jar
```

Üçünde de konsolda şu satırı gördüğünde uygulama hazırdır:

```
Started VeriKalitesiApplication in 1.8 seconds
```

### 4. Tarayıcıda aç

**http://localhost:8080/baglan**

Karşına bağlantı formu gelir. Kendi PostgreSQL bilgilerini gir ve **Bağlan**'a bas.
Sonrasında ekranın üstündeki dokuz adımlık şerit yol gösterir:

1. Tabloyu seç
2. Hangi kolonun firma adı, e-posta, telefon, adres olduğunu eşleştir
3. Kalıp kelime ekranında öneriyi onayla
4. **Analizi başlat** → rapor açılır

> Araç **gömülü veri taşımaz.** İncelenecek veriyi kendi veritabanından okur; boş bir
> veritabanına bağlanırsan uygulama açılır ama gösterecek bulgu bulamaz.

Durdurmak için konsolda **Ctrl + C**.

### 5. (İsteğe bağlı) Bağlantı formunu önceden doldur

Aynı veritabanıyla sık çalışacaksan her seferinde form doldurmamak için
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

### Sık karşılaşılan sorunlar

| Belirti | Sebep ve çözüm |
|---|---|
| `Web server failed to start. Port 8080 was already in use` | 8080 portu dolu. `application.properties` dosyasına `server.port=8081` ekle. |
| `function similarity(text, text) does not exist` | `pg_trgm` eklentisi kurulu değil — 1. adımı çalıştır. |
| `operator class "gin_trgm_ops" does not exist` | Aynı sebep, aynı çözüm. |
| `invalid target release: 21` | Java sürümü 21'in altında. `java -version` ile kontrol et. |
| Rapor ekranı uzun sürüyor | İlk çalıştırma trigram indeksini kurar. Sonraki taramalar belirgin şekilde hızlıdır. |
| Beklenmeyen bir hata | Uygulama beyaz hata sayfası yerine kök nedeni yazan bir ekran gösterir; tam yığın konsoldadır. |

### Ayarlanabilir eşikler

`src/main/resources/application.properties` içinde, her değerin yanında onu haklı çıkaran
ölçüm yazılıdır:

| Ayar | Varsayılan | Ne yapar |
|---|---|---|
| `veri.benzerlikEsigi` | `0.7` | İki adın benzer sayılması için gereken en düşük trigram puanı |
| `veri.kalipKelimeEnAzDususOrani` | `2.0` | Kalıp kelime tespitinde aranan en küçük frekans düşüşü |
| `veri.kimlikNoBaskinKalipOrani` | `0.8` | Bir kalıbın "baskın" sayılması için gereken kapsama oranı |
| `veri.yerTutucular` | liste | Dolu görünen ama bilgi taşımayan değerler |

### Testleri çalıştır

```bash
mvn test
```

219 testin tamamı veritabanı gerektirmez; sahte veriyle çalışır.

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
