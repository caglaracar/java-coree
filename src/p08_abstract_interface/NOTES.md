# 08 — Abstract Class ve Interface

Bu ikisi Java'nın soyutlama (abstraction) araçlarıdır. İkisi de "tam tanımlanmamış şablonlar" üretir — alt sınıfın/implementation'ın dolduracağı boşluklar bırakır. Ama felsefeleri ve kullanım senaryoları farklıdır. "Hangisini ne zaman kullanmalıyım?" sorusu mülakatların klasiğidir; bu paketi bitirdiğinde cevabı elinin tersi gibi bileceksin.

---

## 1) Abstraction Nedir, Neden Var?

**Soyutlama**, karmaşık bir sistemin ayrıntılarını gizleyip sadece "ne yaptığını" göstermektir. Kullanıcısına "nasıl yaptığını" düşündürmez. Bir televizyon uzaktan kumandası düşün: tuşlara basarsın, kanal değişir. İçeride IR sinyali, devreler, sinyal işleme var ama sen **arayüz**i (tuşları) kullanırsın, detay umursamaz.

Java'da aynı prensip:
- **Arayüz** ortada — metod imzaları, sözleşme.
- **İmplementasyon** gizli — nasıl yapıldığı.

Soyutlama iki araçla uygulanır:
1. **Abstract class** — kısmen tanımlı sınıf (bazı metodlar dolu, bazıları boş).
2. **Interface** — tamamen sözleşme (Java 8 öncesi), modernde biraz karma.

---

## 2) Abstract Class — Kısmen Tanımlı Şablon

`abstract` anahtar sözcüğü ile işaretlenen sınıf. Özellikleri:

**Nesnesi oluşturulamaz.**
```java
abstract class Shape { ... }
Shape s = new Shape();   // HATA: Shape abstract is not instantiable
```
Mantıklı, çünkü abstract class tamamlanmamıştır. Tam bir "Şekil" değildir, genel kavramdır. Sadece **alt sınıf** (Circle, Square) somut nesne üretir.

**İçinde abstract ve somut metod karışık olabilir.**
```java
abstract class Shape {
    abstract double area();         // alt sınıf doldurur
    void describe() {                // somut — ortak davranış
        System.out.println("Alan: " + area());
    }
}
```

Abstract metodun gövdesi yoktur (`;` ile biter). Alt sınıf bu metodu **implement etmek zorunda** (kendi de abstract olmayı seçmezse).

**Constructor olabilir.**
```java
abstract class Shape {
    String color;
    Shape(String color) { this.color = color; }   // constructor
}
```
Bu constructor `new Shape(...)` ile çağrılmaz, ama alt sınıfın constructor'ı `super(color)` ile kullanır. Ortak başlatma mantığı için idealdir.

**Field tutabilir.** Her türden (static, final, instance), her erişim seviyesinde.

**Metodların erişimi her seviyede olabilir** (public, protected, private, package-private).

**Tek kalıtım kuralına tabidir** — bir sınıf tek abstract class'tan türeyebilir.

### Ne zaman abstract class kullanmalı?

Şu iki durumda:
1. **Ortak state (field) paylaşılıyor** — alt sınıfların aynı data'yı tutması gerekiyor.
2. **Ortak davranışın bir kısmı concrete, bir kısmı alt sınıfa bırakılıyor** — template method örüntüsü.

Klasik örnek: `AbstractList` Java Collections Framework'te `ArrayList`, `LinkedList`'in ortak iskeleti.

---

## 3) Interface — Saf Sözleşme

Interface **ne yaptığı**nı tanımlar, **nasıl yaptığı**nı değil (modern Java kısmen değiştirdi, birazdan göreceğiz). Bir sınıfın "şu metodları sağlamak zorundayım" diye imzaladığı sözleşme.

```java
public interface Comparable<T> {
    int compareTo(T other);
}
```

`Comparable` implement eden her sınıf `compareTo` metodunu yazmalıdır. Bu sayede `Collections.sort()` gibi metotlar **ne sınıf olduğunu bilmeksizin** karşılaştırabilir — sadece `Comparable`la konuşur.

