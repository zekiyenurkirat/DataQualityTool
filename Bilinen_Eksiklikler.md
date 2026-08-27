# Bilinen Eksikler

Aracın kapsamı dışında kalan işler. Her madde, sektörde karşılığı olan terimle birlikte
yazıldı — eksiğin adını bilmek, onu ne zaman ve nasıl kapatacağını da belirliyor.

---

## A. Süre yetmediği için yapılmadı

### 1. Hazırlık katmanı ve denetim izi yok
**Sektördeki adı:** *Staging Layer & Audit Trail* — hazırlık katmanı ve denetim izi.

Kurumsal araçlarda (Informatica MDM) veri önce bir **hazırlık tablosuna** yazılır,
temizleme orada yapılır, sonuç **ana tabloya** aktarılır. Kaynak tabloya hiç dokunulmaz.
Yanına da her değişikliğin kim/ne zaman/eski değer/yeni değer kaydı tutulur.

Bizim araç kaynak tabloyu **doğrudan** güncelliyor. Sonuç ekranında eski/yeni örnekleri
gösteriyoruz ama bu kalıcı bir kayıt değil, sayfadan çıkınca kayboluyor.

**Neden yapılmadı:** Kalıcı bir geçmiş tablosu, tablo yaratma yetkisi ve şema yönetimi
gerektiriyordu. Yarım kalırsa çalışan temizleme akışını da bozardı.

**Sonucu:** Geri alma (*rollback*) yok. Temizleme öncesi yedek almak kullanıcının sorumluluğunda.

---

### 2. Kimlik doğrulama ve oturum yönetimi yok
**Sektördeki adı:** *Authentication & Authorization* — kimlik doğrulama ve yetkilendirme.

Araç şu an tek kullanıcılı, yerel bir analiz aracı. Veritabanı şifresi ekrandan giriliyor
ve oturum boyunca bellekte tutuluyor. Sunucuya konacaksa Spring Security ile
kullanıcı yönetimi ve rol bazlı yetki gerekir.

---

### 3. E-posta ve telefon ham metin olarak taşınıyor
**Sektördeki adı:** *Primitive Obsession* — ilkel tip takıntısı. Çözümü *Value Object*
(değer nesnesi).

`String eposta` yerine `EPosta` diye bir tip olsaydı, doğrulama nesnenin kendi içinde
tek yerde toplanırdı. Şu an her kullanım yerinde tekrar kontrol etmek gerekiyor.

---

### 4. Kimlik ham metin olarak taşınıyor
Aynı sorunun kimlik alanındaki hali. `long` → `String` dönüşümü sırasında derleyici
`Map` içindeki kullanımları gösteremedi, elle bulmak gerekti. Kendi tipi olsaydı
derleyici her kullanım yerini işaretlerdi.

---

### 5. Açık öbekleme yapılmadı
**Sektördeki adı:** *Blocking* — öbekleme.

Kayıt eşleştirmede önce kaba bir ölçütle (şehir, ülke, adın ilk harfi) küçük öbekler
oluşturulur, pahalı benzerlik hesabı yalnızca öbek içinde yapılır.

Bizde GIN üçlü-harf indeksi aday listesini daraltarak bu işi **dolaylı** yapıyor.
`EXPLAIN ANALYZE` ile ölçüldü: 5000 satırda satır başına ~1 aday, 191 ms. Bu ölçekte
açık öbeklemeye ihtiyaç yok; veri büyürse gerekir.

---

### 6. Servis yapıcısı fazla parametre alıyor
**Sektördeki adı:** *Long Parameter List* — uzun parametre listesi. Çözümü
*Configuration Properties* nesnesi.

`ValidationService` altı parametre alıyor; üçü ayar değeri. Ayarların
`@ConfigurationProperties` ile tek bir nesnede toplanması gerekirdi. Demo öncesi
yeniden düzenleme riskli görüldüğü için ertelendi.

---

## B. Bilinçli olarak kapsam dışı bırakıldı

### 7. Otomatik birleştirme yok — birleştirme var ama onaya bağlı
**Sektördeki adı:** *Golden Record & Survivorship Rules* — altın kayıt ve hayatta kalma
kuralları.

**Yapılan:** Araç iki kipte birleştirme yapıyor. Basit kipte kural zinciri (doluluk →
karakter uzunluğu → kimlik sırası) kaynak kayıtlardan birini ana kayıt seçiyor. Alan
seviyesinde kipte (*field-level survivorship*) her kolon ayrı ayrı seçiliyor ve girdilerin
hiçbiri olmayan **yeni bir ana kayıt sentezleniyor**. Değerler önce onarım motorundan
geçiyor, değişen değerler ekranda "onarıldı" damgası ve ham hâliyle gösteriliyor
(*data provenance*). Hiçbir satır silinmiyor, kaynak tablonun yapısına dokunulmuyor;
karar ayrı bir çapraz referans tablosunda (*XREF*) gerekçesiyle saklanıyor.

