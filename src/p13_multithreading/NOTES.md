# 13 — Multithreading ve Concurrency

Bu paket Java'nın en derin konularından biridir. Yanlış yazılmış concurrent kod, aylarca fark edilmeyen, reproduce edilemeyen, gizemli bug'lar üretir. Doğru yazılmış concurrent kod ise modern CPU'ların gücünü sonuna kadar kullanır, milisaniyelerde binlerce işi paralel yürütür. Bu paketi hem teorik (JMM, happens-before) hem pratik (Thread API, Executor, CompletableFuture) açıdan derinlemesine anlatacağım. Sabırla ilerle — mülakatta bu konular hak ettiğinden çok sık sorulur.

---

## 1) Process ve Thread — Temel Kavramlar

**Process** bir programın çalışan örneğidir. İşletim sistemi tarafından yönetilir, kendi bağımsız bellek alanı vardır. Chrome'un bir process'i var, Spotify'ın başka. Biri çökerse diğeri etkilenmez.

**Thread** bir process içindeki icra birimidir. Aynı process'in thread'leri **aynı belleği paylaşır** (heap) ama **kendi stack'leri** vardır. Bu hem güçtür (veri paylaşımı kolay) hem beladır (senkronizasyon gerek).

Bir Java uygulaması başladığında JVM en az şu thread'leri yaratır:
- **main** — senin `public static void main` çalıştığı.
- **GC thread(ler)** — Garbage Collector arka planda.
- **Finalizer** (artık obsolete).
- **Compiler threads** — JIT arka planda derler.
- **Signal dispatcher** — OS sinyallerini işler.

`Thread.getAllStackTraces()` ile hepsini listeleyebilirsin.

### Neden Multithreading?

**a) Paralelizm**: Modern CPU'lar çok çekirdekli (4, 8, 16 çekirdek). Tek thread bu gücün sadece 1/N'ini kullanır.

**b) Asenkronluk / responsiveness**: UI thread bir ağ isteği beklemesin, başka thread yapsın. Kullanıcı donmuş arayüz görmesin.

**c) I/O paralelliği**: Web server bir request beklerken CPU boşta. 1000 istemciye 1000 thread atayıp hepsini paralel işle.

---

## 2) Thread Yaratma — 3 Ana Yol

### a) `Thread` Sınıfından Extend Etmek
```java
class MyThread extends Thread {
    @Override public void run() {
        System.out.println("thread çalışıyor");
    }
}
new MyThread().start();
```
Pratik değil çünkü **tek kalıtım** kuralı gereği başka bir sınıftan türeyemezsin. Nadiren kullanılır.

