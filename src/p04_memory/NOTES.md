# 04 — JVM Bellek Modeli: Stack, Heap, Metaspace

Bu bölüm Java mülakatlarının kalbidir. Hangi şirket olursa olsun, orta-üst seviye pozisyonlarda "Stack ile Heap arasındaki farkı anlatır mısınız?" sorusu neredeyse garantidir. Cevabı ezberlenmiş olmaktan çok **bellekte neyin nerede durduğunu zihninde gerçekten görebilmelisin**. Bu yüzden bu bölümü zaman ayırıp iyice sindir.

---

## 1) Genel Resim — JVM Belleği Kuş Bakışı

Bir Java programı çalışmaya başladığında JVM, işletim sisteminden bir miktar bellek ister ve bu belleği **mantıksal bölgelere** ayırır. Her bölge farklı tipte veriyi, farklı ömürlerde tutar:

```
  ┌──────────────────────────────────────────────┐
  │               METASPACE                      │  → Sınıf metadatası (Java 8+)
  │  (Java 8 öncesi buraya PermGen deniyordu)    │    Metodların bytecode'u
  │                                              │    Static field'ların saklama yeri
  ├──────────────────────────────────────────────┤
  │                 HEAP                         │  → Tüm nesneler
  │  ┌─────────────┬───────────────┐             │    String pool
  │  │ Young Gen   │ Old Gen       │             │    GC tarafından yönetilir
  │  └─────────────┴───────────────┘             │
  ├──────────────────────────────────────────────┤
  │              JVM STACK                       │  → Her THREAD için 1 tane
  │  ┌─────┐                                     │    Method frame'leri
  │  │ main│                                     │    Local değişkenler
  │  │frame│                                     │    Referanslar (değil, nesneler!)
  │  ├─────┤                                     │
  │  │calc │                                     │
  │  │frame│                                     │
  │  └─────┘                                     │
  ├──────────────────────────────────────────────┤
  │            PC REGISTER                       │  → Her thread'in şu anki bytecode işaretçisi
  ├──────────────────────────────────────────────┤
  │         NATIVE METHOD STACK                  │  → JNI (C/C++) çağrıları için
  └──────────────────────────────────────────────┘
```

Bu bölgelerin herbirinin kendi hayat döngüsü, kendi kuralları var. Hadi tek tek ele alalım.

---

## 2) Heap — Nesnelerin Evi

Heap, Java'nın en büyük ve en önemli bellek bölgesidir. `new` anahtar sözcüğüyle oluşturduğun her şey burada doğar: `new ArrayList<>()`, `new Person("Ali")`, `new int[1000]`... hepsi heap'te.

**Heap'in temel özellikleri:**
- **Tüm thread'ler tarafından paylaşılır.** Bir thread'in yarattığı nesneye başka bir thread de (referansı alırsa) erişebilir. Bu hem güçtür hem bela — senkronizasyon ihtiyacının kaynağı.
- **Dinamik olarak büyür/küçülür** (sınırlar içinde). JVM başlatılırken `-Xms` (başlangıç) ve `-Xmx` (maksimum) ile sınırlarsın.
- **Garbage Collector'ın çalışma sahasıdır.** GC heap'i tarar, ulaşılmayan nesneleri temizler.
- **String pool burada tutulur** (Java 7+).

Heap iki ana bölüme ayrılır — bu ayrım GC performansının sırrıdır:

### Young Generation (Genç Nesil)
Yeni doğan nesneler buraya gelir. Alt bölümleri:
- **Eden**: Bebek odası. `new` ile üretilen her nesne buraya düşer.
- **Survivor S0 ve S1**: Bir Minor GC'den sağ çıkan nesneler S0 ya da S1'e taşınır. Birkaç GC daha atlattıktan sonra "yaşlı" sayılıp Old Generation'a terfi eder (tenuring).

Young Generation'daki toplama işlemine **Minor GC** denir, hızlıdır ve sık çalışır.

### Old Generation (Yaşlı Nesil / Tenured)
Uzun yaşayan nesneler buraya gelir. Cache'ler, singleton'lar, uzun ömürlü bağlantılar... Burayı temizlemeye **Major GC** ya da **Full GC** denir, pahalıdır ve **Stop-The-World** (uygulama durur) etkisi yaratır.

