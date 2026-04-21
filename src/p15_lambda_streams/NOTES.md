# 15 — Lambda, Functional Interface, Stream API, Optional

Java 8 (2014), dilin tarihindeki en büyük sıçramalardan biridir. O sürümle gelen **lambda expression**, **Stream API**, **Optional** ve functional interface paketi, Java'yı fonksiyonel programlamayla tanıştırdı. Öncesi `Iterator`'lar ile yazılan 20 satırlık veri dönüşümleri, şimdi tek satırda biter. Bu paketi iyi öğrenmen mülakatlar için kritik — hiçbir modern Java kodu bunlar olmadan yazılmaz.

---

## 1) Lambda — İsimsiz Fonksiyon

Lambda, **tek bir işlevi temsil eden, küçük, isimsiz fonksiyon**dur. Anonim sınıfların yerini aldı.

### Öncesi (Anonymous Class)
```java
Runnable task = new Runnable() {
    @Override
    public void run() {
        System.out.println("çalışıyorum");
    }
};
```
5 satır, gürültü çok, imza gereksiz tekrarlanıyor.

### Sonrası (Lambda)
```java
Runnable task = () -> System.out.println("çalışıyorum");
```
Tek satır. Niyet açık.

### Lambda Sözdizimi
```java
() -> ifade                   // parametre yok, tek ifade
x -> x + 1                    // tek parametre, parantez opsiyonel
(x) -> x + 1                  // parantezli de olur
(int x, int y) -> x + y       // tip belirtilmiş
(x, y) -> x + y               // tip çıkarımı (inference)
(a, b) -> {                   // blok gövdesi
    int sum = a + b;
    return sum;
}
```

Tek ifade ise `return` ve `;` gerekmez. Blok gövdesinde `return` + `;` normal Java metotu gibi.

### Lambda Nerede Çalışır?
Lambda sadece **functional interface** beklenen yerde kullanılabilir. Derleyici sağ tarafı sol tarafın tipine bakarak anlar:

```java
Runnable r = () -> System.out.println("x");    // Runnable: void run()
Comparator<String> c = (a, b) -> a.compareTo(b); // Comparator: int compare
```

---

## 2) Functional Interface — SAM (Single Abstract Method)

**Sadece bir abstract metodu olan** interface'e functional interface denir.

```java
@FunctionalInterface
public interface Calculator {
    int apply(int a, int b);
}

// Lambda ile
Calculator add = (a, b) -> a + b;
add.apply(3, 5);   // 8
```

`@FunctionalInterface` anotasyonu opsiyoneldir ama yaz — derleyici kontrol eder. İkinci abstract metot eklersen hata alırsın.

Default ve static metotlar **abstract sayılmaz**, eklenebilir:
```java
@FunctionalInterface
interface Calc {
    int apply(int a, int b);                              // tek abstract
    default int applyTwice(int a, int b) { ... }          // default OK
    static Calc add() { return (a, b) -> a + b; }         // static OK
}
```

### Hazır Functional Interface'ler (`java.util.function`)

Java 8 bu pakette genel amaçlı functional interface'ler sundu. Kendi yazmaya gerek kalmadan çoğu senaryoyu kaplar:

| İsim | İmza | Anlam |
|---|---|---|
| `Function<T, R>` | `R apply(T)` | T alır, R döner |
| `BiFunction<T, U, R>` | `R apply(T, U)` | İki giriş, bir çıkış |
| `Predicate<T>` | `boolean test(T)` | Filtre, koşul |
| `Consumer<T>` | `void accept(T)` | T alır, iş yapar, döndürmez |
| `Supplier<T>` | `T get()` | Parametresiz T üretir |
| `UnaryOperator<T>` | `T apply(T)` | T→T |
| `BinaryOperator<T>` | `T apply(T, T)` | İki T, bir T — reduce için |