### Interface'in Klasik Kuralları (Java 7 ve Öncesi)
- **Tüm metodlar otomatik `public abstract`** — yazmadan da öyle olur.
- **Tüm field'lar otomatik `public static final`** (sabit).
- Nesnesi oluşturulamaz.
- `implements` ile kullanılır: `class Car implements Vehicle, Printable { ... }`.
- Bir sınıf birden fazla interface implement edebilir — **çoklu kalıtım** burada olur.

```java
public interface Drawable {
    int MAX_SIZE = 1000;      // aslında public static final
    void draw();              // aslında public abstract
}
```

### Java 8'den Sonra — Interface Yenilendi

Java 8 **default metodlar** ve **static metodlar** getirdi. Artık interface sadece sözleşme değil, biraz implementation da tutabilir:

**Default metod**: Gövdesi vardır, implement eden sınıf override etmek **zorunda değildir**.
```java
public interface Drawable {
    void draw();
    
    default void drawTwice() {   // gövdeli
        draw();
        draw();
    }
}
```

Default metod neden eklendi? **Backward compatibility**. Varsayalım `List` interface'ine yeni bir `sort()` metodu eklemek istiyorsun. Bunu eskiden yaparsan Java ekosistemindeki binlerce `List` implement eden sınıf bir gecede derleyici hatası verirdi. Default ile yeni metot ekleyip varsayılan bir davranış verebilirsin, eski kod çalışmaya devam eder.

**Static metod**: Doğrudan interface adından çağrılır.
```java
public interface Comparator<T> {
    int compare(T a, T b);
    
    static <T> Comparator<T> naturalOrder() {
        return (a, b) -> ((Comparable) a).compareTo(b);
    }
}
```

### Java 9+ — Private Metodlar

Default ve static metodlar arasında kod paylaşımı için:
```java
public interface MyAPI {
    default void a() { common(); ... }
    default void b() { common(); ... }
    private void common() { ... }   // sadece interface içinden erişilir
}
```

---

## 4) Interface mi Abstract Class mı? Karşılaştırma

Tam bir karşılaştırma tablosu:

| Özellik | Abstract Class | Interface |
|---|---|---|
| Çoklu kalıtım | Yok (tek parent) | Var (birden fazla implement) |
| Constructor | Evet | Hayır |
| Field | Her türden | Sadece `public static final` (sabitler) |
| Metod türleri | Abstract, somut, static, final, her erişim | Abstract, default, static, private (Java 8+/9+) |
| Erişim belirleyiciler | Her seviye (public, protected, private, package) | Metodlar `public` (default için), field'lar `public static final` |
| "IS-A" mı "CAN-DO" mu? | IS-A | CAN-DO (kapabilite) |
| State paylaşımı | Var | Yok |
| Eklemeler backward-breaks | Evet | Default metodla hayır |
| Kullanım (keyword) | `extends` | `implements` |

### Karar Rehberi

**Abstract class kullan** şu durumlarda:
- Alt sınıflar **ortak field'lar** (data/state) paylaşıyorsa.
- Ortak **somut davranış** varsa ve altında template method örüntüsü kurmak istiyorsan.
- İlişki gerçekten **"IS-A"** (Dog IS-A Animal gibi).

**Interface kullan** şu durumlarda:
- Farklı sınıf hiyerarşilerinden gelen nesneler **aynı kapabiliteye** sahipse. (`Bird` ve `Airplane` ikisi de `Flyable` — ama farklı ailelerden.)
- Çoklu sözleşme gerekiyorsa.
- API tasarımı yapıyor, **gevşek bağlı (loosely coupled)** kod istiyorsan.
- Gerçekten saf sözleşme ise.

Pratikte çoğu modern Java kodunda **interface** baskındır. Abstract class daha çok framework iç yapılarında görülür.

---

## 5) Çoklu Implementasyon ve Diamond Problem

Bir sınıf birden fazla interface implement edebilir:
```java
class Duck implements Swimmer, Flyer, Quacker {
    public void swim() { ... }
    public void fly() { ... }
    public void quack() { ... }
}
```

Ama ya iki interface aynı imzalı default metoda sahipse? **Diamond problem** doğar:

```java
interface A {
    default void hello() { System.out.println("A"); }
}
interface B {
    default void hello() { System.out.println("B"); }
}

class C implements A, B {
    // HATA: Duplicate default methods — hangisi çalışsın?
}
```

Çözüm: C sınıfı kendi çözümünü yazmak zorunda. `super` ile hangisini istediğini belirtebilir:

```java
class C implements A, B {
    @Override
    public void hello() {
        A.super.hello();     // A'nınki
        B.super.hello();     // B'ninki
        // veya kendi bambaşka davranışın
    }
}
```

Bu C++'daki gerçek diamond'dan (gerçek state çakışması) daha kontrollüdür çünkü sadece davranış çakışması var, state çakışması yok.

---

## 6) Functional Interface — Lambda'ların Temeli

**Sadece bir abstract metodu olan interface**'e *functional interface* denir (SAM — Single Abstract Method). Bu, Java 8 ile gelen **lambda expression**'ların tamam alanıdır.

```java
@FunctionalInterface
public interface Calculator {
    int apply(int a, int b);
}

// Lambda ile kullanım
Calculator add = (a, b) -> a + b;
System.out.println(add.apply(3, 5));    // 8
```

`@FunctionalInterface` anotasyonu opsiyoneldir ama yaz — derleyici kontrol eder. Eğer yanlışlıkla ikinci bir abstract metod eklersen hata verir, hatayı baştan yakalarsın.

Default ve static metodlar "abstract" sayılmaz, ekleyebilirsin:
```java
@FunctionalInterface
interface Calculator {
    int apply(int a, int b);          // tek abstract
    default int applyTwice(int a, int b) { return apply(apply(a,b), b); }  // default OK
    static Calculator sum() { return (a, b) -> a + b; }                    // static OK
}
```

Java'nın kendi kütüphanesinde hazır functional interface'ler:
- `Runnable` — `void run()`
- `Callable<V>` — `V call()`
- `Comparator<T>` — `int compare(T, T)`
- `Function<T, R>` — `R apply(T)`
- `Predicate<T>` — `boolean test(T)`
- `Consumer<T>` — `void accept(T)`
- `Supplier<T>` — `T get()`

Bunları 15. pakette detaylı göreceksin.

---

## 7) Marker Interface

Hiç metodu olmayan interface'e **marker interface** denir. Amaç sadece nesneye bir "işaret" koymak:

```java
public interface Serializable {}     // boş!
public interface Cloneable {}        // boş!
```

Bu interface'leri implement etmek, o sınıfa "bu özellikten faydalanabilir" etiketi koyar. JVM veya framework bu etikete bakıp davranış değiştirir.

Modern Java'da marker interface yerine **annotation** tercih edilir (`@Deprecated`, `@Override`, `@FunctionalInterface`). Anotasyonlar daha esnektir (parametre alabilir, runtime okunabilir).

---

## 8) Interface Segregation Principle (SOLID'in I'si)

"Büyük, kalabalık interface yerine küçük, odaklı interface'ler kullan." Yaygın hata:

```java
// Kötü: kalabalık interface
interface Worker {
    void work();
    void eat();
    void sleep();
}

class Robot implements Worker {
    public void work() { ... }
    public void eat() { throw new UnsupportedOperationException(); }    // ?!
    public void sleep() { throw new UnsupportedOperationException(); }
}
```

Robot yemek yemez, uyumaz. Ama interface öyle dayattığı için boş metot bırakmak zorunda. Çözüm: interface'i böl.

```java
interface Workable { void work(); }
interface Eatable { void eat(); }
interface Sleepable { void sleep(); }

class Human implements Workable, Eatable, Sleepable { ... }
class Robot implements Workable { ... }
```

Herkes ihtiyacı olanı implement eder, gereksizi taşımaz.

---

## 9) Abstract Class + Interface Beraberliği

İki aracı birleştirebilirsin. Çok sık görülen bir pattern: **interface davranışı tanımlar, abstract class kısmi implementation sunar**.

```java
public interface List<E> { ... }                 // sözleşme
public abstract class AbstractList<E> implements List<E> {
    // ortak implementation
}
public class ArrayList<E> extends AbstractList<E> { ... }  // boşlukları doldurur
```

Bu sayede:
- `List` sözleşmeye bağlı kalır (polimorfizm için).
- `AbstractList` ortak kodu toplar.
- `ArrayList` sadece kendine özel kısmı yazar.

Collections Framework boydan boya bu pattern'i kullanır.

---

## 10) Yaygın Hatalar