**Jenerasyonel hipotez** bu tasarımın felsefesidir: "Nesnelerin çoğu genç ölür." Ampirik olarak bu hipotez doğru çıkar. Çoğu nesne yaratıldığı metod bittiğinde zaten kullanılmaz. O halde onları kolay ve hızlı temizleyebileceğimiz bir bölgede tutmak mantıklı. Sadece "yaşayan uzun yaşar" örneği nesneleri Old Gen'e taşıyıp orada daha seyrek tararız.

---

## 3) Stack — Metodların Geçici Not Defteri

Stack (yığın), **her thread'e özel** ayrılmış bir bellek bölgesidir. Metod çağrıları ve bunların local değişkenleri burada tutulur.

**Stack nasıl çalışır?** Bir metod çağrıldığında stack'in üstüne yeni bir **frame** (çerçeve) konur. Bu frame şunları içerir:
- Metodun local değişkenleri (primitive'ler değer olarak, nesne referansları adres olarak)
- Parametreler
- Metodun geri döneceği yerin bilgisi (return address)
- Ara hesaplamalar için operand stack

Metod bittiğinde o frame **pop** edilir, bellek anında geri alınır. GC gibi bir zamanlamaya gerek yok — LIFO (son giren ilk çıkar) düzeni.

```java
void a() {
    int x = 1;
    b();       // b çağrıldığında stack'e yeni frame eklenir
}              // a bittiğinde frame'i pop olur

void b() {
    int y = 2;
    // burada stack: [main frame][a frame][b frame]
}
```

**Stack'in sınırı vardır.** Default ~512 KB veya 1 MB civarı (JVM ve OS'a göre değişir). Aşarsan `StackOverflowError` alırsın. Bunun en yaygın nedeni **recursion** (kendini çağıran metod). Her recursive çağrı yeni frame ekler; çıkış koşulun yanlışsa veya çok derinse stack dolar.

```java
void infinite() {
    infinite();    // yeni frame, yeni frame, yeni frame... StackOverflowError!
}
```

**Thread-safety'nin doğal garantisi**: Her thread'in kendi stack'i olduğu için, stack'teki local değişkenler **hiçbir zaman başka thread tarafından görülmez**. Yani method içindeki local değişkenler otomatik thread-safe'dir.

---

## 4) Metaspace — Sınıf Hafızası

Her `.class` dosyası JVM tarafından yüklendiğinde, o sınıfın "profili" Metaspace'e konur:
- Sınıfın adı, paketi
- Alanların ve metodların listesi (imzalar)
- Method bytecode'u
- Constant pool (literal'ler, referanslar)
- Static field'ların saklama yeri (Java 8+'da static field'lar aslında heap'te tutulur, Metaspace bunlara referans tutar)

**Java 8 öncesi bu alan "PermGen" idi** ve **heap'in içindeydi**, sabit boyutluydu. Büyük uygulamalarda `OutOfMemoryError: PermGen space` korkulu rüyaydı.

**Java 8 ile Metaspace geldi** ve **native bellekte** (heap dışında) tutulur. Varsayılan olarak sınırsızdır (OS bellek izin verdiği sürece büyür). `-XX:MaxMetaspaceSize` ile sınırlayabilirsin.

