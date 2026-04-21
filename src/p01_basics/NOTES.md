# 01 — Java'ya Giriş, JVM/JDK/JRE ve Değişkenler

Bu bölüm senin Java "aha!" anların olmalı. Diğer dillerden geliyor olabilirsin, tamamen yeni de başlıyor olabilirsin — fark etmez. Amacımız şu sorulara **kesin** cevap verebilmek:
- Java gerçekten derlenen mi yorumlanan mı bir dil?
- `.java` yazdığımda tam olarak neler oluyor?
- Bir değişken tanımladığımda bilgisayarın aklında ne oluyor?
- `int` ile `Integer` aslında aynı şey mi, farklıysa neden?

---

## 1) Java Nedir? Neden Hâlâ Ayakta?

Java, 1995'te Sun Microsystems tarafından çıkarılan, Oracle tarafından yönetilen, **statik tipli, nesne yönelimli, platform bağımsız** bir programlama dilidir. Bu üç özelliği tek tek açayım çünkü her birinin altı önemli:

**Statik tipli:** Bir değişkenin tipi **derleme zamanında** bellidir. Yani `int x = "merhaba";` yazarsan daha programı çalıştırmadan, IDE bile sana kırmızı çizgiyi çeker, derleyici (`javac`) programı çalıştırmayı reddeder. JavaScript veya Python gibi dillerde bu hata ancak kodu çalıştırdığında patlar — Java'da daha başlangıçta yakalarsın. Bu, büyük projelerde **altın değerindedir** çünkü hataların çoğu daha kullanıcıya ulaşmadan ölür.

**Nesne yönelimli:** Kod, "nesneler" olarak organize edilir. Bir `Araba` sınıfı yazarsın, içinde `hız`, `renk` gibi veriler ve `hızlan()` gibi davranışlar olur. Bu yaklaşım gerçek dünya problemlerini modellemeyi kolaylaştırır ve kodun büyüdükçe yönetilebilir kalmasını sağlar.

**Platform bağımsız:** Bu Java'nın en büyük silahı ve bunu az sonra detaylıca göreceğiz.

Neden hâlâ popüler? Bankacılık, Android (uzun yıllar), devasa kurumsal sistemler, Spring/Hibernate gibi endüstri standardı framework'ler hep Java üzerine kurulu. Olgun ekosistem + 30 yıllık kütüphane + devasa topluluk = vazgeçilmez.

---

## 2) "Write Once, Run Anywhere" (WORA) Sihri

C/C++ gibi dillerde kodun **doğrudan işletim sisteminin makine diline** derlenir. Windows için derlediğin `.exe`, Linux'ta çalışmaz. Mac için ayrı derlemek zorundasın. Her platform için ayrı derleme = baş ağrısı.

Java bunu **ara bir katman** ile çözdü. Yazdığın `.java` dosyası doğrudan makine koduna değil, önce **bytecode** denen ara bir dile çevrilir (bu `.class` dosyasıdır). Bytecode herhangi bir gerçek CPU'nun anlamadığı, **hayali bir makine** için yazılmış komutlardır. O hayali makine: **JVM**.

Her işletim sisteminin kendi JVM'i vardır (Windows JVM, Linux JVM, macOS JVM...). Bytecode'u o anki sistemin anlayacağı makine koduna **çalışma anında** çevirirler. Yani senin `.class` dosyan değişmez, JVM platforma uyum sağlar.

Hayatı şu şekilde hayal et: Sen İngilizce yazıyorsun (Java kaynağı). Bir tercüman İngilizce'yi **Esperanto**'ya (bytecode) çeviriyor. Her ülkede yerel tercümanlar Esperanto'yu kendi dillerine çeviriyor (JVM). Sen sadece İngilizce yazdın, dünyanın her yerinde insanlar anlıyor.

Akış şöyle:
```
Hello.java  --(javac)-->  Hello.class  --(JVM)-->  CPU'ya özel makine kodu
  kaynak                     bytecode                 çalışır
```

---

## 3) JDK, JRE, JVM — Mülakatların Gözdesi

Bu üç harfli kısaltmayı sürekli karıştıran çok kişi var. Küçük bir sahnede anlatayım:

Diyelim ki bir restoran açıyorsun.
- **JVM (Java Virtual Machine)** = Mutfaktaki ocak. Yemek (bytecode) burada pişer, tek başına müşteriye yemek sunamaz.
- **JRE (Java Runtime Environment)** = Mutfağın kendisi. Ocak + tencereler + malzemeler var. Bir yemek (Java programı) **pişirilebilir** (yani çalıştırılabilir). Ama yeni tarif geliştiremezsin.
- **JDK (Java Development Kit)** = Mutfak + şef aletleri + tarif kitapları. Ocak, tencereler, bıçaklar, termometreler... **Yeni yemek tarif edip** geliştirebilirsin.

Teknik karşılıkları:

**JVM**, yalnızca bytecode'u çalıştıran sanal makinedir. İçinde Class Loader (sınıfları belleğe getirir), Execution Engine (bytecode'u yorumlar veya JIT ile derler), Memory Manager (heap/stack'i yönetir) ve Garbage Collector vardır.

**JRE**, JVM'in yanına standart kütüphaneleri (`java.lang`, `java.util`, `java.io` gibi binlerce hazır sınıf) ekler. Bir kullanıcının bilgisayarında sadece Java uygulaması çalıştırmak istiyorsan JRE yeter. Zaten günümüzde ayrı JRE kurulumu pek yok; JDK içinde gelir.

**JDK**, JRE'nin üstüne geliştirme araçları ekler: `javac` (derleyici), `javadoc` (dokümantasyon üretir), `jdb` (debugger), `jar` (paketleyici), `jshell` (REPL, Java 9+). Sen bir Java geliştiricisi olarak bilgisayarına **JDK kurarsın**.

Mantıksal içerik: **JDK ⊃ JRE ⊃ JVM**. En dıştaki geliştirici setinin içinden çalışma ortamı çıkar, onun da en içinde sanal makine vardır.

---

## 4) JVM'in İçinde Neler Dönüyor? (Derinlemesine)

Bir `.class` dosyasını çalıştırdığında JVM şu adımları yapar:

**a) Class Loader** sınıfı diskten okur, bellekteki **Method Area**'ya (Java 8+ Metaspace) yerleştirir. Üç tür class loader vardır ve sırayla çalışırlar:
1. **Bootstrap Loader** — JVM'in çekirdek sınıflarını yükler (`java.lang.String` gibi).
2. **Extension/Platform Loader** — standart uzantıları yükler.
3. **Application/System Loader** — senin yazdığın sınıfları yükler.

Bu hiyerarşide önemli bir prensip var: **Parent Delegation Model**. Alt loader bir sınıf yüklemeden önce üsttekine "sende var mı?" diye sorar. Bu sayede birisi kendi `java.lang.String` sınıfını yazıp sistemin String'inin yerine geçiremez — güvenlik.

**b) Bytecode Verifier** devreye girer. Bytecode'un tutarlı olduğunu (stack taşmaları yok, tip ihlalleri yok) doğrular. Bu sayede elle düzenlenmiş kötücül bir `.class` JVM'i çökertemez.

**c) Execution Engine** bytecode'u çalıştırır. İki şekilde:
- **Interpreter**: Komut komut okur, yorumlar. Yavaştır ama hemen çalışır.
- **JIT (Just-In-Time) Compiler**: "Sıcak" (sık çalışan) kod bloklarını tespit eder ve runtime'da **native makine koduna** derler. Bu kısım artık interpreter'ı ezer. JIT sayesinde Java, ilk saniyelerde yavaş başlasa bile dakikalar içinde C++'a yaklaşan hıza ulaşır.

**d) Garbage Collector** kullanılmayan nesneleri arka planda temizler. Bunu 14. pakette derinlemesine göreceğiz.

JIT mülakat sorusu olarak çok sık gelir. Cevap şablonu: "JIT, bytecode'u yorumlamak yerine sıcak kodu çalışma anında native koda çeviren, Java'nın asıl hızının kaynağı olan JVM bileşenidir. HotSpot JVM'de C1 (hızlı derleme, az optimizasyon) ve C2 (yavaş derleme, agresif optimizasyon) olmak üzere iki derleyici vardır."

