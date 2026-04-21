# 14 — Garbage Collector (Çöp Toplayıcı)

C/C++ programcıları manuel olarak `malloc`/`free` veya `new`/`delete` ile belleği yönetirler. Bir ömür boyu en sık yaptıkları hatalar: memory leak (unutulan free), double-free (iki kere silme), dangling pointer (silinmiş belleği kullanma), segmentation fault (yasak belleğe erişim). Java bu gailenin çoğunu **Garbage Collector** ile ortadan kaldırdı: sen `new` ile nesne üret, GC uygun gördüğünde çöpü toplar. Ama GC otomatik olmasına rağmen nasıl çalıştığını bilmek zorunluluğudur — çünkü kötü yazılmış kod hâlâ memory leak yapar, GC pause'ları uygulamayı donuklatabilir, ve production'da "neden RAM sürekli artıyor?" sorusuna cevap vermen gerekir.

---

## 1) GC Nedir, Neden Var?

**Garbage Collection**: Bellekte artık kullanılmayan (erişilemez) nesneleri otomatik tespit edip temizleme süreci.

Java tasarımcılarının hedefleri:
- **Güvenlik**: segfault, dangling pointer gibi bellek hatalarını önlemek.
- **Productivity**: programcı "free" yerine iş koduyla ilgilensin.
- **Soundness**: bellek bugları compile-time/runtime'da erken yakalansın.

Bedeli: GC çalışırken belirli pause'lar, ek CPU maliyeti, bazı nesne üretimlerinde latency.

---

## 2) Reachability — Yaşıyor mu Ölü mü?

GC'nin en temel sorusu: **bu nesne hâlâ kullanılabilir mi?**

Cevap: "**GC Root**'lardan başlayarak bir referans zinciri ile ulaşılabiliyorsa yaşıyor, ulaşılamıyorsa ölü."

### GC Root'lar
Her zaman "yaşıyor" sayılan başlangıç noktaları:
- Çalışan thread'lerin **stack'lerindeki local değişkenler**.
- **Static field'lar** (sınıf metadatası üzerinden).
- **JNI (native)** referansları.
- **Senkronizasyon monitor'leri** tuttuğu nesneler.
- Bazı iç JVM nesneleri.

### Ulaşılabilirlik Grafı
Root'lardan → referanslar üzerinden dallanan bir graf. Graf'a dahil olanlar "**reachable**" (canlı). Graf'ta olmayanlar "**unreachable**" (ölü, toplanabilir).

```
[Thread stack] → Person p → Address addr
[Static field] → Cache    → Entry → Key, Value
[Thread stack] → (local var kayboldu)
                   ↑
                 heap'te Orphan nesne — UNREACHABLE → GC alır
```

Önemli: **Nesneler birbirini tutsalar bile** (çembersel referanslar), eğer bir root'tan ulaşılamıyorlarsa hepsi ölüdür. Modern GC'ler çembersel referansı çözer — "reference counting" gibi C++ shared_ptr'ın düştüğü tuzağa düşmez.

---

## 3) Generational Hypothesis (Nesil Hipotezi)

Ampirik gözlem: **"Nesnelerin çoğu genç ölür."**