**Metaspace dolabilir mi?** Evet! Özellikle:
- Uygulama çok fazla sınıf yüklüyorsa (reflection, dinamik proxy'ler).
- ClassLoader sızıntısı varsa (web uygulamaları redeploy edildiğinde eski classloader GC olmazsa biriken sınıflar Metaspace'i şişirir).

---

## 5) Konkrete Örnek — Neyin Nerede Durduğunu Gör

Kısa bir kod parçası:

```java
public class Demo {
    static int counter = 0;           // static → Metaspace referansı + heap'te saklama
    int instanceId;                    // instance field → heap (nesne içinde)

    public static void main(String[] args) {
        int x = 10;                    // primitive local → main'in stack frame'i
        String s = "merhaba";          // referans stack'te, "merhaba" string pool'unda
        Person p = new Person("Ali");  // referans stack'te, nesne heap'te, "Ali" pool'da
        counter++;                     // Metaspace'deki counter'a gider
    }
}
```

Zihninde canlandır: `main` başladığında main'in stack frame'i kurulur. İçinde `x = 10` (primitive, 4 byte doğrudan yazılır), `s` (adres), `p` (adres) var. Heap'te `Person` nesnesi oluşur, içinde `name = "Ali"` şeklinde bir alan var ve bu alan string pool'daki "Ali"ye işaret ediyor.

`main` bittiğinde stack frame'i pop olur. `x`, `s`, `p` referansları kaybolur. Heap'teki Person nesnesine artık hiçbir referans yok — GC bir sonraki çalıştığında temizler. "Ali" literal'i pool'da yaşamaya devam eder (pool nesneleri farklı kuralarla temizlenir).

---

## 6) Static Alan — Sınıfa Ait Durum

`static` ile işaretlenen field/method bir **nesneye** değil, **sınıfa** aittir. Sınıf ilk yüklendiğinde bir kere oluşturulur, tüm nesneler (varsa) onu paylaşır.

```java
public class Counter {
    static int total = 0;       // bütün Counter'lar paylaşır
    int id;                     // her Counter'ın kendine özel
    
    public Counter() {
        total++;
        this.id = total;
    }
}
```

Her `new Counter()` çağrısı `total`'ı artırır ve o anki değeri `id`'ye atar. Yani nesnelerin `id`'leri 1, 2, 3 olur ama `total` hep son değeri tutar.

Static metodlar da sınıfa aittir:
```java
Math.max(3, 5);           // Math nesnesi yaratmadan çağırıyoruz
```

**Static initializer blok** — sınıf ilk yüklendiğinde bir kere çalışır:
```java
static {
    System.out.println("Sınıf yüklendi");
    // karmaşık static başlatma
}
```

**Static'in tuzakları:**
- Static alan paylaşılan state'tir. İki thread aynı anda yazarsa race condition olur.
- Static'in ömrü uygulama ömrüdür — sızıntı kaynağı olabilir (static `Map<String, Object>`'e sürekli ekleyip silmezsen bellek dolar).

---

## 7) `final` Anahtar Sözcüğü

`final` üç ayrı yerde kullanılır, her birinin anlamı farklıdır:

**a) Değişkende — bir kere atanır, değişmez.**
```java
final int MAX = 100;
MAX = 200;   // HATA
```

**b) Metodda — override edilemez.**
Alt sınıflar bu metodun davranışını değiştiremez. `String.equals` gibi kritik metodlar final'dır.

**c) Sınıfta — extend edilemez.**
`String`, `Integer` gibi sınıflar final'dır. Kimse `MyString extends String` yazamaz.

**Büyük tuzak**: `final` bir referans değişkende "**referans değişmez, nesnenin içi değişebilir**" anlamına gelir:
```java
final List<Integer> list = new ArrayList<>();
list.add(1);                   // OK, listenin içi değişiyor, referans değil
list.add(2);                   // OK
list = new ArrayList<>();      // HATA — referansı değiştiremeyiz
```

Yani `final` **shallow immutability** verir. Gerçek immutability için hem referans hem iç duruma dikkat etmelisin (9. pakete bak).

---

## 8) Pass-by-Value — Java'nın En Çok Yanlış Anlaşılan Konusu

**Java her zaman pass-by-value'dır.** Bu bir görüş değil, dil spesifikasyonunun kesin kuralıdır. Ama insanlar sürekli yanlış anlar çünkü nesnelerde davranış kafa karıştırıcıdır.

### Primitive'lerde
```java
void inc(int x) {
    x = x + 1;
}

int a = 5;
inc(a);
System.out.println(a);   // 5 (değişmedi)
```
`inc`'e `a`'nın **kopyası** geçti. `x` stack'te ayrı bir hücre, onu değiştirmek `a`'yı etkilemez.

### Nesnelerde
```java
void rename(Person p) {
    p.name = "Yeni";
}

Person ali = new Person("Ali");
rename(ali);
System.out.println(ali.name);   // "Yeni"  — Ne oldu?!
```

Burada da pass-by-value geçerlidir. Metoda **referansın kopyası** geçti. Hem `ali` hem `p` aynı heap nesnesini gösteriyor. `p.name = "Yeni"` yaptığında heap'teki o nesnenin içini değiştirdin — çağıran da aynı nesneyi gördüğü için değişimi görüyor.