Primitive versiyonları var (boxing'ten kaçınmak için): `IntFunction`, `ToIntFunction`, `IntPredicate`, `IntConsumer`, `IntSupplier` vs.

### Lambda vs Anonymous Class — Teknik Farklar
Lambda = anonymous class shortcut değildir. Performans olarak farklıdırlar:
- Anonymous class her seferinde **yeni sınıf dosyası** üretir.
- Lambda, `invokedynamic` bytecode'u ile daha hafif.
- Lambda `this`, çevreleyen metodun `this`'i. Anonymous class'ta `this`, anonim sınıfın kendisi.

---

## 3) Method References — Lambda'nın Kısayolu

Eğer lambda sadece var olan bir metodu çağırıyorsa, method reference ile daha kısa yaz:

```java
Function<String, Integer> len1 = s -> s.length();
Function<String, Integer> len2 = String::length;   // method reference
```

### 4 Tipi

**1. Static method reference**: `ClassName::staticMethod`
```java
Function<String, Integer> parser = Integer::parseInt;
// eşdeğer: s -> Integer.parseInt(s)
```

**2. Bound instance method**: `instance::method`
```java
String greeting = "Merhaba";
Supplier<Integer> len = greeting::length;
// eşdeğer: () -> greeting.length()
```

**3. Unbound instance method**: `ClassName::instanceMethod`
```java
Function<String, Integer> len = String::length;
// eşdeğer: s -> s.length()  (parametre s üzerine çağrılır)
```

**4. Constructor reference**: `ClassName::new`
```java
Supplier<ArrayList<String>> factory = ArrayList::new;
// eşdeğer: () -> new ArrayList<String>()
```

---

## 4) Closure — Dış Değişkenlerin Yakalanması

Lambda, dış scope'tan değişken **yakalayabilir** (capture):

```java
int factor = 10;
Function<Integer, Integer> scale = x -> x * factor;
scale.apply(5);   // 50
```

Ama yakalanan değişken **effectively final** olmalı — tanımlandıktan sonra değişmeyen:

```java
int factor = 10;
Function<Integer, Integer> scale = x -> x * factor;
// factor = 20;   // HATA — değiştirirsen yukarıdaki lambda derlenmez
```

Neden? Lambda farklı bir thread'de çalışabilir, dış değişken değişirse tutarsız davranış olurdu. Java güvenlik için zorlar.

Workaround (gerekirse): array veya `AtomicInteger` kullan:
```java
int[] counter = {0};
Runnable r = () -> counter[0]++;   // array immutable değil, içeriği değişebilir
```

---

## 5) Stream API — Veri Akışı İşleme

Stream, **kaynak + operasyon zinciri + terminal** mantığında veri işler:

```java
List<String> names = List.of("Ali", "Veli", "Ayşe", "Fatma");

List<String> result = names.stream()           // kaynak
    .filter(n -> n.length() > 3)               // ara operasyon
    .map(String::toUpperCase)                  // ara operasyon
    .sorted()                                  // ara operasyon
    .toList();                                 // terminal
```

Eskinin 15 satır `for` + `if` + `list.add()` kodu → 5 satır declarative.

### Stream'in Üç Aşaması

**1. Source (Kaynak)**: Veri nereden gelir?
- `collection.stream()`, `collection.parallelStream()`.
- `Arrays.stream(arr)`.
- `Stream.of("a", "b", "c")`.
- `Stream.iterate(0, n -> n+2)` — sonsuz.
- `IntStream.range(0, 100)`.
- `Files.lines(path)` — dosya satır satır.

**2. Intermediate Operations (Ara Operasyonlar)**: Lazy — hemen çalışmaz, terminal gelene kadar bekler.

Yaygın olanlar:
- `filter(Predicate)` — koşula uyanları geçir.
- `map(Function)` — dönüştür.
- `flatMap(Function)` — nested stream'leri düzleştir.
- `distinct()` — tekrarsız.
- `sorted()` / `sorted(Comparator)` — sırala.
- `limit(n)` — ilk n eleman.
- `skip(n)` — ilk n'i atla.
- `peek(Consumer)` — her elemana bak (debug için).

**3. Terminal Operations (Bitirici)**: Stream'i tüketir, sonucu üretir.
- `collect(Collector)` — koleksiyona topla.
- `forEach(Consumer)` — her elemana iş yap.
- `count()` — sayı.
- `reduce(...)` — tek değere indirge.
- `findFirst()`, `findAny()` — Optional döner.
- `anyMatch`, `allMatch`, `noneMatch` — boolean.
- `min`, `max`.
- `toList()` (Java 16+), `toArray()`.

---

## 6) Stream Lazy Evaluation

Ara operasyonlar **çalışmaz** terminal gelene kadar:

```java
list.stream()
    .filter(x -> { System.out.println("filter " + x); return x > 0; })
    .map(x -> { System.out.println("map " + x); return x * 2; });
// HİÇBİR ÇIKTI — terminal yok, hiçbir şey çalışmadı
```

Terminal ekle:
```java
list.stream()
    .filter(x -> { System.out.println("filter " + x); return x > 0; })
    .map(x -> { System.out.println("map " + x); return x * 2; })
    .toList();
```

Ayrıca **fusion optimization**: JVM filter → map → collect zincirini tek döngüde çalıştırır, her operasyon için ayrı döngü yapmaz. Ve **short-circuit**: `findFirst`, `anyMatch`, `limit` bulunca hemen durur.

---

## 7) `Collectors` — Sonucu Topla

`collect()` metoduna Collector verirsin. `java.util.stream.Collectors` yaygın kullanılanları sunar:

```java
.collect(Collectors.toList())                         // modern: .toList()
.collect(Collectors.toSet())
.collect(Collectors.toMap(User::getId, Function.identity()))
.collect(Collectors.groupingBy(User::getCity))        // Map<City, List<User>>
.collect(Collectors.groupingBy(User::getCity, Collectors.counting()))
.collect(Collectors.partitioningBy(u -> u.getAge() >= 18))
.collect(Collectors.counting())
.collect(Collectors.joining(", ", "[", "]"))
.collect(Collectors.averagingInt(User::getAge))
.collect(Collectors.summarizingInt(User::getAge))     // count/sum/min/avg/max
.collect(Collectors.mapping(User::getName, Collectors.toList()))
```

`groupingBy` ve `partitioningBy` çok güçlüdür — tek satırda SQL GROUP BY benzeri iş yapar.

---

## 8) Primitive Streams

`Stream<Integer>` boxing maliyetli. Primitive stream'ler (`IntStream`, `LongStream`, `DoubleStream`) bu yükten kaçınır ve hazır aggregate operasyonlar sunar:

```java
IntStream.rangeClosed(1, 100).sum();                       // 5050
IntStream.rangeClosed(1, 100).average();                   // OptionalDouble
IntStream.of(1, 5, 3).max();                               // OptionalInt
IntStream.of(1, 5, 3).boxed().collect(Collectors.toList()); // Integer list
```

Convert:
- `stream.mapToInt(Integer::intValue)` — `Stream<Integer>` → `IntStream`.
- `intStream.boxed()` — `IntStream` → `Stream<Integer>`.
- `intStream.mapToObj(i -> ...)` — her int'ten obje üret.

---

## 9) `Optional<T>` — Null Yerine

Java'nın en çok sevilmeyen hatası: `NullPointerException`. `Optional` bunun çaresidir — metoddan null dönmek yerine `Optional<T>` dönersin, caller "boş olabilir" durumunu görür.

```java
public Optional<User> findById(int id) {
    User u = db.lookup(id);
    return Optional.ofNullable(u);
}

// Kullanım
Optional<User> maybe = repo.findById(1);
maybe.ifPresent(u -> System.out.println(u.getName()));

// Zincir
String name = repo.findById(1)
    .map(User::getName)
    .filter(n -> !n.isBlank())
    .orElse("anonim");
```

### Yaratma
```java
Optional.of(obj)            // null değilse — null verirsen NPE
Optional.ofNullable(obj)    // null olabilir
Optional.empty()            // boş
```

### Kullanım
```java
opt.isPresent()             // var mı (kullanma — map/filter tercih)
opt.isEmpty()               // yok mu (Java 11+)
opt.get()                   // değer — boşsa NoSuchElementException, kullanma
opt.orElse(default)         // boşsa bu
opt.orElseGet(supplier)     // boşsa lazy üretim (supplier çağrılır)
opt.orElseThrow()           // boşsa NoSuchElementException
opt.orElseThrow(() -> new MyException())  // custom
opt.ifPresent(action)       // varsa action
opt.ifPresentOrElse(action, emptyAction)  // Java 9+
opt.map(fn)                 // varsa dönüştür
opt.flatMap(fn)             // nested Optional'ı düzleştir
opt.filter(predicate)       // koşula uy
```

### Kullanım Kuralları
- **Sadece dönüş tipi olarak** kullan. Field, parametre olarak kullanma.
- `.get()` yerine `.orElse`, `.orElseThrow` kullan.
- `Optional.of(null)` → NPE (doğrusu `ofNullable`).
- Listeler için Optional kullanma — boş liste (`List.of()`) zaten null'un yerini tutar.
- Koleksiyon elemanı olarak `Optional` kullanma (`List<Optional<T>>` anti-pattern).

---

## 10) Parallel Stream — Çoklu Çekirdek

`.parallel()` veya `.parallelStream()` ile stream paralel işlenir:

```java
long count = numbers.parallelStream()
    .filter(n -> isPrime(n))
    .count();
```

Arka plan: `ForkJoinPool.commonPool()` kullanır. Veri stream splitatör (spliterator) ile bölünür, her parça ayrı thread işler, sonuç birleşir.

### Ne Zaman Fayda Sağlar?
- **Büyük veri** (binlerce eleman).
- **Pahalı operasyon** (CPU-bound).
- **Stateless işlemler** (side-effect yok).
- **Independent elements** (sıra gerekmez).

### Ne Zaman Yavaşlatır?
- Küçük veri (thread yönetimi zamanı kazançtan fazla).
- I/O bound (parallelizm çözmez, reactive/async gerek).
- Paylaşılan state → race condition + senkronizasyon maliyet.
- Kullanılan Collector thread-safe değilse yanlış sonuç.

Aklı başında kural: **default sequential, paralel kanıtlanmış durumda**.

---

## 11) Yaygın Hatalar

**a) Stream iki kere tüketme**: Stream tek kullanımlıktır.
```java
Stream<Integer> s = list.stream();
s.count();
s.count();   // IllegalStateException
```