Yaratılan nesnelerin büyük kısmı kısa ömürlüdür (metod lokalleri, temp objeler, lambda'lar). Az bir kısmı uzun yaşar (singleton, cache, uzun connection).

Bu gözleme dayanarak heap **nesillere** ayrılır ve her nesil farklı hızda taranır.

### Young Generation (Genç Nesil)
- Yeni nesneler burada doğar.
- Sık sık taranır (**Minor GC**) — hızlıdır, küçüktür.
- Alt bölümleri: **Eden** + **Survivor 0** + **Survivor 1**.

**Eden**: `new` ile üretilen nesne buraya düşer. Eden dolarsa Minor GC tetiklenir.

**Survivor S0 / S1**: Minor GC'den sağ kalanlar buraya taşınır (copying collection). Her GC'de S0↔S1 arası kopyalanır ve nesnenin "yaşı" artar. Belli yaşa gelen (genelde 15) **Old Gen**'e **tenured/promoted** olur.

### Old Generation (Yaşlı Nesil / Tenured)
- Uzun yaşayan nesneler.
- Daha seyrek taranır (**Major GC** veya **Full GC**).
- Pahalıdır — **Stop-The-World** (STW) etkisi yaratır: uygulama tamamen durur.

### Neden Bu Tasarım İyi?
- Çoğu nesne young'da ölür → bu bölgeyi hızlı tararsın (sadece yaşayanları kopyalarsın, ölüyü umursama).
- Old'u az tararsın → STW masrafı düşer.
- Young GC sıkça ama kısa, Old GC seyrek ama uzun → genel throughput yüksek.

---

## 4) Mark-Sweep-Compact — Temel Algoritma

Neredeyse tüm GC'lerin iskeleti:

**1. Mark**: Root'lardan başla, her ulaşılabilir nesneyi "live" işaretle.

**2. Sweep**: İşaretlenmemişleri sil, belleği geri al.

**3. Compact** (opsiyonel): Yaşayanları belleğin başına topla, fragmantasyonu önle.

Compact neden önemli? Sweep sonrası bellek "İsviçre peyniri" gibi delik deşik olur. Büyük bir nesne için ardışık yer bulmak zorlaşır (fragmentation). Compact bellek parçalarını sıkıştırır, ardışık boş alan yaratır.

---

## 5) GC Algoritmaları — JVM'de Hangi Seçenekler?

JVM farklı GC algoritmaları sunar, `-XX:+UseX GC` ile seçersin:

### Serial GC (`-XX:+UseSerialGC`)
- **Tek thread** ile çalışır.
- Küçük heap, tek CPU için.
- Minor/Major GC esnasında uzun pause.
- Klient uygulamalar, embedded sistemler.

### Parallel GC (`-XX:+UseParallelGC`)
- Çok thread'li GC.
- **Throughput** (iş çıktısı) öncelikli.
- Eskiden default (Java 8).
- Pause süresi uzun olabilir, ama toplam iş yüksek.

### CMS (Concurrent Mark-Sweep) — Deprecated
- Uygulamayla paralel çalışan mark aşamaları.
- Düşük pause için tasarlandı.
- Java 9'dan deprecated, Java 14'te kaldırıldı.
- Yerine G1 geldi.

### G1 GC (Garbage First) — Modern Default (Java 9+)
- Heap'i **region**'lara böler (1 MB - 32 MB).
- Her region Eden/Survivor/Old/Humongous olabilir.
- En çok çöp içeren region'ları önce tarar (ismin kaynağı).
- **Pause-time hedefi** verebilirsin: `-XX:MaxGCPauseMillis=200`.
- Predictable latency.
- Çoğu modern uygulama için iyi seçim.

### ZGC (Z Garbage Collector)
- Java 11+'da geliştirildi, Java 15'te production-ready.
- **Sub-millisecond pause**'lar (çoğunlukla <1ms).
- Çok büyük heap destekler (TB'ler).
- Colored pointers, load barriers kullanır.
- Büyük ölçekli, düşük latency gereken uygulamalar için.

### Shenandoah
- Red Hat tarafından geliştirildi.
- ZGC'ye benzer düşük pause hedefi.
- Java 12+ OpenJDK'ta var.

### Epsilon — "No-op GC"
- Hiçbir şey toplamaz.
- Sadece test/benchmark için.
- Kısa ömürlü programlar için heap büyük tutarak GC'ye gerek duymazsın.

---

## 6) G1 GC Detaylı — Modern Default

G1 heap'i küçük region'lara böler:

```
┌─────┬─────┬─────┬─────┐
│ E   │ S   │ O   │ E   │
├─────┼─────┼─────┼─────┤
│ O   │ E   │ H   │ O   │
├─────┼─────┼─────┼─────┤
│ S   │ E   │ O   │ E   │
└─────┴─────┴─────┴─────┘
E=Eden, S=Survivor, O=Old, H=Humongous
```

Region tipleri **dinamik** atanır. Çok büyük nesneler (region size'ın yarısından büyük) "Humongous" region'lara gider.

### G1 Döngüleri
**Young-only** phase: Sadece young region'larda Minor GC.

**Space-reclamation** phase: "Mixed GC" — hem young hem "en kötü" old region'ları tarar.

**Pause time hedefi**: `-XX:MaxGCPauseMillis=200` verirsen G1 her GC'nin 200ms'i geçmemesini hedefler. Garanti değil ama genelde tutar.

### G1 Ne Zaman?
- Orta-büyük heap (4+ GB).
- Düşük-orta latency hedefi.
- Modern Java (9+) default seçim.

### ZGC Ne Zaman?
- Çok büyük heap (10+ GB).
- Sıkı latency hedefleri (<10ms pause).
- Örnekler: oyun server'ları, real-time trading, yüksek-frekans veri işleme.

---

## 7) Reference Tipleri — `java.lang.ref`

Java'da 4 tür referans vardır, her biri GC davranışını etkiler:

### Strong Reference (Default)
Normal `MyClass obj = new MyClass();`. **Asla** otomatik GC olmaz. Tüm referanslar null olana dek yaşar.

### Soft Reference (`SoftReference`)
```java
SoftReference<MyClass> ref = new SoftReference<>(new MyClass());
MyClass m = ref.get();    // null olabilir!
```
GC bellek baskısı altındayken alabilir. Yani "cache'e uygun": normal zamanda tutulur, hafıza dar olunca feda edilir.

### Weak Reference (`WeakReference`)
```java
WeakReference<MyClass> ref = new WeakReference<>(new MyClass());
```
**Bir sonraki GC'de** alınır (başka strong ref yoksa). `WeakHashMap`'te key olarak kullanılır — key için strong ref kalmayınca entry otomatik silinir.

Kullanım: cache değil, "etiket" durumlarında — nesne yaşıyorsa meta-bilgi de olsun, yaşamıyorsa silinsin.

### Phantom Reference (`PhantomReference`)
```java
ReferenceQueue<MyClass> queue = new ReferenceQueue<>();
PhantomReference<MyClass> ref = new PhantomReference<>(obj, queue);
ref.get();   // HER ZAMAN null!
```
`get()` her zaman null döner. Nesne tamamen yok olduğunda queue'ya eklenir — "artık gerçekten temizlendi" bildirimi için. Finalize'ın modern alternatifi olarak `Cleaner` API'de kullanılır.

### Özet Tablo

| Tip | GC Ne Zaman Alır? | Kullanım |
|---|---|---|
| Strong | Asla (otomatik) | Varsayılan |
| Soft | Memory düşük | Cache |
| Weak | Bir sonraki GC | Etiket, WeakHashMap |
| Phantom | Tamamen temizlendikten sonra | Cleanup hook |

---

## 8) `finalize()` — Deprecated ve Tehlikeli

Eski Java'da nesne GC'den önce `finalize()` çağrılırdı. "Son temizlik şansı" gibi düşünüldü ama pratikte felaket:

- **Ne zaman çağrılacağı belirsiz** — belki hiç.
- **Hangi thread'de çağrılacağı belirsiz**.
- Finalize resurrection — finalize içinde kendine referans tutarak "dirilme". Yanıltıcı.
- Performans cezaları (iki GC cycle gerekiyor).
- Exception yutuluyor.

Java 9'dan itibaren **deprecated**. Asla kullanma.

### Modern Alternatifler
**a) `AutoCloseable` + try-with-resources** — deterministic cleanup.
```java
try (MyResource r = new MyResource()) { ... }   // close çağrılır
```

**b) `Cleaner` API (Java 9+)** — background thread ile cleanup, `finalize`'ın güvenli versiyonu:
```java
static final Cleaner CLEANER = Cleaner.create();
CLEANER.register(obj, () -> { /* cleanup */ });
```

---

## 9) Memory Leak'ler — GC Olsa Bile!

"GC var, leak olmaz" yanılgısı. GC sadece **erişilemez** nesneleri toplar. Hâlâ erişilebilen ama kullanılmayan nesneler birikir — işte bu leak'tir.

### Yaygın Sebepler

**a) Static Koleksiyonlar**
```java
static List<User> cache = new ArrayList<>();
// Bir metod add ediyor, remove unutuldu → sonsuza dek büyür
```