Ama şunu yaparsan durum farklı:
```java
void reassign(Person p) {
    p = new Person("Başka");   // YENİ bir nesne yarat, p'yi ona bağla
}

Person ali = new Person("Ali");
reassign(ali);
System.out.println(ali.name);   // "Ali" — değişmedi!
```

Neden? Çünkü `p` local bir değişkendi (kopyadır), onu yeni nesneye bağladın ama çağıran tarafın `ali` referansı hâlâ eski nesneyi gösteriyor.

**Kısaca**: Nesnelerde **içeriği değiştirebilirsin**, ama **referansı yeniden bağlayamazsın** (çağıranın gördüğü referansı değil, sadece kendi local kopyanı bağlarsın).

C++'taki `Person& p` (reference parameter) Java'da yoktur, taklit etmek için wrapper sınıflar veya `AtomicReference` kullanılır.

---

## 9) Escape Analysis ve Stack Allocation

Gelişmiş bir JVM optimizasyonu: **Escape Analysis**. JVM, bir nesnenin yaratıldığı metodun **dışına kaçmadığını** (başka yere referans verilmediğini) tespit ederse, o nesneyi **heap yerine stack'te** ayırabilir. Bu sayede:
- Nesne metod bittiğinde otomatik silinir (GC masrafı yok).
- Cache performansı daha iyi.
- Bazı senkronizasyon kilitleri bile silinir (lock elision).

Bu senin kontrolünde değil, JIT otomatik yapar. Ama bilmen iyi çünkü "her new heap'e gider" sanılır, bazen gerçekte stack'e gidebilir.

---

## 10) OutOfMemoryError Türleri

JVM'de bellek yetersizliği çeşitli şekillerde patlayabilir:

**`OutOfMemoryError: Java heap space`**
Heap doldu. En yaygın sebep: bellek sızıntısı, büyük dizi/liste yaratma, cache'i sınırsız büyütme. `-Xmx` ile heap'i büyütebilirsin ama asıl çözüm kodu düzeltmek.

**`OutOfMemoryError: Metaspace`**
Çok fazla sınıf yüklendi. Sebep: dinamik classloader sızıntısı, reflection/proxy framework'lerinde sürekli yeni sınıf üretme.

**`StackOverflowError`**
Recursion kontrolden çıktı veya çok derine gitti. `-Xss` ile stack boyutunu büyütebilirsin ama mantıksal hata varsa sadece ertelersin.

**`OutOfMemoryError: GC overhead limit exceeded`**
JVM, zamanın %98'ini GC'ye harcıyor ama %2'den az heap geri alabiliyor. Yani çalışıyor ama hiçbir iş yapamıyor. Genelde yakın felaketin göstergesidir.

**`OutOfMemoryError: Direct buffer memory`**
NIO'daki `ByteBuffer.allocateDirect(...)` native bellek alır, bu da sınırlı.

---

## 11) Java'da Bellek Sızıntısı? Evet, Mümkün!

"Garbage Collector var, sızıntı olmaz" yanılgısı. **GC sadece erişilemez nesneleri toplar**. Hâlâ erişilebilen ama kullanılmayan nesneler birikir ve sızıntıya dönüşür. Yaygın sebepler:

**a) Static koleksiyonlara sürekli ekleme:**
```java
static List<User> cache = new ArrayList<>();
// Bir metod sürekli ekliyor ama asla silmiyor → sonsuza dek büyür
```

**b) Listener / Callback'leri de-register etmeme:**
```java
button.addClickListener(myListener);
// Bitişte removeClickListener unutulursa button myListener'ı tutar, o da parent'ı tutar...
```

**c) `ThreadLocal` temizlememe:**
Thread pool ortamında ölümcüldür. Thread asla bitmez, `ThreadLocal` temizlenmez, birikir. `try/finally` ile `threadLocal.remove()` çağır.

**d) Inner class'lar outer reference'ı tutar:**
Non-static inner class, outer'ın gizli bir referansını tutar. Inner class yaşıyorken outer GC olamaz. Eğer outer büyükse bela. Çözüm: `static` nested class kullan.

**e) Bağlantıların (connection, stream, socket) kapatılmaması.**
`try-with-resources` kullan (10. pakete bak).