**b) Stream içinde side-effect**:
```java
List<Integer> result = new ArrayList<>();
list.stream().filter(...).forEach(result::add);    // kötü, parallel'de bozuk
// Doğrusu:
List<Integer> result = list.stream().filter(...).toList();
```

**c) `Optional.of(nullable)` NPE riski**: `ofNullable` kullan.

**d) `.get()` yerine orElse/orElseThrow kullan.**

**e) `parallelStream` körü körüne**: ölç, ölçmeden kullanma.

**f) `forEach` sıra garantisi**: sequential'da garantili, parallel'da değil. Sıra önemliyse `forEachOrdered`.

---

## 12) Text Blocks + Stream — Modern Java Örneği

```java
String csv = """
        Ali,30,Ankara
        Veli,25,İstanbul
        Ayşe,35,İzmir
        """;

List<String> names = csv.lines()
    .filter(line -> !line.isBlank())
    .map(line -> line.split(",")[0])
    .toList();
```

---

## Mülakat Soruları ve Örnek Cevaplar

**1. Lambda ile anonymous class farkı?**
Lambda daha kısa sözdizimi, `this` çevreleyen metodun `this`'i (anonymous class'ta kendi sınıfı), `invokedynamic` bytecode'u kullanır (performans), sadece functional interface ile çalışır. Anonymous class herhangi bir class/interface ile çalışır, her biri ayrı sınıf dosyası üretir.