---

## 5) İlk Programın Anatomisi

```java
public class Hello {
    public static void main(String[] args) {
        System.out.println("Merhaba");
    }
}
```

Bu satırlar basit görünse de her parçanın nedeni var:

**`public class Hello`**: Java'da her kod bir sınıfın içindedir — ayrı fonksiyonlar diye bir şey yoktur. Dosya adı sınıf adıyla **aynı olmak zorundadır** (`Hello.java`). Bir dosyada sadece **bir public sınıf** olabilir.

**`public static void main(String[] args)`**: JVM programı başlatırken özellikle bu imzayı arar. Dört parçası:
- `public` → JVM dışarıdan çağırabilsin.
- `static` → JVM önce bir Hello nesnesi oluşturup sonra çağıracak değil; doğrudan sınıftan çağırır. "Henüz nesne yaratmadan çalışacak kod" demek.
- `void` → geri dönüş yok. JVM zaten dönen değeri kullanmaz.
- `String[] args` → komut satırından `java Hello elma armut` dediğinde "elma" ve "armut" burada gelir.

İmzayı ufacık bile değiştirirsen (`static` yerine yok yazsan, `String[]` yerine `String` yazsan) JVM seni kibarca "`main method not found`" diyerek reddeder.

**`System.out.println(...)`**: `System` sınıfının `out` adında static bir alanı var; bu da bir `PrintStream` nesnesi. Onun `println` metodunu çağırıyoruz. Yani aslında "sistemdeki standart çıktı akışına bir satır yazdır" diyoruz.

---

## 6) Primitive (İlkel) Tipler — Java'nın Yapı Taşları

Java'nın tip sistemi iki büyük kampa ayrılır: **primitive'ler** (8 adet, ilkel, değer tipi) ve **nesne referansları** (milyonlarca sınıf). Primitive'ler düşük seviyeli, hafif ve hızlıdır; nesneler ise heap'te yaşar ve referanslarla erişilir.

8 primitive'in tam listesi, aralıkları ve varsayılan değerleri:

| Tip | Boyut | Aralık | Varsayılan |
|---|---|---|---|
| `byte` | 1 byte (8 bit) | −128 … +127 | 0 |
| `short` | 2 byte (16 bit) | −32,768 … +32,767 | 0 |
| `int` | 4 byte (32 bit) | ≈ ±2.1 milyar | 0 |
| `long` | 8 byte (64 bit) | ≈ ±9.2 × 10¹⁸ | 0L |
| `float` | 4 byte | ~7 basamak hassasiyet | 0.0f |
| `double` | 8 byte | ~15-16 basamak hassasiyet | 0.0d |
| `char` | 2 byte (16 bit UTF-16) | 0 … 65,535 | `'\u0000'` |
| `boolean` | JVM'e bağlı | `true` / `false` | `false` |

**Neden bu kadar çok tamsayı tipi var?** Çünkü her biri farklı miktarda bellek kaplar. Eğer sadece 0-100 arası bir değer tutacaksan `byte` yeter, her biri için 4 byte harcamak israf. Dev bir dizide milyonlarca eleman olduğunda bu fark ciddidir. Ama günümüzde bellek ucuzlayınca çoğu yerde alışkanlıktan `int` kullanırız. Küçük tiplerle uğraşmak çoğu durumda erken optimizasyondur.

**`float` vs `double`**: İkisi de kesirli sayı tutar ama double 2 kat daha hassastır. Java'nın varsayılan kesirli tipi `double`'dır. `float f = 3.14;` yazdığında derleyici kızar çünkü `3.14` literal'ı double'dır, daha küçük olan float'a otomatik sıkıştırılmaz. `float f = 3.14f;` demelisin.

**`char` bir tam sayıdır!** Java'da `char` aslında 16 bit'lik işaretsiz bir tamsayıdır. `'A'` yazdığında JVM bunu 65 değeri olarak saklar. Dolayısıyla `char c = 65;` yasaldır ve `c` harfi 'A' olur. Hatta şunu yapabilirsin: `int codeOfZ = 'Z';` → `codeOfZ = 90`.

