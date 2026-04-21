# 03 — String, StringBuilder, StringBuffer

Java'da string işlemleri, ilk bakışta çok basit görünür: "`String s = "merhaba"` yazdın, bitti." Ama bu sınıfın altında JVM'in en özenle tasarladığı yapılardan biri yatar. `String`'in neden immutable olduğunu, String Pool'un ne sihirli nesne olduğunu ve neden `"a" + "b" + "c"` yazmanın döngü içinde felaket olduğunu bu pakette anlayacaksın.

---

## 1) String Nedir?

`String`, `java.lang` paketinde bulunan bir **sınıftır** (primitive değil!). İçinde karakterlerden oluşan bir dizi tutar. Tarihsel olarak `char[]` kullanıyordu; Java 9'dan itibaren **Compact Strings** özelliği geldi — eğer string sadece Latin-1 karakterleri içeriyorsa `byte[]` olarak saklanıyor (%50 bellek tasarrufu), sadece gerektiğinde UTF-16'ya geçiyor.

En önemli iki özelliği:

**1. `final` sınıftır.** Yani kimse `String`'den miras alıp davranışını değiştiremez. Neden? Çünkü JVM, Security Manager, ClassLoader gibi kritik sistemler String davranışına güvenir. Bir hacker `String`'den türetip `equals` metodunu değiştirebilseydi, "admin" dize kontrollerini atlatabilirdi.

**2. Immutable'dır (değiştirilemez).** Bir String nesnesi yaratıldıktan sonra **asla değişmez**. Değiştirir gibi görünen her metod aslında **yeni bir String** üretir:

```java
String s = "merhaba";
s.toUpperCase();               // dönen değeri almıyoruz!
System.out.println(s);          // hala "merhaba"

s = s.toUpperCase();            // yeni string'i atıyoruz
System.out.println(s);          // "MERHABA"
```

İlk çağrıda `toUpperCase()` aslında `"MERHABA"` diye yeni bir String üretti, ama sen onu hiçbir yere atmadın, kayboldu. `s` hala eski değerini gösteriyor.

---

## 2) String Pool — Java'nın Sihirli Sözlüğü

Heap'in içinde, String'e özel ayrılmış bir alan vardır: **String Constant Pool** (Java 7 öncesi PermGen'deydi, 7+ heap'in kendisinde). Kodda yazdığın her **string literal** önce bu havuza bakılır.

```java
String a = "java";   // "java" pool'da var mı? Yok → pool'a ekle, referansı a'ya ver
String b = "java";   // "java" pool'da var mı? Var → aynı referansı b'ye ver
String c = new String("java");  // new → pool'u bypass et, heap'te YENİ nesne üret
```

Sonuç:
- `a == b` → **true** (ikisi de pool'daki aynı nesneyi gösterir)
- `a == c` → **false** (c heap'te ayrı bir nesne)
- `a.equals(c)` → **true** (içerikler aynı)
- `a == c.intern()` → **true** (`intern()` pool'daki versiyonu döner)

Bu neden var? **Bellek verimliliği**. Büyük bir uygulamada `"Cannot be null"` ifadesi yüzlerce yerde geçebilir. Her biri için ayrı String nesnesi olmaktansa hepsi aynı pool nesnesini paylaşır.

`new String("java")` yazmak neredeyse **her zaman yanlış**tır. Hem pool'u bypass edip fazla bellek yakarsın hem de karşılaştırmalar tutarsız olur. Literal yaz ve Java'nın kendi optimizasyonuna güven.

---

## 3) String Neden Immutable? (Tasarım Gerekçeleri)

Java tasarımcıları String'i kasten değiştirilemez yaptılar. Bunun beş büyük sebebi var:

**a) String Pool güvenliği.** Birçok değişken aynı nesneyi paylaşıyor. Birisi içeriğini değiştirse **başka yerlerde habersiz değişim** olurdu. Tamir edilemez hatalar.

**b) Thread-safety bedava gelir.** Bir nesnenin değişmediğini biliyorsan, onu kaç thread'in kullandığının hiçbir önemi yok — hiçbir senkronizasyona ihtiyaç yok. String'ler tüm thread'ler tarafından güvenle paylaşılır.

**c) HashMap anahtarı olarak güvenli.** HashMap bir anahtarın `hashCode`'una göre bucket seçer. Eğer String mutable olsaydı ve sen anahtar olarak koyduktan sonra içeriğini değiştirsen, anahtarın hashCode'u değişir ve Map bir daha o değeri bulamaz. String değişmediği için `hashCode` bir kere hesaplanır ve sonsuza dek geçerli kalır.