**b) Listener/Callback Deregister Edilmemesi**
```java
button.addActionListener(listener);
// Dispose edilen frame'de removeActionListener çağrılmazsa
// listener → frame → children referans zinciri tutulur
```

**c) `ThreadLocal` Temizlenmemesi**
Thread pool'da thread uzun yaşar. `threadLocal.set(...)` sonrası `remove()` yapılmazsa leak.

**d) Inner Class Outer Reference**
Non-static inner class gizli `this$0` field'ı ile outer'ı tutar. Inner yaşıyorsa outer GC olamaz. Büyük outer instance'lar için bela. Çözüm: `static nested class`.

**e) Unbounded Cache**
Cache'in maksimum boyutu yoksa sonsuza kadar büyür. `LinkedHashMap` + `removeEldestEntry` veya Caffeine kütüphanesi.

**f) Unclosed Resources**
Stream, Connection, Socket kapatılmazsa native handle sızıntısı olur. Try-with-resources kullan.

**g) ClassLoader Leak**
Webapp redeploy senaryosunda eski ClassLoader ve tüm sınıfları bellekte kalırsa Metaspace doluluğu. Servlet container'larda yaygın.

---

## 10) OutOfMemoryError Türleri

**`Java heap space`**: Heap doldu. Sızıntı, büyük data, limit düşük.

