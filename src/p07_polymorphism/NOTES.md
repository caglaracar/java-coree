# 07 — Polimorfizm (Polymorphism)

Polimorfizm, Yunanca'dan "çok biçimlilik" anlamına gelir. OOP'nin dört temel ilkesinden (inheritance, polymorphism, encapsulation, abstraction) belki de **en güçlü** olanıdır. Kalıtımı öğrendiysen ama polimorfizmi anlamadıysan, Java'nın asıl büyüsünü kaçırıyorsun. Bu paketin sonunda "Animal bir metoda geçilse bile içindeki gerçek tip için doğru davranışın çalışması" olgusunu gerçekten anlayacaksın.

---

## 1) Polimorfizmin Kalbi — Aynı İsim, Farklı Davranış

Temel fikir: **aynı çağrı, nesnenin gerçek tipine göre farklı davranır**.

Bir hayvanat bahçesi uygulaması düşün. Yüzlerce hayvan var. Hepsinin `sound()` metodu var. Köpek havlar, kedi miyavlar, kuş öter. Eğer polimorfizm olmasa şöyle yazmak zorunda kalırdın:

```java
if (animal instanceof Dog) {
    ((Dog) animal).bark();
} else if (animal instanceof Cat) {
    ((Cat) animal).meow();
} else if (animal instanceof Bird) {
    ((Bird) animal).sing();
}
```

Yeni bir hayvan türü eklediğinde buraya bir `else if` daha eklemek zorundasın. Yüzlerce yerde böyle kontroller. Bakımı cehennem. Polimorfizm bu sorunu eritir:

```java
animal.sound();   // runtime'da doğru tip için doğru sound() çağrılır
```

Sen `Animal` sınıfında `sound()` tanımlarsın, her alt sınıf (Dog, Cat, Bird) kendi versiyonunu yazar. `animal.sound()` çağrıldığında JVM **nesnenin gerçek tipine bakar** ve o tipe ait metodu çalıştırır. Yeni hayvan eklersen? Sadece `extends Animal` + `override sound()` yap, mevcut kod değişmez.

Bu prensip **Open/Closed Principle**'ın kalbidir: kod yeni türlere **açık**, mevcut davranışa **kapalı**.

---

## 2) İki Tür Polimorfizm

Java'da iki farklı polimorfizm türü vardır ve genelde birbirine karıştırılır:

### a) Compile-Time Polymorphism — Method Overloading
Aynı sınıfta aynı isimli, farklı imzalı metodlar. Derleyici hangisinin çağrılacağını **derleme zamanında** belirler.

```java
public class Calculator {
    int add(int a, int b) { return a + b; }
    double add(double a, double b) { return a + b; }
    int add(int a, int b, int c) { return a + b + c; }
    String add(String a, String b) { return a + b; }
}

calc.add(2, 3);         // int versiyonu — compile-time'da kararlaştırılır
calc.add(2.5, 3.5);     // double versiyonu
calc.add("a", "b");     // string versiyonu
```

Derleyici hangi metodu çağıracağını **argüman tiplerinden** çıkarır. Runtime'da bir şey değişmez; bytecode'da hangi metodun çağrılacağı zaten sabitlenmiştir.

### b) Runtime Polymorphism — Method Overriding + Upcasting
Asıl "sihir" burada. Üst sınıf referansı alt sınıf nesnesini tutar, metot çağrısında **nesnenin gerçek tipindeki** override edilmiş versiyon çalışır.

```java
Animal a = new Dog();
a.sound();      // "Hav!" — Dog.sound() çalıştı, Animal.sound() değil!
```

Bu davranışa **dynamic method dispatch** veya **virtual method invocation** denir. JVM, `a` referansının derleme zamanı tipine değil, **runtime'da gösterdiği nesneye** bakar.

Aslında Java'da her non-static, non-private, non-final metod "virtual"dır — yani polimorfik çağrıya tabidir. C++'ta `virtual` keyword'ü yazmak zorundaydın, Java default olarak vermiş.