**Otomatik olmayan kısım — bilinçli:** Araç kendi başına birleştirmiyor, **adayları insana
sunuyor** (*Human-in-the-loop*). Gerekçesi ölçüldü: Kırgızistan verisinde
`226879-3301-Ф-л` kaydındaki `Ф-л` **şube** demek. Bunu merkez kaydıyla otomatik
birleştirmek iki ayrı tüzel kişiliği tek kayda düşürürdü. 5000 satır, incelenmesi gereken
6 karara indirildi.

**Hâlâ yapılmayan iki şey:** (1) *güncellik* ve *kaynak güveni* kuralları — tablolarda
güvenilir bir son güncelleme tarihi ve kaynak kolonu olmadığı için ölçülemiyor;
(2) birleştirmeyi geri alma (*unmerge*) — kaynak veriye dokunulmadığı için ucuz bir iş,
Faz 3'e bırakıldı.

---

### 7b. Birebir kopya grupları (A bölümü) birleştirilemiyor — yapısal sınır
**Sektördeki adı:** *Exact duplicate collapse* — birebir kopya sadeleştirme.

Araç `tablo_irak_tasjeelmot` üzerinde **999 grup ve 3.987 fazlalık satır** buluyor: aynı
firma bazen on beş kez girilmiş. "Bir tanesi kalsın, gerisi silinsin" doğal bir istek ama
araç bu kararı **veremiyor** — ve bu bir eksiklik değil, yapısal bir sınır.

**Sebep:** A bölümü grupları kimliğe göre kuruluyor. Bu tabloda kimlik kolonu olmadığı için
kimlik, eşleştirilen alanların SHA-256 özetinden üretiliyor. **Aynı kimlik = aynı içerik**
demek; yani gruptaki on beş satır araç açısından **birbirinden ayırt edilemez.** Her
hayatta kalma kuralı (doluluk, uzunluk) beraberlikle sonuçlanır.

İkinci sebep daha ağır: belirli bir satırı silmek için o fiziksel satıra **işaret
edebilmek** gerekir. Tabloda birincil anahtar yok. Postgres'in `ctid` alanı var ama
`UPDATE` ve `VACUUM` sonrası değiştiği için saklanabilir bir karar üretmez.

Üçüncüsü: araç yalnızca **eşleştirilen** kolonları okuyor. Firma adı aynı olan iki satır,
hiç bakılmamış bir kolonda (`capital`, `nationality`, kayıt tarihi) farklı olabilir.
Silmek, aracın hiç görmediği bilgiyi yok etmek olurdu.

**Ne gerekirdi:** kaynak tabloda gerçek bir birincil anahtar. O zaman doğal kural "en küçük
anahtar kalsın" (ilk yüklenen) ya da "tüm kolonlar üzerinden en dolu olan kalsın" olurdu,
ve silme fiziksel değil yumuşak olurdu.

**Sektörde nasıl:** birebir kopya sadeleştirme genelde veri kalitesi aracının değil,
**yükleme katmanının** işidir; satırın tamamının özeti alınıp ilk yüklenen kayıt tutulur.

Aracın tavrı: fazlalığı **ölçüp raporluyor**, kararı vermiyor. Birincil anahtarın
bulunmaması zaten başlı başına bir veri kalitesi bulgusudur.

---

### 8. Kimlik numaralarında otomatik düzeltme yapılmıyor
**Sektördeki adı:** *Non-destructive Cleansing* — bozmadan temizleme.

Kimlik numarası kolonunda kalıba uymayan değerler **işaretleniyor ama değiştirilmiyor**.
Kolombiya verisinde `1119182679-3` bulundu: sondaki `-3` bir kontrol hanesi. Silmek
mantıklı görünüyor ama aynı kalıp bir şube kodu da olabilir. Ölçüldü: 689 satırda
1 istisna, 4999 satırda 602 sapma. Hepsi rapora girdi, hiçbiri değiştirilmedi.

---

### 9. Farklı dillerdeki isimler ve aynı isimli farklı şirketler ayrıştırılmıyor
Ticari ad ile hukuki ad ayrımı, aynı isimli gerçekten farklı şirketler, farklı
alfabelerdeki yazımlar. Bunlar tespit edilebilir ama otomatik çözülemez — veride
cevabı olmayan, insan kararı gerektiren sorular.

