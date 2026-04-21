# 06 — Kalıtım (Inheritance)

Kalıtım, OOP'nin "IS-A" (ŞUDUR) ilişkisini kuran mekanizmadır. Bir `Dog`, bir `Animal`'dır. Bir `Manager`, bir `Employee`'dır. Bir `Circle`, bir `Shape`'tir. Bu ilişki sayesinde ortak davranışları üst sınıfta toplar, alt sınıflara otomatik miras veririz. Kod tekrarından kurtuluruz ama bundan çok daha önemlisi — **polimorfizmin kapısını açarız**, ki bu Java'nın gerçek gücüdür.

---

## 1) Kalıtımın Amacı

Diyelim ki bir hayvan barınağı sistemi yazıyorsun. Başta sadece köpekler vardı:

```java
public class Dog {
    String name;
    int age;
    void eat() { ... }
    void sleep() { ... }
    void bark() { ... }
}
```

Sonra kedi geliyor, sonra kuş, sonra balık... Her seferinde `name`, `age`, `eat()`, `sleep()` kodunu tekrar yazmak zorunda mısın? Hayır! Hepsinin ortak olan kısmını bir `Animal` sınıfında topla, her biri onu **miras alsın**:

```java
public class Animal {
    String name;
    int age;
    void eat() { System.out.println(name + " yiyor"); }
    void sleep() { System.out.println(name + " uyuyor"); }
}

public class Dog extends Animal {
    void bark() { System.out.println(name + ": Hav!"); }
}

public class Cat extends Animal {
    void meow() { System.out.println(name + ": Miyav!"); }
}
```

`Dog` otomatik olarak `name`, `age`, `eat()`, `sleep()` sahibidir — bunları tekrar yazmaya gerek yok. Sadece köpeklere özel olan `bark()`'ı ekliyorsun.