---

## 3) Runtime Polymorphism Nasıl Çalışır? (İç Mekanizma)

Her sınıfın bellek içinde gizli bir **Virtual Method Table (vtable)** vardır. Bu tabloda sınıfın tüm metodlarının adresleri tutulur. Alt sınıf bir metot override ederse, kendi vtable'ında o metodun adresi değiştirilir.

```
Animal.vtable:          Dog.vtable:
  sound() -> Animal.sound   sound() -> Dog.sound   ← override edildi
  eat()   -> Animal.eat     eat()   -> Animal.eat  ← miras
```

`a.sound()` çağrıldığında JVM şunu yapar:
1. `a`'nın gerçek nesnesine bak (heap'te hangi sınıfın instance'ı?).
2. O sınıfın vtable'ına git.
3. `sound()`'un adresini oradan al.
4. O metodu çağır.

Bu işlem birkaç pointer dereference ile biter, çok hızlıdır. Hatta JIT compiler, tek bir alt sınıf tespit ederse vtable lookup'ını bile atlayıp doğrudan inline eder (monomorphic call site optimization).

---

## 4) Polimorfizmin İstisnaları — Override Edilmeyen Şeyler

Her şey polimorfik değildir. Şu durumlarda beklediğin dynamic dispatch **olmaz**:

### a) Static Metodlar → Hiding
```java
class Parent {
    static void hello() { System.out.println("Parent"); }
}
class Child extends Parent {
    static void hello() { System.out.println("Child"); }
}

Parent p = new Child();
p.hello();    // "Parent" — referans tipi ne ise o çalışır!
```

Static metodlar nesneye değil **sınıfa** aittir. `p.hello()` aslında `Parent.hello()` gibi derlenir. Buna **method hiding** denir, override değil. IDE'n genelde `p.hello()` yazmak yerine `Parent.hello()` yazmanı önerir çünkü yanıltıcıdır.

### b) Private Metodlar
Private metodlar alt sınıftan görünmez. Alt sınıfta aynı isimli metod yazarsan yeni bir metot olur, override değildir. Polimorfik dispatch yapılmaz.

### c) Final Metodlar
Final metodlar zaten override edilemez. Dynamic dispatch söz konusu değildir.

### d) Field'lar (Shadowing)
En kritik noktalardan biri: **field'lar polimorfik değildir**.
```java
class Parent { String type = "Parent"; }
class Child extends Parent { String type = "Child"; }

Parent p = new Child();
System.out.println(p.type);    // "Parent" — referans tipi belirler!
```

İki ayrı `type` field'ı var (biri Parent'ta, diğeri Child'da — gizler/shadows). Hangisinin okunduğu **referans tipine** göredir. Bu yüzden aynı isimde field kullanmaktan kaçın; override sanıp yanılabilirsin. Field'ları `private` tut ve getter kullan — getter polimorfiktir.

### e) Constructor'lar
Constructor override edilmez. Her sınıfın kendi constructor'ları vardır.

---

## 5) Polimorfizmin En Güzel Kullanımı — Genelleştirilmiş Kod

Polimorfizmin pratik faydası: **metotların belirli tiplerle değil, soyut tiplerle konuşması**.

```java
public static double totalArea(List<Shape> shapes) {
    double sum = 0;
    for (Shape s : shapes) {
        sum += s.area();   // her shape'in kendi area'sı çağrılır
    }
    return sum;
}
```

`totalArea` metodu `Circle`, `Square`, `Triangle` veya henüz yaratılmamış hiçbir türü bilmez. Sadece `Shape` arayüzüyle konuşur. Yeni bir tür (`Hexagon`) eklediğinde bu metodu **bir milimetre bile değiştirmen gerekmez**. Yeni sınıf otomatik uyumlu.

Bu, tüm OOP tasarım örüntülerinin (Strategy, Observer, Template Method, Visitor...) temelidir.

---

## 6) `instanceof` ve Pattern Matching