### b) `Runnable` Implement Etmek (Tercih Edilen)
```java
Runnable task = () -> System.out.println("görev");
Thread t = new Thread(task);
t.start();
```
- Kalıtım kuralını ezmez (`Runnable` interface'tir, extends değil implements).
- `Runnable` functional interface — lambda ile yaz.
- Aynı `Runnable`'ı birden fazla thread'de kullanabilirsin.

### c) `Callable` + `ExecutorService` (Sonuç Döndüren)
```java
Callable<Integer> task = () -> 42;
ExecutorService exec = Executors.newFixedThreadPool(2);
Future<Integer> future = exec.submit(task);
Integer result = future.get();   // bloklanır, sonucu bekler
```
Modern Java'da en tercih edilen. Sonuç döndürmek, exception fırlatmak, thread pool avantajı.

### `start()` vs `run()` — Tuzak
```java
new Thread(r).start();   // YENİ thread'de çalışır
new Thread(r).run();     // MEVCUT thread'de çalışır, normal method çağrısı!
```
`start()` JVM'e yeni OS thread yaratmasını söyler. `run()` doğrudan çağrılırsa parallelism yoktur, sıradan metoddur.

---

## 3) Thread Yaşam Döngüsü

```
NEW → RUNNABLE → {BLOCKED, WAITING, TIMED_WAITING} → RUNNABLE → TERMINATED
```

**NEW**: `new Thread(r)` yaptın ama `start()` çağırmadın henüz.

**RUNNABLE**: `start()` çağrıldı. Ya CPU'da çalışıyor ya scheduling için bekliyor.

**BLOCKED**: Bir **monitor lock** (synchronized) için bekliyor.

**WAITING**: Süresiz bekliyor — `wait()`, `join()`, `LockSupport.park()`.

**TIMED_WAITING**: Belirli süre bekliyor — `sleep(ms)`, `wait(ms)`, `join(ms)`.

**TERMINATED**: `run` bitti veya exception fırlattı.

`thread.getState()` ile anlık durumu görebilirsin.

---

## 4) Temel Thread Metotları

```java
thread.start()           // thread'i başlatır (1 kere)
Thread.sleep(ms)         // mevcut thread'i uyutur
thread.join()            // bu thread bitene kadar bekle
thread.join(ms)          // en fazla ms kadar bekle
Thread.yield()           // "başka thread'e sıra veriyim" ipucu
thread.interrupt()       // kibar "dur" sinyali
Thread.currentThread()   // şu anki thread
thread.isAlive()         // çalışıyor mu?
thread.setName("x")
thread.setDaemon(true)   // daemon: JVM bu bitmese bile kapanır
thread.setPriority(n)    // 1-10, öneri; OS bağlayıcı değil
```

### Interrupt Mekanizması — Kibarca Durma
Thread'i **zorla durdurmanın güvenli yolu yok**. `Thread.stop()` deprecated ve tehlikeli. Onun yerine **cooperative shutdown**:

```java
Thread worker = new Thread(() -> {
    while (!Thread.currentThread().isInterrupted()) {
        doWork();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();   // bayrağı tekrar set et
            return;                                // çıkış
        }
    }
});
worker.start();
// ...
worker.interrupt();   // bitmesini iste
worker.join();
```

`interrupt()` bir **bayrak** setler. Thread kendi kendine kontrol edip çıkar. Blocking metodlar (sleep, wait, join, io) `InterruptedException` atarak anında uyanır.

---

## 5) Race Condition — Paylaşılan Veri Tehlikesi

Birden fazla thread aynı veriyi aynı anda değiştirirse tutarsız sonuç çıkar.

Klasik örnek: `counter++`
```java
static int counter = 0;

// 10 thread, her biri 100_000 kere
Runnable inc = () -> {
    for (int i = 0; i < 100_000; i++) counter++;
};
```

Beklenen: 1_000_000. Gerçek: sık sık daha az (mesela 834,562).

**Neden?** `counter++` aslında **üç adım**dır:
1. Belleği oku (`int tmp = counter`).
2. Bir arttır (`tmp = tmp + 1`).
3. Belleğe yaz (`counter = tmp`).

İki thread bu adımlar arasına girerse birinin artışı kaybolur:
```
Thread A: read 5
Thread B: read 5
Thread A: write 6
Thread B: write 6     ← 7 olmalıydı
```

Bu **race condition**'dır. Çözüm: senkronizasyon.

---

## 6) `synchronized` — Temel Kilit

`synchronized` bir **monitor lock** alır. Aynı anda sadece bir thread girer:

### Instance Method Üzerinde
```java
public synchronized void inc() {
    counter++;
}
```
Kilit: **`this`** nesnesi. Aynı nesnenin başka synchronized metodları da bu kilidi bekler.

### Static Method Üzerinde
```java
public static synchronized void foo() { ... }
```
Kilit: **`ClassName.class`** nesnesi (tüm sınıf için tek kilit).

### Blok Formu — En Esnek
```java
public void inc() {
    synchronized (this) {     // sadece bu blok için
        counter++;
    }
}

// Özel lock nesnesi (en iyi pratik)
private final Object lock = new Object();
public void inc() {
    synchronized (lock) { counter++; }
}
```

### Reentrant
Aynı thread aynı kilidi **birden fazla alabilir**. Recursive metotlar rahat çalışır:
```java
synchronized void a() { b(); }   // this kilidi alınır
synchronized void b() { ... }    // aynı this kilidi tekrar alınır — OK
```

### Performans Maliyeti
Her girişte kilit alma, çıkışta bırakma → overhead var. Ama JVM akıllı:
- **Biased locking** — tek thread sürekli aynı kilidi alıyorsa zero overhead.
- **Lightweight locking** — düşük contention'da CAS kullanır.
- **Heavyweight locking** — yoğun yarışmada OS mutex'e düşer.

---

## 7) `volatile` — Görünürlük Garantisi

`volatile` bir değişkeni işaretler: **her okuma doğrudan bellekten, her yazma doğrudan belleğe**.

```java
private volatile boolean running = true;

public void stop() { running = false; }

public void work() {
    while (running) {
        // iş yap
    }
}
```

`volatile` olmadan, CPU cache'leri (L1/L2) yüzünden bir thread'in yaptığı değişikliği diğeri **hiç görmeyebilir**. `volatile` ile görünürlük garantili.

### ÖNEMLİ: volatile atomicity vermez
```java
private volatile int count = 0;
count++;   // hâlâ güvensiz! (üç adımlı işlem)
```

`volatile`:
- Görünürlük garantisi → VAR
- Atomicity garantisi → YOK (tek okuma/yazma atomicdir ama `++` üç işlemdir)

Compound işlemler (++, check-then-act) için `synchronized` veya `AtomicInteger` kullan.

### volatile Kullanım Senaryoları
- Durum bayrakları (`running`, `shutdown`).
- Tek bir thread yazıyor, birçok thread okuyor.
- Double-checked locking (singleton).

---

## 8) Atomic Sınıflar — Kilitsiz, Hızlı

`java.util.concurrent.atomic` paketi: `AtomicInteger`, `AtomicLong`, `AtomicReference`, `AtomicBoolean`, `AtomicIntegerArray`...

```java
AtomicInteger counter = new AtomicInteger(0);
counter.incrementAndGet();      // thread-safe ++
counter.addAndGet(5);
counter.compareAndSet(10, 20);  // CAS: 10 ise 20 yap
```

### CAS (Compare-And-Swap)
Modern CPU'ların native desteklediği atomik operasyon. "Eğer bellek hâlâ bu değerdeyse, yeni değeri yaz; değilse fail." Tek assembly komutu.

Atomic sınıflar CAS ile lock-free çalışır — `synchronized`'dan genelde hızlıdır.

**Tek değişken atomicity** için kullan. Karmaşık invariant'lar (2+ değişken tutarlılığı) için yine lock gerekir.

### LongAdder / LongAccumulator
Yüksek contention altında AtomicLong bile yavaşlayabilir. `LongAdder` her thread için ayrı counter tutar, gerekince birleştirir — counter senaryosunda çok hızlı.

---

## 9) `Lock` Arayüzü — `synchronized`'ın Gelişmiş Kardeşi

`java.util.concurrent.locks.Lock` — daha esnek kilit:

```java
Lock lock = new ReentrantLock();
lock.lock();
try {
    // kritik bölge
} finally {
    lock.unlock();          // mutlaka finally içinde
}
```

### Avantajları
- **`tryLock()`** — anında dene, alamazsan false dön (deadlock önleme).
- **`tryLock(ms)`** — belli süre bekle.
- **`lockInterruptibly()`** — bekliyorken interrupt'a cevap ver.
- **Fair locking** — `new ReentrantLock(true)` ilk bekleyene sırasını verir.
- **Multiple `Condition`**s — farklı koşullar için ayrı bekleme kuyrukları.

### ReadWriteLock
Okuma ağırlıklı senaryolarda:
```java
ReadWriteLock rwl = new ReentrantReadWriteLock();
rwl.readLock().lock();    // birçok okuyucu paralel
rwl.writeLock().lock();   // yazıcı tek (okuyucular bekler)
```

### StampedLock (Java 8+)
Optimistic read destekler, ReadWriteLock'tan daha hızlı. Daha sofistike API.

---

## 10) `wait` / `notify` / `notifyAll` — Düşük Seviye Senkronizasyon

`Object` sınıfının metodları. `synchronized` blok içinde çağrılmalı:

```java
synchronized (sharedObj) {
    while (!condition) {
        sharedObj.wait();        // kilidi bırak, bekle
    }
    // koşul sağlandı, devam et
}

// Başka thread:
synchronized (sharedObj) {
    condition = true;
    sharedObj.notifyAll();       // bekleyenleri uyandır
}
```

Kurallar:
- `wait`/`notify` çağırdığın monitor'a sahip olmalısın (synchronized içinde).
- `wait` kilidi **bırakır** ve bekler, uyandığında kilidi **geri alır**.
- `notify` tek bir bekleyeni rastgele, `notifyAll` hepsini uyandırır (genelde `notifyAll` güvenli).
- **Her zaman `while` ile kontrol et** (spurious wakeup olabilir — sebep yok uyanabilir).

### Modern Alternatifler
- **`BlockingQueue`** (producer-consumer için ideal).
- **`Condition`** (Lock ile).
- **`CountDownLatch`**, **`CyclicBarrier`**, **`Semaphore`**, **`Phaser`**.

Yeni kodda `wait/notify` kullanmaktan kaçın, yüksek seviye araçlar kullan.

---

## 11) Deadlock — Karşılıklı Bekleme Kilitlenmesi

İki thread'in birbirinin tuttuğu kilidi beklemesi:
```java
Thread A:
  lock A
  lock B   ← B'yi Thread B tuttu, bekliyor

Thread B:
  lock B
  lock A   ← A'yı Thread A tuttu, bekliyor
```

Her iki thread sonsuza dek bekler.

### Önleme Teknikleri
**a) Kilit sıralaması** — tüm thread'ler kilitleri hep aynı sırada alırsa deadlock olmaz.