**d) Güvenlik.** Dosya yolu, URL, SQL parametresi gibi kritik stringler: düşün, bir metoda `deleteFile(path)` çağırıyorsun. İzin kontrolü sonrası path değiştirilebilseydi, TOCTOU (time-of-check, time-of-use) saldırıları mümkün olurdu. Immutable olduğu için kontrol sonrası değişemez.

**e) Cache edilebilir `hashCode`.** `String.hashCode()` ilk çağrıda hesaplanır, sonra saklanır. Bir daha hesaplanmaz. HashMap performansı için altın değerinde.

---

## 4) `new String("x")` Kaç Nesne Yaratır?

Klasik mülakat sorusu. Cevap: **Pool durumuna göre 1 veya 2.**

- Eğer `"x"` pool'da yoksa → önce pool'a eklenir (1 nesne), sonra `new` ile heap'te ayrı bir kopya üretilir (2. nesne). **Toplam: 2**.
- Eğer `"x"` pool'da zaten varsa → sadece heap'te yeni nesne üretilir. **Toplam: 1**.

Pratikte `new String(...)` kullanmak için neredeyse hiç sebep yoktur.

---

## 5) En Sık Kullanılan Metotlar

Günlük hayatta sürekli kullanacağın temel metotlar:

```java
s.length()                // karakter sayısı (parantezli! dizide length parantizsiz)
s.charAt(i)               // i. karakter (char döner)
s.indexOf("x")            // x'in ilk geçtiği index, yoksa -1
s.lastIndexOf("x")        // son geçtiği index
s.substring(a, b)         // [a, b) aralığı — b dahil değil
s.substring(a)            // a'dan sona
s.toLowerCase()           // küçük harf
s.toUpperCase()           // büyük harf
s.trim()                  // baştaki/sondaki boşlukları atar (ASCII)
s.strip()                 // trim gibi ama Unicode-aware (Java 11+)
s.split(",")              // ayırıcıya göre String[] döner
s.replace("a", "b")       // tüm a'ları b yapar
s.replaceAll(regex, ...)  // regex ile değiştir
s.contains("x")           // x var mı?
s.startsWith("ön")        // ile mi başlıyor?
s.endsWith("son")         // ile mi bitiyor?
s.equals(other)           // içerik karşılaştırma
s.equalsIgnoreCase(other) // büyük/küçük umursama
s.compareTo(other)        // sözlük sırası: negatif/0/pozitif
s.isEmpty()               // length == 0
s.isBlank()               // sadece boşluklardan oluşuyorsa true (Java 11+)
s.repeat(3)               // "ab".repeat(3) → "ababab" (Java 11+)
String.join(",", list)    // elemanları birleştirir
String.format("%d-%s", n, s)  // printf tarzı
```

**`equals` mi `equalsIgnoreCase` mi?**: Kullanıcıdan gelen girdiyi karşılaştırırken büyük/küçük fark umurunda değilse ikincisini kullan. "Java" ve "JAVA" aynı kabul edilsin istiyorsan.

**`trim` vs `strip`**: `trim` sadece ASCII ≤ 32 karakterleri siler. `strip` Unicode boşluk karakterlerini (non-breaking space `\u00A0` vs.) da siler. Java 11+'dan itibaren `strip` kullan.

---

## 6) String Concatenation ve Performans

Yaygın ama tehlikeli örüntü:
```java
String result = "";
for (int i = 0; i < 10_000; i++) {
    result += i + ",";
}
```

Bu kod neden yavaş? Çünkü her `+=` işleminde `result` immutable olduğu için **yeni bir String nesnesi üretilir**. 10.000 iterasyonda 10.000 yeni String, her biri öncekinden bir eleman daha uzun. Toplam operasyon **O(n²)**. 10.000 iterasyon normal çalışır ama 100.000'de gözle görülür yavaşlama, 1.000.000'da felaket.