Bazen upcast edilmiş bir referansı alt tipe indirmek gerekir. Önce kontrol, sonra cast:

```java
// Eski yöntem
if (shape instanceof Circle) {
    Circle c = (Circle) shape;
    System.out.println("yarıçap: " + c.getRadius());
}

// Java 16+ pattern matching — daha temiz
if (shape instanceof Circle c) {
    System.out.println("yarıçap: " + c.getRadius());
}
```

Pattern matching hem null'a karşı güvenlidir (`null instanceof X` her zaman false) hem de cast'i otomatik yapar. Artık bunu kullan.

**Uyarı**: `instanceof` bazlı uzun zincirler genelde polimorfizmin **yetersiz kullanımının** işaretidir. "`if instanceof X` yapıyorsan, muhtemelen o davranışı sınıfın içine koymamışsındır". Doğru yaklaşım çoğu zaman sınıfa yeni bir metod eklemektir.

---

## 7) Switch Pattern Matching (Java 21+) — Polimorfizmin Modern Formu

Java 21'den itibaren switch de pattern matching destekliyor:

```java
Object o = getSomething();
String result = switch (o) {
    case Integer i when i > 0 -> "pozitif int: " + i;
    case Integer i            -> "int: " + i;
    case String s             -> "string: " + s;
    case List<?> list         -> "liste, boyut=" + list.size();
    case null                 -> "null";
    default                   -> "bilinmez";
};
```

Sealed class'larla birleştirince derleyici **exhaustiveness** kontrolü yapar — eksik bir alt tipi unutursan derleme hatası alırsın. Bu da polimorfizmin alternatif bir yüzüdür.

---

## 8) Sealed Classes (Java 17+) — Polimorfizmi Sınırlama

Normalde herkes senin sınıfından türeyebilir. Ama bazen "bu hiyerarşiye sadece şu 3 sınıf dahil olabilir" demek istersin. Java 17 bunu **sealed class** ile getirdi:

```java
public sealed class Shape permits Circle, Square, Triangle {}

public final class Circle extends Shape { ... }
public final class Square extends Shape { ... }
public final class Triangle extends Shape { ... }
```

Artık Shape'ten başkası türeyemez. Faydaları:
- Switch expression exhaustive olur (derleyici tüm durumların kapandığını garanti eder).
- API tasarımında kontrollü genişleme.
- ADT (Algebraic Data Type) benzeri kullanım — Kotlin'deki sealed class, Rust'taki enum gibi.

---

## 9) Overriding Kuralları — Detaylı İnceleme

Bir metodu override ederken:

**a) İmza aynı kalmalı.** Farklı parametre = overload olur, override değil.

**b) Dönüş tipi aynı veya alt tip (covariant return).**
```java
class Parent { Animal get() { ... } }
class Child extends Parent { Dog get() { ... } }   // OK, Dog Animal'ın alt tipi
```

**c) Erişim daraltılamaz.**
```java
class Parent { public void foo() { ... } }
class Child extends Parent { 
    private void foo() { ... }   // HATA — public idi, daralttın
    protected void foo() { ... } // HATA — hâlâ daha dar
    public void foo() { ... }    // OK, aynı veya genişletme
}
```

**d) Atılan checked exception'lar daraltılabilir ama genişletilemez.**
```java
class Parent { 
    void read() throws IOException { ... }
}
class Child extends Parent { 
    void read() throws FileNotFoundException { ... }   // OK, daha spesifik
    void read() throws Exception { ... }               // HATA — daha geniş
}
```
Sebep: caller, parent'ın imzasına göre IOException bekliyordu, alt sınıf daha geniş atsa yakalanmamış exception olurdu.

**e) `final`, `static`, `private` metodlar override edilemez.**

---

## 10) Polimorfizmin Pattern'leri