**b) `tryLock` + timeout** — alamazsan pes et, geri dön.

**c) Kilit hiyerarşisi** — üst seviye kilit alındığında alt seviye alınabilir, tersi yasak.

**d) Kilit sayısını minimize et** — ne kadar az kilit o kadar az risk.

**e) Immutable nesneler** — kilit gerekmez.

### Livelock
Deadlock'un kuzeni: iki thread sürekli "sen önce geç, yok sen geç" diye davranır, ilerleyemez. Randomized backoff ile çözülür.

### Starvation
Bir thread kilide hiç erişemez (başkaları hep önce alır). Fair lock kullan.

---

## 12) ExecutorService — Modern Thread Yönetimi

Manuel `new Thread(r).start()` çağrısı anti-pattern. Thread oluşturmak pahalı, yönetmek zor. **Thread pool** kullan.

```java
ExecutorService pool = Executors.newFixedThreadPool(4);

pool.submit(() -> doWork());
Future<Integer> f = pool.submit(() -> compute());
Integer result = f.get();

pool.shutdown();                  // yeni task kabul etme
pool.awaitTermination(1, TimeUnit.MINUTES);
```

### Hazır Fabrikalar (`Executors`)
- `newFixedThreadPool(n)` — sabit n thread.
- `newCachedThreadPool()` — ihtiyaca göre büyür/küçülür (60s idle'dan sonra kapanır).
- `newSingleThreadExecutor()` — tek thread, iş sırası garantili.
- `newScheduledThreadPool(n)` — periyodik/delayed görevler.
- **Java 21+**: `newVirtualThreadPerTaskExecutor()` — virtual thread başına.

### `ThreadPoolExecutor` — Tam Kontrol
Production için çoğu zaman `Executors` fabrika metotları yerine doğrudan `ThreadPoolExecutor` ile parametreleri ayarlarsın — queue boyutu, reject politikası vb.

### `Future` ve `get()`
`Future` asenkron sonucu temsil eder. `get()` bloklanır, sonuç gelene kadar bekler. `get(timeout)` süre sınırı.

### Shutdown Sırası
```java
pool.shutdown();                             // yeni görevlere hayır
pool.awaitTermination(60, TimeUnit.SECONDS); // bitsinler
if (!pool.isTerminated()) pool.shutdownNow();// zor kullan
```

---

## 13) CompletableFuture — Asenkron Programlamanın Çağdaşı

`Future.get()` bloklar. Bu gerçekten "async" değil. **`CompletableFuture`** (Java 8) asenkron compose'luk getirir:

```java
CompletableFuture.supplyAsync(() -> fetchUser(id))
    .thenApply(user -> enrichUser(user))
    .thenCompose(user -> saveAsync(user))
    .thenAccept(saved -> log(saved))
    .exceptionally(ex -> { log(ex); return null; });
```

- `thenApply` → sonuç üzerinde dönüşüm (sync).
- `thenCompose` → CompletableFuture zincirlemesi (flat map).
- `thenAccept` → sonucu kullan (void).
- `thenRun` → sonucu umursama, sadece tamamlanmasını bekle.
- `exceptionally` → hata yakala.
- `thenCombine(other, fn)` → iki future'i birleştir.
- `allOf(f1, f2, f3)` → hepsi tamam olduğunda.
- `anyOf(f1, f2)` → herhangi biri tamam olduğunda.

### `supplyAsync` vs `runAsync`
- `supplyAsync(Supplier)` — değer döner.
- `runAsync(Runnable)` — void.

Default ForkJoinPool.commonPool() kullanır; kendi executor'ını vermek isteyen: `supplyAsync(s, myExecutor)`.

---

## 14) Concurrent Koleksiyonlar

Thread-safe, yüksek performanslı koleksiyonlar:

**`ConcurrentHashMap`** — HashMap'in paralel versiyonu. Segment/bucket-level locking.

**`CopyOnWriteArrayList`** — her modifikasyon iç array'i kopyalar. Okuma lock-free, yazma pahalı. Okuma ağırlıklı listeler (listener koleksiyonları) için ideal.

**`ConcurrentLinkedQueue`** — lock-free queue (CAS).

**`BlockingQueue`** — producer-consumer temeli. `put` (dolu ise bekler), `take` (boş ise bekler).
- `ArrayBlockingQueue` — sabit boyut.
- `LinkedBlockingQueue` — opsiyonel sınırlı.
- `PriorityBlockingQueue` — öncelik kuyruğu.
- `SynchronousQueue` — 0 kapasite; el ele teslim.

**`ConcurrentSkipListMap/Set`** — TreeMap'in concurrent versiyonu.

---

## 15) Java Memory Model (JMM) — Happens-Before

Multi-threaded Java kodu neden bazen tuhaf davranır? Çünkü:
- **CPU cache**: her çekirdeğin L1/L2 cache'i var, bellekle tam senkron değil.
- **Compiler reordering**: derleyici/JIT komutları sıralayıp optimize edebilir.
- **CPU reordering**: CPU bile komutları sıralı işlemeyebilir.

JMM bunları disipline eder ve **happens-before** ilişkisi tanımlar. Eğer A happens-before B ise, A'nın etkileri B'den önce görünür.

### Happens-Before Kuralları
- Aynı thread içinde program sırası.
- `volatile` write → `volatile` read.
- `synchronized` unlock → sonraki `synchronized` lock (aynı monitor).
- `Thread.start()` → thread içindeki her şey.
- Thread içindeki her şey → `join()` dönüşü.
- `Thread.interrupt()` → interrupted check.
- Constructor → final field read.

Happens-before olmadan **başka thread'in yazdığını hiç görmeyebilirsin**. `synchronized`, `volatile`, `AtomicX`, `Lock` hepsi bu ilişkileri kurar.

---

## 16) ThreadLocal — Her Thread'e Özel Değişken

```java
ThreadLocal<SimpleDateFormat> dateFormat = 
    ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

String s = dateFormat.get().format(new Date());
```

Her thread kendi nesnesini alır. Thread-safe olmayan nesneleri (SimpleDateFormat gibi) paylaşmak yerine thread başına kopyalamak için idealdir.

**Tehlikesi**: Thread pool ortamında `ThreadLocal.remove()` çağrılmazsa bellek sızıntısı olur. Thread tekrar kullanılır, eski değer asılı kalır. `try/finally` ile temizle.

---

## 17) Virtual Threads (Java 21+) — Devrim

Klasik Java thread'i = OS thread'i. 1 MB+ bellek, bağlamsal geçiş pahalı. 10,000 thread = 10 GB RAM. Web server'lar bu yüzden thread pool'lara mahkum.

Virtual thread: JVM'in yönettiği **hafif thread**. Binlerce, milyonlarca yaratabilirsin. I/O beklerken OS thread'ini bırakır.

```java
Thread.startVirtualThread(() -> doWork());

ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();
```

I/O-bound uygulamalar (REST API, veritabanı sorguları) için devrim. CPU-bound için fayda sağlamaz.

---

## Mülakat Soruları ve Örnek Cevaplar

**1. Process ile thread arasındaki fark?**
Process bağımsız bellek alanı olan program örneği; OS tarafından yönetilir. Thread process içindeki icra birimi; heap paylaşılır, stack her thread'e özel. Thread oluşturmak process'ten ucuz, iletişim kolay ama senkronizasyon gerek.

**2. `start()` ve `run()` farkı?**
`start()` JVM'e yeni OS thread yaratmasını söyler — paralel çalışır. `run()` doğrudan çağrı — mevcut thread'de normal method gibi çalışır, paralelizm yok.

**3. `synchronized`, `volatile`, `Atomic` farkları?**
`synchronized` lock alır — atomicity + visibility, karmaşık invariant'lar için. `volatile` visibility garantisi — atomicity YOK, flag gibi tek okuma/yazma için. `Atomic` lock-free CAS — tek değişken atomicity, yüksek performans.

**4. Deadlock nedir, nasıl önlenir?**
İki veya daha fazla thread'in birbirinin tuttuğu kilitleri beklemesi. Önleme: tüm thread'ler kilitleri aynı sırada alsın, `tryLock` + timeout kullan, kilit hiyerarşisi kur, kilit sayısını minimize et, mümkünse immutable nesneler kullan.

**5. `wait`/`notify` neden synchronized içinde olmalı?**
Çünkü monitor'un sahibi olmak gerekir. `wait` kilidi bırakır, `notify` bekleyenleri uyandırır — her ikisi de monitor semantiği üzerine çalışır. Synchronized dışında çağırırsan `IllegalMonitorStateException`.

**6. `ConcurrentHashMap` nasıl thread-safe olur?**
Java 7 öncesi segment-level lock (16 segment, her biri ayrı kilit). Java 8+ bucket-level lock + CAS. Read'ler çoğunlukla lock-free (volatile + happens-before). Write sadece ilgili bucket'ı kilitler. `Hashtable`'dan çok daha hızlı.

**7. ExecutorService ile doğrudan Thread farkı?**
ExecutorService thread pool yönetir — thread'leri yeniden kullanır (yaratma maliyeti amortize), queue'lanmış task'ları dağıtır, graceful shutdown sunar, Future ile sonuç döndürür. Doğrudan Thread her seferinde yeni OS thread yaratır, pahalı ve kontrolsüz.

**8. `Future` ile `CompletableFuture` farkı?**
`Future.get()` bloklar — synchronous wait. `CompletableFuture` asenkron compose destekler: `thenApply`, `thenCompose`, `exceptionally`. Callback-based pipeline. Ayrıca manuel complete edilebilir, multiple future'ları birleştirme (`allOf`, `anyOf`) kolay.

**9. Race condition örneği?**
`counter++` — iki thread aynı anda okursa artış kaybolur. Veya check-then-act örüntüsü: `if (!map.containsKey(k)) map.put(k, v);` — iki thread arasına girer, aynı anda iki put olabilir. Çözüm: `synchronized`, `AtomicInteger`, `ConcurrentHashMap.putIfAbsent`.

**10. Happens-before ilişkisi nedir?**
JMM'in temel kuralı. Bir işlem A'nın etkileri B'den önce garantili görünüyorsa A happens-before B. `synchronized`, `volatile`, `thread.start/join`, `AtomicX`, final fields gibi yapılar bu ilişkileri kurar. Happens-before olmadan bir thread'in yazdığını diğeri hiç görmeyebilir.

**11. Thread pool boyutunu nasıl seçerim?**
CPU-bound: `Runtime.getRuntime().availableProcessors()` kadar (çekirdek sayısı). I/O-bound: daha fazla (I/O bekleme oranına göre, 2x-10x). Little's Law: thread sayısı ≈ `throughput * response time`.

**12. `InterruptedException` nasıl ele alınmalı?**
İki seçenek: (1) Daha üste fırlat (`throws`), (2) Yakala ve `Thread.currentThread().interrupt()` ile bayrağı yeniden set et. Bastırmak (boş catch) kötü pratik — çağıranın kesilme haberini kaybettirir.

**13. ThreadLocal neden bellek sızıntısı yapabilir?**
Thread pool ortamında thread uzun yaşar. `ThreadLocal.set(...)` sonrası `remove()` çağrılmazsa değer thread'e tutulu kalır. Web server'larda her request için ThreadLocal kullanıp temizlememe klasik leak sebebidir. `try/finally` ile `remove()` et.

**14. Virtual thread ne zaman kazandırır?**
I/O-bound senaryolarda: her request bir virtual thread, milyonlarca eş zamanlı bağlantı mümkün. Klasik thread pool + reactive programming'in yerini alabilir. CPU-bound (sıkı hesaplama) için fayda yok — bir virtual thread de olsa bir OS thread çalışacak.

**15. `volatile` singleton yeter mi?**
"Double-checked locking" için `volatile` gerekli. Yeterli mi? Java 5+ için evet — `volatile` happens-before garantisiyle doğru çalışır. Ama modern tercih `enum` singleton veya static final initialization.