**`Metaspace`**: Sınıf metadatası doldu. ClassLoader leak, dinamik proxy abuse.

**`Direct buffer memory`**: NIO'nun native buffer'ları doldu (`ByteBuffer.allocateDirect`).

**`GC overhead limit exceeded`**: JVM zamanın %98'ini GC'de harcıyor ama %2'den az bellek alıyor. Yakın OOM.

**`Unable to create new native thread`**: OS sınırı, çok fazla thread.

**`StackOverflowError`**: Stack doldu (recursion).

---

## 11) GC Gözlemleme

### JVM Flags
```
-verbose:gc                           # basit GC log
-Xlog:gc*:file=gc.log                 # Java 9+ detailed
-XX:+PrintGCDetails                   # eski, Java 8
-XX:+HeapDumpOnOutOfMemoryError       # OOM'da dump
-XX:HeapDumpPath=/tmp/heap.hprof
```

### Araçlar
- **JVisualVM** — GUI, heap/CPU profiling.
- **JConsole** — basit monitoring.
- **Java Mission Control (JMC)** — Oracle'dan, detaylı.
- **Eclipse MAT (Memory Analyzer)** — heap dump analizi.
- **jmap** — CLI heap dump.
- **jstat** — GC istatistikleri.
- **async-profiler** — düşük overhead profiling.

### `System.gc()` — Rica Et, Garanti Yok
```java
System.gc();   // JVM dikkate almayabilir
```
Production'da **asla** çağırma. JVM kendi bilir. `System.gc()` çağırınca genelde Full GC tetiklenir — gereksiz pause.

---

## 12) GC Tuning — İpuçları

**1. Önce ölç, sonra tune et.** Varsayımla ayar bozma.

**2. Heap'i doğru boyutlandır.**
```
-Xms4g -Xmx4g     # başlangıç ve maks aynı → resize maliyeti yok
```

**3. GC seçimi uygulamaya göre.**
- Latency-critical → G1 veya ZGC.
- Throughput öncelik → Parallel GC.
- Küçük uygulama → Serial.

**4. Short-lived obje yoğunsa Young'u büyüt:**
```
-XX:NewRatio=1     # young:old oranı
-XX:MaxTenuringThreshold=10
```

**5. Allocation rate'i düşür.** Nesne yaratmayı azalt — nesne pool, primitive collections, string builder reuse.

**6. Escape analysis**i engelleme. `final`, kısa metodlar, inline-friendly kod.

---

## Mülakat Soruları ve Örnek Cevaplar