**`boolean`ın boyutu garip**: Java spesifikasyonu `boolean`'ın kaç bit olduğunu **belirtmez**. Teorik olarak 1 bit yeterli ama çoğu JVM performans için 1 byte veya daha fazlasını kullanır. Bu yüzden tabloya "JVM'e bağlı" yazdım.

**Taşma (overflow) davranışı — mutlaka bil**: Java primitive aritmetiğinde taşma yaşandığında **hata atmaz**, sessizce sarar (wrap around). `Integer.MAX_VALUE + 1` → `Integer.MIN_VALUE`. Bu bir C mirasıdır ve finans/güvenlik kritik kodda başını ağrıtabilir. Güvenli toplama için `Math.addExact(a, b)` metodu `ArithmeticException` atar.

**Primitive vs Nesne — bellek hikayesi**: Primitive değişkenler **stack**'te, değer olarak durur. Yani `int a = 5;` dediğinde stack'te 4 byte'lık bir hücre açılır, içine 5 yazılır. Nesnelerde (az sonra) durum çok farklı.

---

## 7) Wrapper Sınıflar — Primitive'lerin Nesne Kıyafeti

Java zaman zaman primitive'leri nesneye çevirmek zorundadır. Mesela `List<int>` yazamazsın, çünkü generic'ler sadece nesne tipleri kabul eder. Bu yüzden her primitive'in bir **wrapper (sarmalayıcı)** sınıfı vardır:

| Primitive | Wrapper |
|---|---|
| `byte` | `Byte` |
| `short` | `Short` |
| `int` | `Integer` |
| `long` | `Long` |
| `float` | `Float` |
| `double` | `Double` |
| `char` | `Character` |
| `boolean` | `Boolean` |

**Autoboxing** (otomatik kutulama): `Integer x = 5;` yazdığında Java sessizce `Integer.valueOf(5)` çağırıp bir Integer nesnesi yaratır. Sen int verdin, Integer aldın.

**Unboxing** (kutuyu açma): `int y = x;` yazdığında Java `x.intValue()` çağırıp Integer'dan int çıkarır.

Çoğu zaman bu iki dönüşüm şeffaftır, fark etmezsin. Ama iki tehlikeli nokta var:

**1. NullPointerException**: `Integer maybe = null; int val = maybe;` → Unbox edilmeye çalışılan null referans → NPE. Wrapper'lar null olabilir, primitive'ler olamaz. İki dünya arasında geçiş yaparken null kontrolü mecbur.

**2. Integer cache tuzağı**: Java, `-128` ile `+127` arasındaki Integer nesnelerini **önbellekler**. Yani `Integer a = 100; Integer b = 100;` yazdığında `a == b` true döner (aynı nesneyi paylaşırlar). Ama `Integer a = 200; Integer b = 200;` dediğinde `a == b` **false**! Çünkü 200 cache dışında, her biri yeni nesne. Bu yüzden **wrapper'ları her zaman `.equals()` ile karşılaştır**.

Performans notu: Milyonlarca sayıyla çalışıyorsan `ArrayList<Integer>` yerine `int[]` veya `IntStream` düşün, autoboxing hem hafıza hem CPU maliyetli.

---

## 8) Literal'ler ve Sayı Sistemleri

Kodda doğrudan yazdığın sabit değerlere **literal** denir. Java farklı tabanlarda yazmayı destekler:

```java
int decimal = 100;             // normal 10'luk
int binary  = 0b0110_0100;     // 2'lik (0b ile başla)
int octal   = 0144;            // 8'lik (0 ile başla — dikkat!)
int hex     = 0x64;            // 16'lık (0x ile başla)
```

Dördü de aynı değeri (100) tutar. Binary yazarken `_` karakterini **okunabilirlik için** araya koyabilirsin; `0b0110_0100` ile `0b01100100` derleyici için aynı. Bu özellikle büyük sayılarda çok yardımcıdır: `1_000_000` yazmak `1000000`'dan okunaklıdır.

Long literal'ları sonuna `L` yaz (büyük veya küçük olur ama **büyük L tavsiye edilir** çünkü küçük `l` rakam 1'e çok benzer): `long big = 10_000_000_000L;`. Bu L olmazsa derleyici literal'i int olarak okur ve int'e sığmadığı için hata verir.