**2. Functional interface nedir?**
Tek abstract metodu olan interface (SAM — Single Abstract Method). Lambda ve method reference ile kullanılır. Default ve static metotlar eklenebilir. `@FunctionalInterface` anotasyonu derleyici kontrolü verir.

**3. map ve flatMap farkı?**
`map(T → R)` her elemanı dönüştürür, stream tipi değişir ama tek seviye kalır. `flatMap(T → Stream<R>)` her elemanı stream'e dönüştürür ve **düzleştirir** (tek seviye). Nested listeyi flat yapmak, bir-çoklu dönüşüm için kullanılır.

**4. Stream neden lazy?**
Performans. Ara operasyonlar terminal gelene kadar çalışmaz. JVM tüm zinciri fusion ile optimize eder, short-circuit (findFirst, limit) erken duruş. Sonsuz stream'ler (Stream.iterate) bu sayede çalışabilir.

**5. `parallelStream` ne zaman kullanılmalı?**
Büyük data + CPU-bound + stateless + independent işlemlerde. Küçük data, I/O, shared state durumlarında faydasız veya zararlı. Her zaman ölç — varsayımla paralel yapma.

**6. `Optional` doğru kullanımı?**
Dönüş tipi olarak kullan (field, parametre DEĞİL). `.get()` yerine `.orElse`, `.orElseGet`, `.orElseThrow`. `.map`, `.flatMap`, `.filter`, `.ifPresent` ile zincirle. `Optional.of(nullable)` yerine `Optional.ofNullable`. Liste yerine Optional kullanma (boş liste varken).