**f) Büyümeye izin verilen sınırsız cache.**
Her cache'in TTL'si veya maksimum boyutu olmalı. `LinkedHashMap` + `removeEldestEntry` ile LRU yapabilirsin, ya da Caffeine gibi bir kütüphane kullan.

---

## 12) JVM Bellek Parametreleri (Sık Kullanılanlar)

JVM'i çalıştırırken bellek davranışını kontrol eden flag'ler:

```
-Xms512m                        Minimum heap
-Xmx2g                          Maksimum heap
-Xss1m                          Her thread'in stack boyutu
-XX:MaxMetaspaceSize=256m       Metaspace üst sınırı
-XX:+UseG1GC                    G1 çöp toplayıcısı (Java 9+ default)
-XX:+HeapDumpOnOutOfMemoryError OOM'da hafıza dump'ı al
-XX:HeapDumpPath=/tmp/heap.hprof  Dump dosyası yeri
-Xlog:gc*                       GC loglarını yazdır (Java 9+)
```

---

## Mülakat Soruları ve Örnek Cevaplar

**1. Stack ile Heap arasındaki fark nedir?**
Stack her thread'e özel, method frame'lerini ve local değişkenleri (primitive'leri değer, nesne referanslarını adres olarak) tutar; LIFO düzeninde, metod bittiğinde otomatik temizlenir; hızlı ama sınırlı boyuttadır ve aşılırsa StackOverflowError olur. Heap tüm thread'ler tarafından paylaşılır, `new` ile üretilen nesneler burada yaşar, Garbage Collector tarafından yönetilir, dinamik boyuttadır.

**2. String'ler nerede saklanır?**
String literal'leri String Pool'da (Java 7'den itibaren heap'in içinde özel bir bölüm). `new String(...)` ile üretilenler normal heap'te. `intern()` ile pool'dakini döndürürsün.

**3. Metaspace ile PermGen arasındaki fark?**
PermGen heap içinde, sabit boyutlu, Java 7 ve öncesinde kullanılıyordu — kolayca doluyordu. Metaspace native bellekte (heap dışı), varsayılan olarak sınırsız (OS izin verdiği kadar), Java 8+'da PermGen'in yerini aldı.

**4. Java pass-by-reference mı pass-by-value mı?**
Her zaman pass-by-value. Primitive'lerde değerin kopyası, nesnelerde referansın kopyası geçer. Referans kopyası aynı heap nesnesini gösterdiği için nesnenin iç durumunu değiştirebilirsin, ama çağıranın referansını yeni bir nesneye bağlayamazsın.

**5. Static field nerede tutulur?**
Java 8+'da static field'ların kendisi heap'te, Metaspace'deki sınıf metadatası tarafından referanslanır. Java 7 öncesinde PermGen'de idi. Mantıksal olarak "sınıfa ait" şeklinde düşün.

**6. `final` bir referans ile immutability aynı şey mi?**
Hayır. `final` referans referansın değişmezliğini garanti eder ama nesnenin içeriği (mutable field'lar) değişebilir. `final List<Integer> list = new ArrayList<>(); list.add(1);` geçerlidir. Gerçek immutability için nesnenin kendi alanlarının da değişmez olması gerekir.

**7. `OutOfMemoryError` türleri?**
`Java heap space`, `Metaspace`, `GC overhead limit exceeded`, `Direct buffer memory`, `unable to create new native thread`. `StackOverflowError` (Error ama OOM değil) recursion için.

**8. Java'da bellek sızıntısı örneği?**
Static koleksiyona sürekli ekleme, deregister edilmemiş listener'lar, ThreadLocal temizlememek, static `Map` içinde ClassLoader referansı, kapatılmamış connection/stream.

**9. Escape Analysis nedir?**
JIT'in bir nesnenin metod dışına kaçmadığını tespit edip onu stack'te ayırması. GC masrafını ve kilit maliyetini düşüren bir optimizasyon. Programcı elle kontrol etmez.

**10. `-Xmx` ve `-Xms` ne yapar?**
`-Xms` başlangıç heap boyutu, `-Xmx` maksimum heap boyutu. İkisini eşit vermek JVM'in sürekli resize yapmasını engeller — production'da yaygın pratik.