Float literal'leri `f` veya `F` eki alır: `float f = 3.14f;`. `double` için ek opsiyoneldir (`d`/`D`).

Char literal'leri tek tırnakta: `'A'`. Unicode escape ile de yazılabilir: `'\u0041'` (hex olarak A).

String literal'leri çift tırnakta: `"Merhaba"`. Tek tırnak **sadece tek karakter** için, çift tırnak her zaman String.

---

## 9) Type Casting — Tür Dönüşümü

Bir tipten diğerine geçmeye casting denir. İki türü var:

### a) Widening (Genişletme / Implicit)
Küçük tipten büyüğe gidiyorsan, bilgi kaybı riski yok. Derleyici sessizce kabul eder.

```
byte → short → int → long → float → double
```

```java
int i = 100;
long l = i;       // int 4 byte, long 8 byte — sorun yok
double d = l;     // long → double, otomatik
```

Dikkat: `long → float` geçişi widening sayılsa bile, 4 byte float 8 byte long'un **hassasiyetini** tutamayabilir. Yani bazı çok büyük long değerlerinde yuvarlama olabilir. Bu garip durumu bilmek yeter.

### b) Narrowing (Daraltma / Explicit)
Büyükten küçüğe geçerken bilgi kaybı olabilir, bu yüzden **sen onaylamak zorundasın** parantezle:

```java
double pi = 3.99;
int i = (int) pi;          // i = 3  (kesirli kısım ATILIR, yuvarlanmaz)

int big = 130;
byte b = (byte) big;       // b = -126
```

Son satır ilginç: 130 sayısı byte'ın aralığına (−128 … 127) sığmaz. 8 bit'e kırpılınca üst bit işaret bitine denk gelir ve sonuç negatif olur. Bu tamamen **matematiksel bir kesme işlemi**, programın hata atması değil.

### c) Nesne Casting
İleride göreceğimiz inheritance'la alakalı. Bir `Dog` nesnesini `Animal` olarak görmek **upcasting** (hep güvenli), bir `Animal` referansını tekrar `Dog` olarak görmek **downcasting** (yanlışsa `ClassCastException` atar).

---

## 10) `var` Anahtar Kelimesi (Java 10+)

Java uzun yıllar `int x = 5;` gibi açıkça tip yazmayı zorunlu tuttu. Java 10 ile **local değişkenlerde** tip çıkarımı (type inference) geldi:

```java
var name = "Ali";              // derleyici String olarak belirler
var list = new ArrayList<Integer>();   // ArrayList<Integer>
var price = 9.99;              // double
```

Bu **dinamik tip değil**. JavaScript veya Python'daki gibi değişkene sonradan başka tipte değer atayamazsın:

```java
var x = "merhaba";
x = 5;   // HATA: x zaten String olarak belirlendi
```

`var`'ın kısıtları:
- Sadece local değişkenlerde (method içinde) kullanılabilir. Field'larda olmaz.
- Başlatma zorunlu: `var x;` derlenmez çünkü çıkarım yapacak ipucu yok.
- Lambda parametrelerinde tek başına olmaz.
- `null` ile başlatılamaz: `var x = null;` — çıkarılabilir tip yok.

**Ne zaman kullan?** Tip adı çok uzun ve okumayı zorlaştırıyorsa: `var cache = new HashMap<String, List<UserProfile>>();` yerine sağ tarafı görünce anlarsın. Kısa tiplerde (`int`, `String`) okunabilirlik için `var` kullanmak tartışmalıdır, ekipte karar verin.

---

## 11) Değişken Kapsamı (Scope)

Bir değişkenin "yaşadığı" yere kapsam denir. Java'da üç ana seviye:

**Local değişken**: Bir metod veya blok içinde tanımlanır. Metod bittiğinde yok olur, stack'ten silinir. **Varsayılan değeri yoktur**, kullanmadan önce atamalısın:
```java
int x;
System.out.println(x);   // DERLEME HATASI: "variable x might not have been initialized"
```