Çözüm: **`StringBuilder`**.

```java
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 10_000; i++) {
    sb.append(i).append(",");
}
String result = sb.toString();
```

StringBuilder içte mutable bir `char[]` tutar, append ettiğinde sadece o diziye yazar (gerekirse büyütür). Toplam maliyet **O(n)**.

**Not**: Derleyici tek satırdaki `a + b + c` gibi string concatenation'ı zaten StringBuilder'a dönüştürür. Yani `String full = first + " " + last;` yazmak sorun değil. Asıl belası **döngü içindeki `+=`**.

---

## 7) StringBuilder vs StringBuffer

Bu ikisi aynı işi yapar, API'leri neredeyse özdeştir. Tek farkları:

| Özellik | StringBuilder | StringBuffer |
|---|---|---|
| **Mutability** | Mutable | Mutable |
| **Thread-safety** | Thread-safe **değil** | Thread-safe (synchronized metodlar) |
| **Performans** | Hızlı | Yavaş (sync maliyeti) |
| **Tanıtım** | Java 5 | Java 1.0 |

**Pratik kural**: Tek thread (çoğu durum) → `StringBuilder`. Birden fazla thread aynı anda yazacaksa → `StringBuffer`. Modern kodda StringBuffer'a nadiren ihtiyaç olur çünkü çoklu thread string birleştirme zaten yaygın bir senaryo değildir.

StringBuilder'ın temel API'si:
```java
sb.append("x")          // sonuna ekle
sb.insert(pos, "x")     // belirli pozisyona ekle
sb.delete(a, b)         // [a,b) arası sil
sb.replace(a, b, "x")   // aralığı yenisiyle değiştir
sb.reverse()            // tersine çevir
sb.length()             // uzunluk
sb.capacity()           // iç buffer kapasitesi (default 16)
sb.toString()           // Normal String'e çevir
```

Kapasite hakkında: StringBuilder başlangıçta 16 karakter kapasitesi açar. Doluyorsa iki katına çıkarıp kopyalar. Çok uzun string üreteceğini biliyorsan başta kapasite ver: `new StringBuilder(10_000)`. Bu sayede yeniden boyutlandırma masraflarından kaçınırsın.

---

## 8) Derleyici Aslında Ne Yapıyor?

`String s = "a" + "b" + variable;` yazdığında derleyici bunu aslında şuna çevirir (Java 8 ve öncesi):

```java
String s = new StringBuilder().append("a").append("b").append(variable).toString();
```

Java 9 ile **Indify String Concatenation** geldi. `invokedynamic` bytecode'u kullanarak runtime'da en uygun stratejiyi seçer — bazen StringBuilder, bazen daha optimize rotalar. Yani modern Java'da `+` operatörü daha da hızlıdır.

**Ama bu döngü içinde fark etmez.** Çünkü derleyici her iterasyonda yeni bir `StringBuilder` yaratır. Döngü dışından tek bir StringBuilder açıp `append` etmekle kıyaslanmaz.

---

## 9) Text Blocks — Çok Satırlı String'ler (Java 15+)

Uzun JSON, HTML, SQL yazarken `\n` ve escape cehenneminden kurtulma:

```java
String json = """
        {
            "ad": "Ali",
            "yas": 30
        }
        """;
```