**1. GC ne zaman çalışır? Garanti var mı?**
Belirli bir zamanlama garantisi yok. JVM heap doluluğuna, nesne yaşına, alloc rate'e göre karar verir. `System.gc()` sadece bir "rica"dır, tutulmayabilir. Production'da GC'ye güvenme ama döngünü bozma.

**2. Generational GC neden iyidir?**
"Nesnelerin çoğu genç ölür" hipotezine dayanır. Young generation sık ama küçük — minor GC hızlı. Old generation seyrek tarama — pause süresi düşük. Bu sayede toplam throughput yüksek, latency öngörülebilir.

**3. Minor GC ile Major GC farkı?**
Minor GC sadece young generation'ı tarar — hızlı, sık. Major GC (Full GC) tüm heap'i tarar — yavaş, STW uzun sürer. Modern G1 "mixed GC" gibi ara versiyonlar ekledi.

**4. Strong/Soft/Weak/Phantom referans farkı?**
Strong: varsayılan, asla otomatik alınmaz. Soft: memory baskısında alınır, cache için. Weak: bir sonraki GC'de alınır, WeakHashMap kullanımı. Phantom: tamamen temizlendikten sonra queue'ya eklenir, cleanup hook için.

**5. Java'da memory leak örneği?**
Static koleksiyona sürekli ekleme + remove unutma, de-register edilmeyen listener, temizlenmeyen ThreadLocal (thread pool'da), unclosed resource, unbounded cache, inner class'ın outer referansı, webapp redeploy'da eski classloader.

**6. `finalize()` neden deprecated?**
Belirsiz çalışma zamanı, performans cezası, resurrection sorunu, exception yutma, thread belirsizliği. Java 9+'dan beri deprecated. Yerine AutoCloseable + try-with-resources veya Cleaner API.

**7. G1 ve ZGC farkı?**
G1: region-based, pause-time hedefi (~200ms), orta heap için ideal, Java 9+ default. ZGC: colored pointers, sub-ms pause, çok büyük heap destekler (TB), düşük-latency kritik uygulamalar için. G1 çoğu durum için yeter, ZGC özel ihtiyaç.

**8. Stop-the-world nedir?**
GC çalışırken tüm uygulama thread'lerinin durdurulması. Mark phase güvenliği için gerek. Modern GC'ler (G1, ZGC) STW süresini minimize etmeye çalışır ama tamamen kaldıramaz.

**9. Memory leak'i nasıl tespit ederim?**
JVisualVM/JMC ile heap büyümesini izle, OOM'da heap dump al (`-XX:+HeapDumpOnOutOfMemoryError`), Eclipse MAT ile analiz et — "Dominator Tree" en çok bellek tutan nesneleri gösterir. Referans zincirini takip ederek "hangi root bu nesneyi tutuyor?" sorusunu cevapla.

**10. Escape Analysis nedir?**
JIT'in bir nesnenin metod dışına kaçmadığını tespit edip stack allocation yapması. GC masrafı düşer, synchronization eliminate edilebilir. Programcı doğrudan kontrol etmez ama kısa metod, final field gibi pratiklerle kolaylaştırabilir.

**11. `System.gc()` çağırmak zararlı mı?**
Genelde evet. Gereksiz Full GC tetikler, uzun pause yaratır. JVM kendi zaten optimize eder. Sadece hassas benchmark/test senaryolarında düşünülebilir. Production kodunda yeri yok.

**12. Heap dump nasıl alınır, ne için kullanılır?**
`-XX:+HeapDumpOnOutOfMemoryError` ile OOM'da otomatik. Manuel: `jmap -dump:format=b,file=x.hprof <pid>`. `.hprof` dosyası Eclipse MAT veya JVisualVM ile açılır. Memory leak analizi, büyük nesne tespiti, class loader analizi için.

**13. `-Xmx` ve `-Xms` nedir, aynı vermek niye iyi?**
`-Xms` başlangıç heap, `-Xmx` maks heap. Farklı verirsen JVM heap'i dinamik büyütür/küçültür — runtime overhead. Aynı verirsen en başta tam boyutta tutar — production'da yaygın pratik, predictability sağlar.