---

### 10. Benzerlik puanları tek skorda birleştirilmiyor
**Sektördeki adı:** *Weighted Match Score* — ağırlıklı eşleşme puanı.

Ad, adres, telefon benzerliklerini tek bir puanda toplamak için alanlara ağırlık
vermek gerekir. Ağırlıkları etiketlenmiş örneklerden ölçmeden atamak, savunulamayan
bir sayı üretmek olurdu.

---

### 11. Çoklu değer içeren hücreler tespit ediliyor ama ayrıştırılmıyor
Bir hücrede iki e-posta ya da iki telefon varsa araç bunu bulguyor. Ayrıştırmıyor,
çünkü ikinci değerin nereye yazılacağı teknik değil **iş kararı** — yeni kolon mu,
yeni satır mı, yoksa silinecek mi?

---

### 12. Doğruluk ve güncellik ölçülmüyor
**Sektördeki adı:** *Accuracy* ve *Timeliness* — DAMA veri kalitesi boyutlarından ikisi.

Skor kartında bu iki boyut "—" olarak duruyor.

- **Doğruluk**, verinin gerçek dünyayla uyumudur. Ölçmek için doğruluğu bilinen bir
  **referans kaynak** (resmi ticaret sicili gibi) gerekir; elimizde yok.
- **Güncellik**, verinin ne kadar taze olduğudur. Ölçmek için kaydın ne zaman
  toplandığını gösteren güvenilir bir tarih alanı gerekir; eşleştirme ekranında böyle
  bir alan tanımlanmıyor.

Ölçülemeyen bir boyuta tahmini puan vermek yerine ölçülemediğini yazmayı tercih ettik.

---

### 13. Adres ayrıştırma ve standartlaştırma yapılmıyor
**Sektördeki adı:** *Address Parsing & Standardization* — adres ayrıştırma ve
standartlaştırma. Açık kaynak karşılığı `libpostal`, ticari karşılıkları Informatica
AddressDoctor ve Loqate.

Kurumsal araçlar serbest metin adresi bileşenlerine ayırır (ülke, şehir, mahalle, sokak,
kapı no) ve resmi posta veri tabanına karşı doğrular. Böylece "aynı sokak mı" ile
"aynı şehir mi" sorularını ayrı ayrı cevaplayabilirler.

**Bizde ne var:** yalnızca bulanık karşılaştırma. Eşleştirme ekranında birden fazla adres
kolonu seçilebiliyor; seçilenler birleştirilip trigram ile karşılaştırılıyor. Ayrıştırma
ve referans doğrulama yok.

**Bunun somut sonucu:** tablolar adresi farklı şekillerde tutuyor.
- `tablo_irak_tasjeelmot.company_address` tek kolonda tam adres:
  `العراق|البصرة|حي الحمراء م118`
- `tablo_irak` ise altı ayrı kolon: `governorate, city, qada, mahalla, zuqaq, building`

İkisi de destekleniyor: birinci tabloda tek kolon, ikincisinde dört kolon birden
seçilebiliyor. Ölçüldü (`tablo_irak`, 25.817 çift): yalnızca `city` seçilince 124 güçlü
aday çıkıyor, `building + city + mahalla + zuqaq` seçilince 113. Aradaki **11 çift**,
aynı şehirde ama farklı mahallede olan firmalar -- tek kolonla yakalanamıyordu.

Kullanıcı yine de tek kolon seçebilir; o zaman puan o kolonun ayırt ediciliği kadar olur.
`city` seçilirse 1,000 puan "aynı adres" değil "aynı şehir" demektir. Bu uyarı hem
eşleştirme hem rapor ekranında yazılı.

**Kalan eksik:** serbest metin adresin bileşenlerine ayrıştırılması. `tablo_irak_tasjeelmot`
adresi tek kolonda `ülke|şehir|mahalle` diye tutuyor; araç bunu tek metin olarak
karşılaştırıyor, "şehirler aynı ama mahalleler farklı" ayrımını yapamıyor. Bunun için
`libpostal` gibi bir ayrıştırıcı ya da lisanslı referans veri gerekiyor.

---

### 14. Tablo ve kolon adları doğrulanmıyor
**Sektördeki adı:** *SQL Identifier Validation*.

Sorgular metin birleştirmeyle kuruluyor. Tablo ve kolon adları açılır listeden geldiği
için pratikte güvenli, ancak form gövdesine elle başka bir değer gönderilebilir.
`information_schema` üzerinden doğrulama eklenmesi gerekir.

Değerler için bu risk yok — hepsi parametre olarak gönderiliyor.