**a) Abstract class'a çok fazla state/metod koymak.** Bu bir sonraki jenerasyon geliştiricinin bakımını zorlaştırır. Küçük tut.

**b) Interface'i data container olarak kullanmak.** Interface sadece `public static final` sabitlere izin verir, bu yüzden sabit havuzu olarak kullanılır — ama bu **anti-pattern**'dir (Constant Interface Antipattern). Sabitleri normal sınıfta tut.

**c) Çok derin abstract class hiyerarşisi.** Her yeni seviye complexity ekler. İki-üç seviye sonra sorgula — belki composition daha doğrudur.

**d) Diamond problem'den kaçmak için default metot çözümünü unutmak.** Derleyici sana hatırlatır ama bazen mesajı yanıltıcı olur.

---

## Mülakat Soruları ve Örnek Cevaplar

**1. Abstract class ile interface arasındaki farklar nelerdir?**
Abstract class tek kalıtımdır, constructor ve field'ı vardır, her türden metot (abstract, concrete, static, final) tutabilir. Interface çoklu implement edilebilir, constructor'ı ve instance field'ı yoktur (sadece `public static final`), Java 8'den sonra default/static/private metodlar ekleyebilir. Abstract class "IS-A" ilişkisi ve ortak state paylaşımı için, interface saf kapabilite ve gevşek bağlı tasarım için.

**2. Interface'te field yazabilir miyim?**
Evet ama otomatik olarak `public static final` olur (sabit). Instance field tutamazsın. Bu yüzden interface "Constant Interface" olarak abuse edilmemeli.

**3. Java'da çoklu kalıtım nasıl sağlanır?**
Sınıf seviyesinde çoklu kalıtım yasaktır. Ama bir sınıf birden fazla interface implement edebilir, böylece çoklu "sözleşme" elde edilir. Java 8'den sonra default metodlarla interface'ler kısmi implementation da taşıyabilir.

**4. Default metod neden eklendi?**
Backward compatibility. `List` gibi var olan interface'lere yeni metot eklemek, milyonlarca implement eden sınıfı kırardı. Default metod sayesinde yeni metot varsayılan davranışla eklenebilir, eski kod derlenmeye devam eder. `forEach`, `stream`, `removeIf` gibi Java 8 eklemeleri bu sayede yapıldı.

**5. Diamond problem Java'da nasıl çözülür?**
İki interface aynı imzalı default metod sağlarsa, implement eden sınıf **kendi versiyonunu yazmak zorunda**dır. `A.super.methodName()` ile hangi interface'inkini kullanacağını seçebilir.

**6. Functional interface nedir?**
Tek abstract metodu olan interface. Lambda ve method reference ile kullanılır. Default ve static metod sayısı önemsizdir. `@FunctionalInterface` anotasyonu opsiyoneldir ama eklemek derleyici kontrolü sağlar.

**7. Abstract class'tan nesne oluşturulur mu?**
Hayır. Ancak **anonymous subclass** üzerinden "gibi" görünebilir: `new AbstractX() { void absMethod() { ... } }` aslında anonim bir alt sınıf oluşturur.

**8. Abstract class'ın constructor'ı olur mu?**
Evet, olabilir ve sıkça olur. `new` ile doğrudan çağrılmaz ama alt sınıf constructor'ı `super(...)` ile kullanır. Ortak başlatma mantığı için idealdir.

**9. Interface'te static metot ne zaman geldi?**
Java 8 ile. Doğrudan interface adından çağrılır (`MyInterface.staticMethod()`). Faktör metodlar ve yardımcı (utility) metodlar için sıkça kullanılır. `Comparator.naturalOrder()`, `List.of(...)` örnekleri.

**10. "Favor composition over inheritance" ilkesini interface nasıl destekler?**
Interface'ler gevşek bağ sağlar — implement eden sınıf sözleşme dışında hiçbir şeye maruz kalmaz. Kalıtımın "fragile base class", "tight coupling" sorunlarından kaçılır. Modern Java framework'leri (Spring, Hibernate) interface ağırlıklıdır, implementation ise composition ile birleştirilir (dependency injection).

**11. Marker interface nedir?**
Metodu olmayan interface. Sınıfa bir "etiket" koymak için kullanılır (`Serializable`, `Cloneable`). Modern Java'da yerini çoğunlukla **annotation**'lar aldı çünkü daha esnek ve parametrik.