**Strategy Pattern** — Davranışı runtime'da seç:
```java
interface Payment { void pay(double amount); }
class CreditCard implements Payment { public void pay(double a) { ... } }
class PayPal implements Payment     { public void pay(double a) { ... } }

Payment p = getSelectedPayment();
p.pay(100);   // hangisi olduğunu bilmiyoruz, polimorfizm halleder
```

**Template Method** — Parent iskeleti çizer, alt sınıf dolgu yapar:
```java
abstract class Game {
    public final void play() {     // template
        initialize();
        startPlay();
        endPlay();
    }
    abstract void initialize();
    abstract void startPlay();
    abstract void endPlay();
}
```

**Observer** — Nesneler aynı interface'i implement edip olay dinler.

Bu pattern'ler polimorfizm olmadan imkânsızdır.

---

## Mülakat Soruları ve Örnek Cevaplar

**1. Overloading ile overriding arasındaki farklar?**
Overloading aynı sınıfta aynı isimli farklı imzalı metodlardır, compile-time'da çözülür. Overriding alt sınıfta parent'ın aynı imzalı metodunu yeniden yazmaktır, runtime'da dynamic dispatch ile çözülür. Overloading "ne yapılacak" seçimini derleyici yapar, overriding "kim yapacak" seçimini JVM çalışırken yapar.

**2. Java'da dynamic method dispatch ne demek?**
JVM'in, bir metot çağrısında referansın tipine değil, nesnenin gerçek tipine bakarak çağrılacak metodu seçmesi. Üst sınıf referansı + alt sınıf nesnesi + override edilmiş metod üçlüsünde gerçekleşir. Altında virtual method table (vtable) vardır.

**3. Static metod override edilir mi?**
Hayır. Aynı isimli static metod yazmaya **method hiding** denir. Dynamic dispatch uygulanmaz, referans tipi belirleyicidir. `Parent p = new Child(); p.staticMethod();` Parent'ınkini çağırır. IDE'n bu çağrıyı genelde `Parent.staticMethod()` olarak yazmanı önerir.

**4. Private metot override edilir mi?**
Hayır. Private metot alt sınıftan görünmez. Aynı isimli metod yazarsan bağımsız yeni bir metod olur, override değil.

**5. Field'lar polimorfik mi?**
Hayır. Field'lar referans tipine göre erişilir (static binding). Alt sınıfta aynı isimle field tanımlamak parent'ı **shadow** eder. Her iki field de nesnede ayrı bellekte tutulur. Bu yüzden field'ları private tutup getter polimorfizmi kullanmak daha güvenlidir.

**6. Constructor polimorfik mi?**
Hayır, constructor override edilmez. Her sınıfın kendi constructor'ları vardır. Alt sınıf `super(...)` ile parent'ınkini çağırır.

**7. Covariant return type nedir?**
Override'da dönüş tipinin parent'ınkinden daha dar (alt tip) olmasına izin verilmesi. Java 5+'da desteklenir. Alt sınıf kullanıcılarına cast'siz daha spesifik tip döner: `Dog clone()` overrideı `Animal clone()`'a göre covariantdir.

**8. Polimorfizmin iki türü nedir?**
**Compile-time (static)** — method overloading. **Runtime (dynamic)** — method overriding + upcasting. İkisinin mekanizmaları tamamen farklıdır. Klasik "polymorphism" denince çoğunlukla ikincisi kastedilir.

**9. `instanceof` kullanımı ne zaman kod kokusu olur?**
Bir hiyerarşide sürekli `if (x instanceof A) else if (x instanceof B)` zincirleri polimorfizmi doğru kullanmadığının işaretidir. Genelde o davranışı soyut bir metot haline getirip alt sınıflarda implement ettirmek daha iyidir. Visitor pattern veya sealed class + switch alternatifler sunar.

**10. Sealed class polimorfizme ne katar?**
Hangi sınıfların hiyerarşiye dahil olabileceğini kısıtlar. Bu sayede switch/pattern matching exhaustive hale gelir — derleyici tüm olası durumların kapandığını garanti eder. Controlled polymorphism sağlar, ADT benzeri kullanıma imkan verir.