Kurallar:
- `"""` açılış ve kapanış arasında herhangi bir içerik.
- İçerideki otomatik girintileme **tüm satırların ortak minimum girintisine** göre hizalanır (derleyici halleder).
- `\` karakteri satır sonunda olursa o satırın newline'ı iptal edilir (uzun satırı bölmek için).

---

## 10) String.format ve printf

`printf` tarzı biçimlendirme:
```java
String s = String.format("Ad: %-10s Yaş: %03d Pi: %.2f", "Ali", 7, 3.14159);
// "Ad: Ali        Yaş: 007 Pi: 3.14"
```

Yaygın format belirteçleri:
- `%s` — string
- `%d` — decimal integer
- `%f` — float/double (`%.2f` iki basamak)
- `%e` — bilimsel notasyon
- `%x` — onaltılık
- `%b` — boolean
- `%n` — platform-uygun satır sonu (Unix `\n`, Windows `\r\n`)

Hizalama: `%-10s` (sol hizalı, 10 karakter), `%10s` (sağ hizalı). `%03d` sıfır-padding.

`System.out.printf(...)` doğrudan yazdırır, `String.format(...)` değerini döndürür.

---

## 11) Yaygın Hatalar ve İpuçları

**a) `==` ile String karşılaştırma.** Asla yapma. Her zaman `.equals()`.

**b) Null-safety**: `if (s.equals("x"))` → `s` null ise NPE. Çözüm: `if ("x".equals(s))` (literal soldayken NPE olmaz çünkü literal null olamaz). Daha temiz: `Objects.equals(s, "x")`.

**c) `s.split(",")` boş elemanları atar**: `"a,,b".split(",")` → `["a", "", "b"]` (ortadaki boş da gelir). Ama `"a,b,".split(",")` → `["a", "b"]` (sondaki boş atılır!). Sondaki boşları da istiyorsan `s.split(",", -1)` kullan.

**d) Regex özel karakterleri**: `split` ve `replaceAll` regex alır. `"a.b".split(".")` boş array döner çünkü `.` regex'te "herhangi karakter" demektir. `split("\\.")` doğrusu.

**e) Intern fanatikliği**: `s.intern()` çağırmak bellek tasarrufu sağlar gibi görünür ama pool Java 7 öncesi PermGen'de sınırlıydı, dikkatsiz kullanınca OOM olurdu. Günümüzde de gereksiz karmaşıklık, çoğu durumda ihtiyaç yok.

---

## Mülakat Soruları ve Örnek Cevaplar

**1. String neden immutable?**
Beş sebep: (1) String pool güvenliği — paylaşılan nesnenin değişmesi başka yerleri bozardı. (2) Thread-safety — hiç senkronizasyona ihtiyaç yok. (3) HashMap anahtarı olarak güvenli — hashCode değişmez. (4) Güvenlik — dosya yolları, URL'ler, SQL parametreleri sonradan değiştirilemez. (5) Cache edilebilir hashCode.

**2. `==` ile `.equals()` String'de ne döner?**
`==` referans (adres) karşılaştırır. `.equals()` içerik karşılaştırır. Literal stringler pool'u paylaştıkları için bazen `==` true verebilir ama bu güvenilir değil. Her zaman `.equals()` kullan.

**3. `new String("java")` kaç nesne yaratır?**
Eğer "java" pool'da yoksa 2 (pool'a bir, heap'e bir). Varsa 1 (sadece heap).

**4. StringBuilder ve StringBuffer arasındaki fark?**
Her ikisi de mutable. StringBuffer thread-safe (synchronized) ama daha yavaş. StringBuilder thread-safe değil ama hızlı. Tek thread'de StringBuilder, çoklu thread paylaşımında StringBuffer tercih et. Çoğu modern kodda StringBuilder kullanılır.

**5. String pool nerede tutulur?**
Java 7'den itibaren normal heap'te, özel bir bölgede. Öncesi PermGen'deydi. `String.intern()` bir string'i pool'a ekleyip oradaki referansı döndürür.

**6. `"a" + "b" + "c"` nasıl derlenir?**
Java 8 ve öncesi: StringBuilder ile `new StringBuilder().append("a").append("b").append("c").toString()`. Java 9+: `invokedynamic` ile runtime-optimized strateji. Ama bu optimizasyon tek ifade içindir; döngü içinde `+=` hâlâ yavaştır.

**7. Immutable olmak performansı nasıl etkiler?**
Olumlu: thread-safety ücretsiz, hashCode cache'lenebilir, pool ile paylaşılır. Olumsuz: her değişiklik yeni nesne demek, döngüde StringBuilder yerine String kullanırsan O(n²) olur.

**8. `s.intern()` ne yapar?**
String'in pool'daki referansını döndürür. Pool'da yoksa ekler. Aynı içerikli stringleri tek referansa indirger, bellek tasarrufu sağlayabilir ama çoğu durumda gereksiz.

**9. Büyük bir metinde çok sayıda değiştirme yapacaksam?**
StringBuilder kullan. Her değiştirme operasyonunu üzerinde yap, sonunda `.toString()` ile String'e çevir.