**7. Collector nedir, kendi Collector'ını yazabilir misin?**
Stream'i terminal olarak toplayan araç. `Collectors` sınıfında hazır: `toList`, `toMap`, `groupingBy`, `joining`, `partitioningBy`. Custom Collector: `Collector.of(supplier, accumulator, combiner, finisher)` ile yaz. Ama genelde hazırlar yeter.

**8. Stream'i iki kere kullanabilir miyim?**
Hayır, `IllegalStateException` alırsın. Stream tek tüketimliktir. Yeniden kullanman gerekiyorsa: sonucu koleksiyona topla veya kaynağı yeniden stream'le (`list.stream()` tekrar).

**9. Method reference tipleri?**
4 tip: static (`Integer::parseInt`), bound instance (`obj::method`), unbound instance (`String::length` — parametre üzerine çağrılır), constructor (`ArrayList::new`). Her biri spesifik lambda'nın kısa halidir.

**10. Closure nedir, effectively final ne demek?**
Closure: lambda'nın dış scope'taki değişkenleri yakalaması. Yakalanan değişken **effectively final** olmalı — tanımlandıktan sonra değişmeyen. Java güvenlik ve multi-thread tutarlılığı için zorluyor. Array veya AtomicReference ile workaround mümkün.

**11. Stream side-effect neden kötü?**
Parallel stream'de race condition. Thread-safe olmayan koleksiyona add etmek bozuk sonuç verir. Functional programming disiplini side-effect'leri sadece terminal'de (collect, forEach) beklentisi üzerine kurulu. `filter`/`map` pure olmalı.

**12. `forEach` ile `forEachOrdered` farkı?**
Sequential stream'de aynı. Parallel stream'de: `forEach` sıra garantisi vermez (hızlı), `forEachOrdered` orijinal sırayı korur (yavaş). Side-effect'in sırası önemli değilse `forEach`, önemliyse `forEachOrdered`.

**13. `reduce` nasıl çalışır?**
Stream'i tek değere indirger. `reduce(identity, BinaryOperator)`: identity ile başla, her elemanı birleştir. Örnek: `list.stream().reduce(0, Integer::sum)`. Primitive için `IntStream.sum()`, `min()`, `max()` hazırdır.

**14. `Optional` ne zaman anti-pattern olur?**
Field olarak: nesne state'i olarak tutma. Method parametresi: caller'ı zorlama, default değer daha temiz. Collection/Map elemanı: boş koleksiyon zaten yokluk temsil ediyor. Primitive için: `OptionalInt`, `OptionalLong`, `OptionalDouble` var ama genelde primitive için null sorunu olmuyor.

**15. Stream ile for döngü karşılaştırması — hangisi seçilmeli?**
Okunurluk ve dönüşüm zinciri varsa stream. Performans kritik döngülerde for daha hızlı olabilir (özellikle primitive array). Debug kolaylığı for'da daha iyi. Genel kural: veri dönüşümü pipeline'ında stream, basit iterasyonda for. Ekip tercihi de önemli.