Faydaları:
1. **Kod tekrarı azalır** (DRY — Don't Repeat Yourself).
2. **Polimorfizm** mümkün olur: `Animal` referansıyla tüm alt tipleri tutabilirsin. Bu bir sonraki pakette.
3. **Bakım kolaylaşır** — `eat()` davranışını değiştirmen gerektiğinde tek yerde değiştirirsin, tüm alt sınıflar otomatik güncellenir.

---

## 2) `extends` Anahtar Sözcüğü

Java'da kalıtım `extends` ile yapılır:

```java
public class Dog extends Animal { ... }
```

Dog artık "**Animal'ın yaptığı her şeyi yapar + ekstra özellikleri vardır**". Animal'ın `public` ve `protected` üyelerine doğrudan erişebilir. `private` üyeleri miras ALIR ama **doğrudan erişemez** — getter/setter ile erişmelisi (kapsüllemeyi bozmaz).

### Tek Kalıtım Kuralı

Java'da **bir sınıf yalnızca bir sınıftan türer**. `class Dog extends Animal, Pet` yasaktır. Bunun gerekçesi C++'ın çoklu kalıtımında görülen **Diamond Problem**'dir (aynı dedeyi iki farklı yoldan miras almak kafa karışıklığı ve belirsizlik yaratır). Java bunu dil seviyesinde yasaklayarak bu sorundan kurtuldu.

Ama "çoklu kalıtım" ihtiyacı Java'da **interface'ler** ile çözülür. Bir sınıf birden fazla interface implement edebilir. Bunu 8. pakette göreceğiz.

### Her Sınıf `Object`'in Torunu

Hiçbir şey `extends` etmesen bile, derleyici sessizce `extends Object` ekler. Yani Java'daki her sınıf — doğrudan veya dolaylı — `java.lang.Object`'in torunudur. Bu yüzden her nesnede `toString`, `equals`, `hashCode` gibi metodlar vardır.

```
Object
  ├── Animal
  │     ├── Dog
  │     ├── Cat
  │     └── Bird
  │           └── Parrot
  └── Vehicle
        ├── Car
        └── Bike
```

---

## 3) `super` — Üst Sınıfa Erişim

Alt sınıftan üst sınıfın üyelerine erişmek için `super` kullanılır. Üç yaygın kullanımı var:

### a) Üst Sınıf Constructor'ını Çağırma
```java
public class Dog extends Animal {
    String breed;
    
    public Dog(String name, int age, String breed) {
        super(name, age);      // Animal(name, age)'i çağır
        this.breed = breed;
    }
}
```
**`super(...)` constructor'ın ilk satırı olmak zorundadır**. Çağırmazsan derleyici otomatik `super();` ekler; eğer parent'ın no-arg constructor'ı yoksa derleme hatası alırsın.

Sebep: nesnenin oluşumu **tepeden aşağı** yapılır. Önce Object, sonra Animal, sonra Dog başlatılır. Parent doğru başlatılmadan alt sınıf kendi alanlarını başlatamaz çünkü parent'ın alanlarına ihtiyaç duyabilir.

### b) Override Edilmiş Metodun Üst Versiyonuna Çağrı
```java
public class Dog extends Animal {
    @Override
    public void eat() {
        super.eat();                        // Animal.eat() çalıştır
        System.out.println("ekstra kemik"); // sonra kendi davranışımı ekle
    }
}
```
Bu "önce atamın yaptığını yap, sonra kendimin ekini ekle" şeklindedir ve **template method** pattern'inin temelidir.

### c) Shadowed Field'a Erişim
```java
class A { int x = 10; }
class B extends A { 
    int x = 20;             // A.x'i gizler (shadow)
    void show() {
        System.out.println(x);       // 20 (kendim)
        System.out.println(super.x); // 10 (A'nın)
    }
}
```
Field'lar override değil **shadow** edilir (polimorfik değildir). İki tane `x` var, hangi referans tipindense o çağrılır. Bu çoğu zaman hatanın göstergesidir — aynı isimde field kullanmaktan kaçın.

---

## 4) Constructor Zinciri — Nesne Oluşumunun Sırası

Alt sınıf nesnesi yaratıldığında olanları adım adım görelim:

```java
class A {
    A() { System.out.println("A ctor"); }
}
class B extends A {
    B() { System.out.println("B ctor"); }
}
class C extends B {
    C() { System.out.println("C ctor"); }
}

new C();
```

Çıktı:
```
A ctor
B ctor
C ctor
```

Neden bu sırada? Çünkü `C()` çağrıldığında derleyici otomatik `super()` ekler (yazılmamışsa). `B()` çağrılır. O da `super()` ile `A()`'yı çağırır. A'nın parent'ı Object, Object'in ctor'u sessizce çalışır. Sonra A bitirir, B devam eder, C devam eder. **Yani üstten aşağı**.

Bu sırayı **değiştiremezsin** — `super(...)` ilk satırda olmak zorunda. Bu sayede alt sınıf asla "yarı-başlatılmış" bir parent üzerinde çalışmaz.

---

## 5) Method Overriding — Davranışı Yeniden Tanımlama

**Overriding**, alt sınıfın üst sınıftaki bir metodu **aynı imzayla yeniden yazmasıdır**. Amacı: üst sınıfın davranışını alt sınıfa özel şekilde değiştirmek.

```java
class Animal {
    public void sound() { System.out.println("genel ses"); }
}
class Dog extends Animal {
    @Override
    public void sound() { System.out.println("Hav!"); }
}
class Cat extends Animal {
    @Override
    public void sound() { System.out.println("Miyav!"); }
}
```

### Override Kuralları
1. **İmza aynı olmalı** (isim + parametre tipleri).
2. **Dönüş tipi aynı veya alt tip** olabilir (covariant return).
3. **Erişim daraltılamaz**: parent public ise alt private yapamaz. Genişletebilir.
4. **Atılan checked exception'lar daha az veya daha spesifik olabilir**. Yeni, daha geniş exception atamaz.
5. **`static`, `private`, `final` metodlar override edilemez**.
   - Static → **hiding** (saklama) — dinamik dispatch yok.
   - Private → alt sınıf parent'ın private'ını görmez, farklı bir metod olur.
   - Final → dil seviyesinde yasak.

### `@Override` Anotasyonu

Her override için `@Override` yaz. Anotasyon derleyiciye "ben override yapmak istiyorum" der. Eğer imzayı yanlışsa (yazım hatası, parametre eksik), derleyici hata verir. Bu olmadan aynı hata sessizce yeni bir metod yaratmaya dönüşür.

```java
@Override
public void sound() { ... }          // doğru

@Override
public void sond() { ... }           // DERLEME HATASI — parent'ta sond yok
```

Anotasyonu atlarsan ve yazım hatası yaparsan derleme başarılı olur ama çalışmaz. `@Override` senin kurtarıcındır.

### Covariant Return Type
Override ederken dönüş tipi alt tipe daraltılabilir:

```java
class Animal {
    public Animal clone() { return new Animal(); }
}
class Dog extends Animal {
    @Override
    public Dog clone() { return new Dog(); }   // dönüş tipi alt tip — LEGAL
}
```

Bu sayede alt sınıfta cast'e gerek kalmaz.

---

## 6) Override vs Overload — Karıştırılmaması Gereken İkili

| | Overload | Override |
|---|---|---|
| Nerede | Aynı sınıf | Alt sınıf |
| İmza | **Farklı** | **Aynı** |
| Dönüş tipi | Fark etmez | Aynı ya da alt tip |
| Erişim | Fark etmez | Daraltılamaz |
| Zamanı | Compile-time | Runtime (dynamic dispatch) |
| `static` metodlarda | Evet | Hayır (hiding) |

Overload aynı sınıfta aynı isimle farklı işler (`add(int, int)`, `add(double, double)`). Override kalıtımda aynı imzanın alt sınıftaki yeniden tanımlanmasıdır.

---

## 7) `Object` Sınıfının Önemli Metodları

Her sınıf `Object`'ten miras aldığı için bu metodları değerlendirmelisin:

### `toString()`
Nesneyi string olarak temsil eder. Varsayılan: `Car@1a2b3c` gibi bir şey. Override et:
```java
@Override
public String toString() {
    return "Car[brand=" + brand + ", color=" + color + "]";
}
```
IDE'n hazır üretir. `System.out.println(obj)` aslında `obj.toString()` çağırır.

### `equals(Object other)`
İki nesne içerikçe eşit mi? Varsayılan `==` (referans eşitliği). **Override etmen gerekir** aksi halde HashSet, equals kontrolü olan algoritmalar beklediğin gibi çalışmaz.

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Car c)) return false;
    return Objects.equals(brand, c.brand) && Objects.equals(color, c.color);
}
```

### `hashCode()`
Nesnenin hash'i — `HashMap`, `HashSet` bunu kullanır. **`equals` override ettiğinde `hashCode`'u da ETMEK ZORUNDASIN**. Kural: `a.equals(b)` ise `a.hashCode() == b.hashCode()` olmalı. Aksi halde HashSet'e koyduğun nesneyi `contains` ile bulamazsın! (11. paketteki demo bunu görsel olarak gösteriyor.)

```java
@Override
public int hashCode() {
    return Objects.hash(brand, color);
}
```

### `getClass()`
Nesnenin sınıf bilgisini döner. Reflection için başlangıç noktası.

### `clone()`
Shallow copy. `Cloneable` interface implement etmen gerek. Pratikte önerilmez — kopya constructor veya factory method yaz.

### `wait()`, `notify()`, `notifyAll()`
Thread senkronizasyonu için (13. paket).

### ~~`finalize()`~~
GC'den önce çağrılırdı. Java 9'dan beri deprecated. Asla kullanma.

---

## 8) Upcasting ve Downcasting

Kalıtım sayesinde alt sınıf nesnesi üst sınıf referansında tutulabilir:

```java
Animal a = new Dog("Karabaş", 3);    // UPCASTING
```

Bu **güvenli** ve otomatiktir — Dog, Animal'ın tüm özelliklerini zaten sağlar. Ama artık `a` üzerinden sadece **Animal**'a ait metodları çağırabilirsin:
```java
a.eat();        // OK, Animal'da var
a.bark();       // HATA — Animal'da bark yok
```

Bu kısıtlama rahatsız edici görünebilir ama **polimorfizmin temelidir**. Bir metoda `Animal` verirsin, içerde köpek mi kedi mi bilmene gerek yoktur.

Tekrar geri almak için **downcasting**:
```java
Animal a = new Dog("Karabaş", 3);
Dog d = (Dog) a;
d.bark();
```

Downcasting **tehlikelidir** — `a` gerçekten `Dog` değilse `ClassCastException` alırsın:
```java
Animal a = new Cat("Pamuk", 2);
Dog d = (Dog) a;   // ClassCastException!
```

Güvenli yol `instanceof` kontrolü:
```java
if (a instanceof Dog d) {     // Java 16+ pattern matching
    d.bark();
}
```

---

## 9) Composition vs Inheritance — Modern OOP'nin Tavsiyesi

Yıllar içinde öğrenilen acı ders: **kalıtım fazla kullanılmamalı**. Modern tasarım prensibi: *"Favor composition over inheritance"* (kalıtım yerine bileşim).

**Kalıtım:** "IS-A" — `Dog` bir `Animal`'dır.
**Composition:** "HAS-A" — `Car` bir `Engine`'e sahiptir (içerir).

Kalıtımın dezavantajları:
- **Tight coupling** — parent değiştiğinde child'lar otomatik etkilenir. Parent'ta yeni bir metod eklediğinde beklenmedik override sorunları çıkabilir.
- **Fragile base class** — base class'ta bir davranış değişirse tüm alt sınıflar bozulabilir.
- **Tek kalıtım kısıtı** — sadece bir parent'tan türeyebilirsin.
- **Kapsülleme bozulur** — alt sınıf parent'ın protected içine erişir.

Composition alternatif:
```java
public class Car {
    private final Engine engine;     // Car bir Engine'e sahiptir
    public Car(Engine engine) { this.engine = engine; }
    public void start() { engine.ignite(); }
}
```
Engine'i değiştirmek istersen, Car'ı bozmadan yaparsın. Esneklik daha yüksek.

**Pratik kural**: gerçekten "IS-A" ilişkisi varsa ve davranışın çoğu paylaşılıyorsa kalıtım kullan. Aksi halde composition yap + interface ile "davranış sözleşmesi" tanımla.

---

## 10) `final` Kalıtımı Engeller

İki seviyede `final`:

**`final class`** — alt sınıfı yasaklar:
```java
public final class String { ... }
// class MyString extends String {}   // HATA
```
Neden? String gibi kritik sınıfların davranışı değişirse güvenlik açığı doğar.

**`final method`** — override'ı yasaklar:
```java
public final void criticalOperation() { ... }
// Alt sınıf bu metodu yeniden yazamaz
```
Bir davranış değişmemesi gerekiyorsa final yap.

Performans yan etkisi: JIT, final metodları daha agresif optimize eder (inline gibi). Ama bu mikro-optimizasyon, genelde final'ı tasarım/güvenlik gerekçesiyle koyarsın.

---

## 11) Abstract Sınıflar — Kısa Değini

Bazen bir üst sınıf **kısmi** olmalıdır: bir kısmı tanımlı, diğer kısmı alt sınıfın doldurması gereken. Buna **abstract class** denir:

```java
public abstract class Shape {
    public abstract double area();    // gövdesi yok, alt sınıf doldurur
    public void describe() { System.out.println("Alan: " + area()); }
}
```

Abstract class'tan **nesne oluşturulamaz** (`new Shape()` yasaktır). Alt sınıfta abstract metod doldurulmalı. Bunu 8. pakette detaylıca göreceksin.

---

## Mülakat Soruları ve Örnek Cevaplar

**1. Java çoklu kalıtımı destekler mi?**
Sınıflar için **hayır** (Diamond Problem'den kaçınmak için). Bir sınıf tek bir sınıftan türeyebilir. Ama interface'ler için **evet** — bir sınıf birden fazla interface implement edebilir. Bu sayede çoklu davranış sözleşmeleri olur.

**2. `super()` ne zaman otomatik eklenir?**
Alt sınıfın constructor'ında **ilk satırda** `this(...)` veya `super(...)` yoksa, derleyici otomatik `super();` ekler. Bu parent'ın no-arg constructor'ını çağırır. Parent'ta no-arg yoksa derleme hatası alırsın.

**3. `@Override` anotasyonunun faydası?**
Derleyiciye "bu metod override etmeli" der. İmza hatası (yazım, eksik parametre) yaptıysan derleme hatası verir. Anotasyon olmasa hata yeni bir metod oluşturarak sessizce geçer. `@Override` kurtarıcıdır.

**4. `equals` override edildiğinde neden `hashCode`'u da override etmeliyiz?**
Kontratın bir kuralı: `a.equals(b)` true ise `a.hashCode() == b.hashCode()` olmalı. HashMap/HashSet hashCode'u kullanarak bucket seçer; eşit sayılan iki nesne farklı bucket'larda olursa `contains` bulamaz, `get` null döner. Kontrat bozulursa koleksiyonlar sessizce yanlış çalışır.

**5. Covariant return type nedir?**
Override edilen metodun dönüş tipinin parent'ınkinden **daha dar** (alt tip) olması. Java 5'ten beri izinli. `Animal clone()` metodunu Dog'da `Dog clone()` olarak override edebilirsin. Alt sınıf kullanıcılarına cast'siz daha spesifik tip döner.

**6. Constructor miras alınır mı?**
**Hayır**, constructor miras alınmaz. Alt sınıf parent constructor'ını `super(...)` ile çağırır ama kendi ayrı constructor'ını yazmak zorundadır. Abstract class'ların bile constructor'ları vardır (alt sınıf super ile çağırmak için).

**7. Private method override edilir mi?**
Hayır. Private metot alt sınıftan görünmez, aynı isimle yazarsan bu yeni bir metottur — override değil. Polimorfik dispatch yapılmaz. Benzer şekilde static metotlar da override edilmez, **hiding** olur.

**8. `Object` sınıfında kaç tane metod vardır?**
Yaklaşık 11: `toString`, `equals`, `hashCode`, `getClass`, `clone`, `finalize`, `wait` (3 overload), `notify`, `notifyAll`. Bazısı final (wait, notify, getClass), bazısı native.

**9. Kalıtım mı composition mı?**
"IS-A" gerçekten varsa ve davranış paylaşımı yoğunsa kalıtım. Aksi durumda composition + interface daha esnek ve sürdürülebilir. Modern pratik "composition favor" der.

**10. `final class` neden kullanılır?**
Davranışın değişmesini önlemek (güvenlik, immutability), performans (JIT agresif optimize eder), API tasarımında sağlam (stable) contract sunmak için. `String`, `Integer`, `LocalDate` gibi kritik sınıflar final'dır.
