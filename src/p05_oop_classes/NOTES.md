# 05 — Nesne Yönelimli Programlama Temelleri

Nesne yönelimli programlama (OOP), Java'nın **dünya görüşüdür**. Java'da fonksiyonlar havada uçuşmaz — her şey bir sınıfın içindedir. Bu pakette OOP'nin temel yapı taşlarını tek tek ele alıyoruz: sınıf nedir, nesne nedir, constructor ne işe yarar, `this` ne anlama gelir. Sonraki paketlerde bu üzerine inheritance, polymorphism, abstraction, encapsulation inşa edilecek.

---

## 1) Sınıf ve Nesne — Kalıp ve Üründür

**Sınıf (class)** bir şablondur, bir tariftir, bir kalıptır. Kendisi bir şey yapmaz, sadece bir "tip"i tanımlar.

**Nesne (object / instance)** o şablondan üretilmiş somut bir varlıktır.

En sevilen analoji: **Ev planı ve evler**. Mimarın çizdiği bir ev planı (sınıf) vardır. Plana göre 100 ev (nesne) inşa edilebilir. Her ev aynı plana sahiptir ama her biri ayrı bir binadır: farklı mahallede, farklı renkte, farklı mobilyalarla. Plan değişirse (sınıf güncellenirse) yeni inşa edilenler değişir, ama evler birbirinden bağımsız yaşar.

```java
public class Car {
    // ALANLAR (fields / attributes) — her Car'ın sahip olacağı özellikler
    String brand;
    String color;
    int speed;

    // METODLAR (methods / behaviors) — her Car'ın yapabileceği eylemler
    void accelerate(int amount) {
        speed += amount;
    }
    
    void brake() {
        speed = 0;
    }
}
```

Bu sadece **tanım**. Hiçbir araba yaratılmadı. Şimdi üretelim:

```java
Car audi = new Car();           // heap'te yeni Car nesnesi
audi.brand = "Audi";
audi.color = "Red";
audi.accelerate(50);

Car bmw = new Car();            // farklı bir nesne
bmw.brand = "BMW";
bmw.color = "Black";
```

`audi` ve `bmw` iki ayrı nesnedir. Biri 50 hıza çıktığında diğerinin hızı değişmez. Her birinin kendi `brand`, `color`, `speed` alanları vardır.

---

## 2) `new` — Nesne Yaratmanın Perde Arkası

`new Car("Audi")` yazdığında arka planda şu adımlar olur:

1. **Heap'te bellek alanı ayrılır.** Nesnenin field'larını tutmak için yeterli yer bulunur.
2. **Field'lar varsayılan değerlerine ayarlanır.** `int → 0`, `boolean → false`, `referans → null`. Bu senin atamandan önce gerçekleşir.
3. **Parent class'ın constructor'ı çağrılır** (inheritance'ta göreceğiz).
4. **Instance initializer blokları çalışır** (nadiren kullanılır).
5. **Constructor çalışır.** Senin verdiğin başlangıç değerleri set edilir.
6. **Nesnenin referansı** döner ve sol taraftaki değişkene atanır.

Bu kademeli süreci bilmen önemlidir çünkü bazı tuzaklar bu sırayla alakalıdır. Örneğin constructor'da henüz başlatılmamış bir field'a erişebilirsin ama o hâlâ varsayılan değerindedir.

---

## 3) Constructor — Nesneye Can Veren Tören

**Constructor** bir metot gibidir ama bazı özel kuralları vardır:

- **İsmi sınıfın ismiyle aynı olmalı**.
- **Geri dönüş tipi yoktur** (`void` bile değil, hiçbir şey).
- **`return` yazılabilir** ama değer döndürmez — sadece erken çıkış.
- `new` çağrıldığında otomatik çalışır, elle çağıramazsın.

```java
public class Car {
    String brand;
    String color;
    
    // Constructor
    public Car(String brand, String color) {
        this.brand = brand;
        this.color = color;
    }
}
```

### Boş (No-arg) Constructor Kuralı

Eğer hiç constructor yazmazsan, derleyici sana **otomatik** boş bir constructor ekler:

```java
public Car() { }   // görünmez ama var
```

Ama parametreli bir constructor yazdığın anda **otomatik boş constructor eklenmez**! Bu çok yaygın bir tuzaktır:

```java
public class Car {
    String brand;
    public Car(String brand) {   // yazdığım constructor
        this.brand = brand;
    }
}

Car c = new Car();   // HATA — no-arg constructor yok!
```

Eğer hem parametreli hem parametresiz kullanmak istiyorsan, ikisini de açıkça yaz.

### Constructor Overloading

Aynı sınıfın birden çok constructor'ı olabilir, yeter ki **imzaları farklı** olsun:

```java
public class Rectangle {
    int width, height;
    
    public Rectangle() {              // default
        this(1, 1);
    }
    public Rectangle(int side) {      // kare
        this(side, side);
    }
    public Rectangle(int w, int h) {  // dikdörtgen
        this.width = w;
        this.height = h;
    }
}
```

### `this(...)` — Kendi Constructor'ını Çağırmak

Aynı sınıfın başka bir constructor'ına delege etmek için `this(...)` kullanılır. **İlk satır olmak zorunda**:

```java
public Rectangle(int side) {
    this(side, side);      // parametreli constructor'a git
    // bundan sonra ek iş yapabilirsin
}
```

Neden ilk satırda olmalı? Çünkü **bir nesne ancak bir kere başlatılabilir**. Önce başka iş yapıp sonra constructor çağırmak "yarı başlatılmış" durum oluşturur; Java buna izin vermez.

### `super(...)` — Parent Constructor'ını Çağırmak

Inheritance'tan öğreneceğiz ama kısaca: alt sınıf constructor'ı, üst sınıfın constructor'ını `super(...)` ile çağırmak zorundadır. Yazmazsan derleyici `super();` ekler (parent'ın no-arg constructor'ı olmalı).

---

## 4) `this` Anahtar Sözcüğü

`this` şu anki nesnenin referansıdır. İki ana kullanımı var:

### Field ile parametre çakışmasını ayırmak
```java
public Car(String brand) {
    this.brand = brand;   // soldaki field, sağdaki parametre
}
```
Bu Java'da çok yaygındır çünkü parametre isimlerini field isimleriyle aynı tutmak (`String brand`) daha okunaklıdır, `this.` çakışmayı çözer.

### Aynı sınıfın başka constructor'ını çağırmak
```java
public Rectangle(int side) {
    this(side, side);
}
```

### Kendini döndürmek — Method Chaining (Builder Pattern)
```java
public class Car {
    private int speed;
    
    public Car accelerate(int amount) {
        this.speed += amount;
        return this;     // kendimi döndür
    }
    public Car brake(int amount) {
        this.speed -= amount;
        return this;
    }
}

// Kullanım
new Car().accelerate(50).accelerate(30).brake(20);
```
Buna **fluent interface** veya **method chaining** denir. Builder pattern'in temelidir.

### Static metod içinde `this` YOK
Static metod bir sınıfa aittir, nesne yoktur, `this` de yoktur:
```java
static void hello() {
    System.out.println(this.name);   // HATA
}
```

---

## 5) Field Çeşitleri

**Instance field** — nesneye aittir, her nesnenin kendi kopyası var:
```java
class Car { int speed; }   // her Car'ın kendi speed'i
```

**Static (class) field** — sınıfa ait, tüm nesneler paylaşır:
```java
class Car { static int totalProduced; }   // tüm Car'lar paylaşır
```

**Local değişken** — method içinde tanımlı, method bitince yok olur. Varsayılan değeri yok, atama zorunlu.

**Parametre** — metoda dışarıdan geçen değer. Metod gövdesinde local gibi davranır.

---

## 6) Metod Anatomisi

```java
public int add(int a, int b) {
    return a + b;
}
```

- `public` — erişim belirleyici.
- `int` — dönüş tipi (`void` ise dönmez).
- `add` — isim.
- `(int a, int b)` — parametre listesi.
- Gövde `{...}` içinde.

### Method İmzası (Signature)
İmza = **metod adı + parametre tipleri** (sırasıyla). Dönüş tipi ve parametre **isimleri** imzanın parçası **değildir**.

```java
int foo(int x, String s) { ... }
void foo(int x, String s) { ... }   // HATA — aynı imza
```

İmzalar aynı, dönüş tipi farklı olunca derleme başarısız. Overload için **parametre tipleri veya sayısı** farklı olmak zorunda.

### Method Overloading
Aynı isimli, farklı imzalı metodlar tanımlayabilirsin:
```java
int add(int a, int b) { return a + b; }
double add(double a, double b) { return a + b; }
int add(int a, int b, int c) { return a + b + c; }
```

Derleyici hangi metodu çağıracağını **parametrelerin tipine bakarak derleme zamanında** karar verir. Buna "compile-time polymorphism" de denir.

---

## 7) Nesnenin Yaşam Döngüsü

Bir nesnenin doğumdan ölüme yolculuğu:

1. **Doğuş**: `new Foo()` ile heap'te yer açılır, constructor çalışır. Nesne yaşamaya başlar.
2. **Kullanım**: Bir veya daha fazla referans nesneyi işaret ettiği sürece yaşar. Referanslar değişken, koleksiyon, parametre olarak dolaşabilir.
3. **Ulaşılmazlık**: Tüm referanslar kaybolur (scope bittiği için veya `= null` yapıldığı için). Artık nesneye erişemezsin ama bellekte hâlâ var.
4. **Garbage Collection**: JVM bir noktada tarar, erişilemez nesneyi tespit eder, belleğini geri alır.
5. **~~`finalize()`~~**: Eski Java'da GC'den önce çağrılırdı — **Java 9+ deprecated**, asla kullanma. Yerine `AutoCloseable` + try-with-resources veya `Cleaner` API.

Önemli: Sen nesnenin ne zaman öleceğini kontrol edemezsin. GC'yi rica edebilirsin (`System.gc()`) ama garanti yoktur.

---

## 8) Nested ve Inner Sınıflar

Bir sınıf başka bir sınıfın içinde tanımlanabilir. Dört tip vardır:

### Static Nested Class
```java
public class Outer {
    static class Nested {
        void hi() { System.out.println("merhaba"); }
    }
}

Outer.Nested n = new Outer.Nested();
```
`static` olduğu için Outer'ın instance'ına bağlı değildir, bağımsız yaşar. Outer'ın private static üyelerine erişebilir.

### Inner Class (non-static)
```java
public class Outer {
    int x = 10;
    class Inner {
        void show() { System.out.println(x); }   // Outer'ın field'ını kullanır
    }
}

Outer o = new Outer();
Outer.Inner in = o.new Inner();
```
Inner, **Outer'ın instance'ına bağlı**dır. Outer nesnesi olmadan yaratılamaz. Outer'ın gizli bir referansını tutar — bu bellek sızıntısı kaynağı olabilir.

### Local Class
Bir metod içinde tanımlanan sınıf. Nadiren kullanılır, genelde lambda yeterlidir.

### Anonymous Class
İsimsiz, tek seferlik:
```java
Runnable r = new Runnable() {
    @Override public void run() {
        System.out.println("anonim");
    }
};
```
Java 8 öncesi bunu lambda yerine kullanırdık. Çoğu durumda artık `() -> System.out.println("anonim")` yazmak daha temiz.

---

## 9) `record` — Modern Veri Sınıfları (Java 16+)

Sık karşılaştığımız bir durum: sadece veri tutan, immutable, basit bir sınıf lazım. Eskiden:

```java
public class Point {
    private final int x, y;
    public Point(int x, int y) { this.x = x; this.y = y; }
    public int getX() { return x; }
    public int getY() { return y; }
    @Override public boolean equals(Object o) { /* 8 satır */ }
    @Override public int hashCode() { /* 3 satır */ }
    @Override public String toString() { /* 2 satır */ }
}
```

20+ satır boilerplate. Java 16 ile `record`:

```java
public record Point(int x, int y) {}
```

Tek satır ve derleyici otomatik üretir:
- `private final` field'lar (`x`, `y`)
- Bir canonical constructor (iki parametreli)
- **Getter'lar** (ama `getX()` değil, **`x()`**)
- `equals` ve `hashCode` (içeriğe göre)
- `toString` (`Point[x=3, y=5]` formatında)

Record'lar **immutable**'dır. Field'ları değiştiremezsin. İhtiyaç olursa `with` pattern'i uygulayabilirsin: `new Point(newX, oldPoint.y())`.

Validation eklemek istersen **compact constructor**:
```java
public record Point(int x, int y) {
    public Point {   // parametre listesi YOK
        if (x < 0 || y < 0) throw new IllegalArgumentException();
    }
}
```

DTO, value object, API response model gibi durumlarda record kullan, boilerplate'ten kurtul.

---

## 10) Initializer Blokları

Constructor dışında başlatma kodu yazmanın iki yolu:

### Instance Initializer
Her `new` ile çağrılır, constructor'dan önce:
```java
public class Foo {
    int x;
    { 
        x = 10;    // her nesne için çalışır
        System.out.println("instance init");
    }
    public Foo() { System.out.println("ctor"); }
}
```

### Static Initializer
Sınıf yüklendiğinde bir kere çalışır:
```java
static {
    System.out.println("sınıf yüklendi");
    // karmaşık static başlatma
}
```

Çalışma sırası: **static init → instance init → constructor**. Kalıtım varsa: parent'ın staticleri → alt'ın staticleri → parent instance init + ctor → alt instance init + ctor.

---

## 11) `Object` — Her Şeyin Atası

Java'da her sınıf, yazmasan bile `java.lang.Object`'ten türer. Bu yüzden her nesne miras olarak şu metodları alır:

- `toString()` — varsayılan `ClassName@hashHex`, override etmelisin.
- `equals(Object o)` — varsayılan `==` ile aynı (referans eşitliği).
- `hashCode()` — integer hash, HashMap gibi yapılar kullanır.
- `getClass()` — nesnenin sınıf bilgisini (`Class<?>`) verir.
- `clone()` — shallow copy, `Cloneable` gerektirir — önerilmez.
- `~~finalize()~~` — deprecated.
- `wait()`, `notify()`, `notifyAll()` — thread senkronizasyonu.

Bu metodların hiçbirini yazmasan da nesneler bunlara sahiptir. Çoğu sınıfta en azından `toString` ve `equals`/`hashCode` override edilir.

---

## Mülakat Soruları ve Örnek Cevaplar

**1. Sınıf ile nesne arasındaki fark nedir?**
Sınıf bir şablondur, tipi tanımlar ama kendi başına bir varlık değildir. Nesne o şablondan üretilmiş somut bir instance'dır, kendine ait state ve kimliği vardır, heap'te yaşar. `Car` sınıfı, `audi` nesnesi.

**2. Constructor'ın dönüş tipi nedir?**
Yoktur — `void` bile değildir. Bu constructor'ı normal metottan ayıran temel farktır. `return;` yazılabilir (erken çıkış için) ama `return value;` yazılamaz.

**3. No-arg constructor ne zaman otomatik eklenir?**
Sadece hiç constructor yazmadığında. Parametreli bir constructor yazdığın anda, otomatik no-arg constructor eklenmez. Gerekiyorsa elle eklersin.

**4. `this` ve `super` farkı?**
`this` şu anki nesnenin referansı — kendi field/metodlarına erişim, kendi başka constructor'ına delege. `super` üst sınıfın referansı — parent'ın field/metodlarına erişim, parent constructor'ına delege. Her ikisi de constructor çağrısı formunda (`this(...)`, `super(...)`) **ilk satır** olmak zorunda.

**5. Method overloading kuralları?**
Aynı isim, farklı imza (parametre tipleri veya sayısı). Dönüş tipi ve parametre adları imzanın parçası değil, tek başına overload sağlamaz. Derleme zamanında çözülür.

**6. Static method içinden instance method çağırabilir misin?**
Doğrudan değil çünkü static method'un `this`'i yoktur. Ama bir nesne referansın varsa onu üzerinden çağırabilirsin: `someObj.instanceMethod()`.

**7. `record` ile normal sınıf arasındaki fark?**
Record otomatik olarak immutable field'lar, getter'lar, equals, hashCode, toString üretir. Değiştirilemez (immutable) ve basit veri tutma için tasarlanmıştır. Normal sınıfta her şeyi elle yazarsın ve mutable olabilir.

**8. Inner class static nested class'tan nasıl farklıdır?**
Inner class outer'ın instance'ına bağlıdır (gizli referans tutar), outer nesnesi olmadan yaratılamaz. Static nested class bağımsızdır, outer nesnesi olmadan yaratılabilir. Bellek sızıntısını önlemek için genelde static nested tercih edilir.

**9. Nesne nasıl yok olur?**
Sana ait bir yok etme mekanizması yoktur. Tüm referansları kaybeden nesne **unreachable** hale gelir, GC bir noktada temizler. Kaynak temizleme (file, socket) için `AutoCloseable` implement et ve try-with-resources kullan.

**10. Constructor chaining nedir?**
Bir constructor'ın başka constructor'ı `this(...)` ile veya parent'ınınkini `super(...)` ile çağırması. Ortak başlatma mantığını tek yerde toplamak için kullanılır.