**Instance field**: Sınıfın içinde, metod dışında tanımlanır. Her nesne için ayrı kopyası vardır, nesne yaşadığı sürece yaşar. **Varsayılan değeri vardır** (int=0, ref=null, boolean=false).

**Static (class) field**: `static` ile işaretlenir. Sınıfa aittir, tüm nesneler paylaşır. Sınıf ilk yüklendiğinde oluşur, uygulama boyunca yaşar.

Blok kapsamı da var: `if`, `for`, `{ }` bloklarında tanımlanan değişken blok dışında görünmez.

---

## Mülakat Sorularına Hazırlık

**1. `int` ile `Integer` arasındaki farklar nelerdir?**
`int` primitive, 4 byte, stack'te değer olarak durur, null olamaz, karşılaştırma `==` ile tutarlıdır, hızlıdır, generic'lerde kullanılamaz. `Integer` nesnedir, heap'te durur, referansı stack'te olur, null olabilir, `==` referans karşılaştırır bu yüzden `.equals()` gerekir, autoboxing ile primitive'den otomatik dönüşür, koleksiyonlarda kullanılır.

**2. Java derlenen mi yorumlanan mı bir dildir?**
İkisi de. Önce `javac` ile bytecode'a derlenir, sonra JVM bytecode'u **yorumlar** veya JIT ile native koda derler. Bu karma yaklaşım hem platform bağımsızlık hem performans sağlar.

**3. `float f = 3.14;` neden derleme hatası verir?**
`3.14` literal'i varsayılan olarak `double`'dır (8 byte). `float` 4 byte olduğu için otomatik (implicit) daraltma yapılmaz. Çözüm: `float f = 3.14f;` ya da `float f = (float) 3.14;`.

**4. JDK, JRE ve JVM arasındaki farklar?**
JVM bytecode'u çalıştıran sanal makine. JRE = JVM + standart kütüphaneler, Java uygulamasını çalıştırmak için yeterli. JDK = JRE + geliştirici araçları (javac, javadoc, jdb vs.), Java geliştirmek için gerekir. Kapsamaca: JDK ⊃ JRE ⊃ JVM.

**5. Java pass-by-value mi pass-by-reference mi?**
**Her zaman pass-by-value**. Primitive'lerde değer kopyalanır. Nesnelerde **referansın kopyası** geçer; yani iki taraf aynı heap nesnesini gösterir, nesnenin iç durumunu değiştirebilir ama referansın kendisini yeni bir nesneye bağlamak çağıran tarafı etkilemez. Detayını 4. pakette göreceğiz.

**6. JIT compiler nedir, ne işe yarar?**
Just-In-Time compiler, JVM'in çalışma anında bytecode'u native makine koduna derleyen bileşenidir. "Sıcak" (sık çalışan) metodları tespit eder, optimize ederek derler, bir sonraki çağrılarda interpreter yerine bu native kodu kullanır. Java'nın uzun vadede C++'a yakın hıza ulaşmasını sağlar.

**7. Autoboxing/unboxing nedir, performans etkisi var mı?**
Primitive ile wrapper arasındaki otomatik dönüşüm. `Integer x = 5;` autoboxing, `int y = x;` unboxing. Her dönüşüm yeni nesne üretimi veya method çağrısı demektir. Sıkı döngülerde (milyonlarca iterasyon) fark edilebilir maliyet oluşturur. Ayrıca null unboxing NPE atar.

**8. Integer cache nedir?**
Java, `-128 … +127` arasındaki Integer nesnelerini önbelleğe alır. `Integer.valueOf(100)` her çağrıldığında aynı nesneyi döner. Bu yüzden `Integer a = 100; Integer b = 100;` dendiğinde `a == b` **true**, ama 200 için false. Nesne karşılıştırmasını her zaman `.equals()` ile yap.

**9. `var` kullanmanın sınırları nelerdir?**
Sadece local değişkenlerde kullanılır (method içi). Field, parametre, dönüş tipi olamaz. Başlatılmadan tanımlanamaz (`var x;` yasak). `null` ile başlatılamaz. Lambda parametresinde tek başına olmaz. `var` runtime'da dinamik değildir, derleme zamanında sabit bir tipe dönüşür.
