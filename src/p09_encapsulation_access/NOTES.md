# 09 — Encapsulation, Access Modifiers, Immutability

Encapsulation (kapsülleme), OOP'nin dört temel ilkesinden biridir ve bana sorarsan **en çok ihmal edileni**. Herkes inheritance ve polymorphism'i konuşur ama iyi bir yazılımın asıl dayanağı doğru kapsüllemedir. "Veriyi sakla, davranışı aç" prensibi kodunun gelecekte değişebilirliğini, güvenliğini ve anlaşılabilirliğini belirler. Bu paketle sınıflarını **dışarıdan saldırılara ve yanlış kullanıma karşı zırhlayacaksın**.

---

## 1) Encapsulation Nedir? Hangi Problemi Çözer?

**Kapsülleme**: Bir sınıfın verisini dışarıdan saklamak, sadece kontrollü yollarla erişime izin vermek.

Düşün: bir banka hesabı sınıfı yazıyorsun.
```java
// Kötü tasarım
public class BankAccount {
    public double balance;    // herkes erişir!
}

// Kullanım
BankAccount a = new BankAccount();
a.balance = -5_000_000_000.0;    // Hesabın sahibine yazık
a.balance = Double.NaN;          // Artık balance NaN, her işlem bozulur
```

`balance` public olduğu için herkes istediği değeri koyar. Validasyon yok, iş kuralı yok, loglama yok. Bu tasarım felakettir.

İyi tasarım:
```java
public class BankAccount {
    private double balance;      // gizli
    
    public double getBalance() { return balance; }
    
    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("pozitif olmalı");
        balance += amount;
    }
    
    public void withdraw(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("pozitif olmalı");
        if (amount > balance) throw new IllegalStateException("yetersiz bakiye");
        balance -= amount;
    }
}
```

Artık:
- `balance`'a doğrudan yazılamaz — sadece deposit/withdraw üzerinden.
- Her değişiklik **validasyondan** geçer.
- Negatif para yatırma, hesaptan fazla çekme mümkün değil.
- Yarın logging eklemek istersen tek yerde eklersin.
- İç yapıyı değiştirmek (mesela balance'ı BigDecimal'a çevirmek) dış dünyayı etkilemez.

Encapsulation'ın beş büyük faydası:
1. **Veri bütünlüğü** (invariant koruma).
2. **Değişkenlik** (implementation değiştirilebilir, API sabit kalır).
3. **Loglama, senkronizasyon, cache** merkezileştirilir.
4. **Güvenlik** — yetki kontrolleri metotlarda.
5. **Okunabilirlik ve bakım** — davranış field'la değil metod adıyla ifade edilir.

---

## 2) Access Modifiers — Dört Erişim Seviyesi

Java'da bir üyenin (field, method, nested class) kimin erişebileceğini belirleyen dört seviye vardır:

| Modifier | Aynı sınıf | Aynı paket | Alt sınıf (farklı paket) | Her yer |
|---|---|---|---|---|
| `private`     | ✅ | ❌ | ❌ | ❌ |
| *(default)*   | ✅ | ✅ | ❌ | ❌ |
| `protected`   | ✅ | ✅ | ✅ | ❌ |
| `public`      | ✅ | ✅ | ✅ | ✅ |

### `private` — Sadece Ben
```java
public class Foo {
    private int secret = 42;
    private void helper() { ... }
}
```
Aynı sınıfın dışından (alt sınıf dahil, aynı paket dahil) görülmez. Implementation detayları için ideal. **Field'ların çoğu private olmalı**.

İstisna: Aynı sınıfın farklı nesneleri birbirinin private'ına erişebilir:
```java
public class Point {
    private int x;
    boolean equals(Point other) {
        return this.x == other.x;   // other.x private ama aynı sınıf, erişim var
    }
}
```

### *(default)* — Aynı Paket (Package-Private)
Hiçbir modifier yazmazsan bu olur.
```java
class Foo { }              // default class
int counter;               // default field
void helper() { ... }      // default method
```
Aynı paketteki herkes erişir, dışından kimse erişemez. Paket içi yardımcı sınıflar için. Bir kütüphane yazarken "public API" dışında tuttuğun iç araçlara uygulanır.

### `protected` — Aile (Paket + Alt Sınıflar)
```java
protected int sharedState;
protected void template() { ... }
```
Aynı paket + **farklı paketteki alt sınıflar** erişebilir. Alt sınıfların ihtiyaç duyacağı ama rastgele dış kodun kullanmaması gereken üyeler için. Abstract class'ta template method'ların yardımcıları tipik olarak protected olur.

### `public` — Herkese
```java
public class Api { }
public void launch() { ... }
```
Her yerden erişilir. Public API yüzün olur. Bir kez public yaptığın bir şeyi geri almak zor — tüm kullanıcılarını bozar. Dikkatli seç.

### Sınıf Seviyesi
Top-level class sadece **`public`** veya **default** olabilir. `private` veya `protected` top-level class **yasaktır**:
```java
public class A { }            // OK
class B { }                   // OK (default)
private class C { }           // HATA
```
(Inner class'lar private/protected olabilir.)

---

## 3) Getter ve Setter — Ama Her Field'a Değil

Klasik encapsulation getter/setter yazmayı önerir:
```java
private String name;
public String getName() { return name; }
public void setName(String name) { this.name = name; }
```

Ama **gerçekten bu kadar basit mi?** Körlemesine getter/setter yazmak anti-pattern olabilir:

**a) Anlamsız gözlemci getter'lar**: Bazı field'lar dışarıya açılmak zorunda değil. Sadece sınıf içinde kullanılıyorsa getter ekleme.

**b) Mutator (setter) tehlikeli**: Setter yazmak field'ın **değişebileceğini** duyurur. İhtiyaç yoksa yazma — immutable tutmaya çalış.

**c) Validation olmadan setter = public field'dan farksız**:
```java
public void setAge(int age) { this.age = age; }   // any değer alır — anlamsız
```
Setter'ın amacı kontrol koymak. Aksi halde public field olsa olurdu.

**d) Daha iyi yaklaşım**: **anlamsal metodlar**.
```java
// Kötü
account.setBalance(account.getBalance() + 100);

// İyi
account.deposit(100);
```
İkincisi **ne yaptığını** söyler, sadece veri nakletmez.

Modern pratik: mümkün olduğunca immutable sınıflar yaz, setter yerine "with" metodları kullan:
```java
public class User {
    private final String name;
    private final int age;
    
    public User withAge(int newAge) {
        return new User(this.name, newAge);
    }
}
```

---

## 4) `final` — Çok Anlamlı Bir Kelime

`final` üç yerde, üç farklı anlamda kullanılır:

### a) `final` Değişken — Bir Kere Atanır
```java
final int MAX = 100;
MAX = 200;             // HATA

final String name;     // blank final — sonradan bir kere atanabilir
name = "Ali";          // OK
name = "Veli";         // HATA
```
Sabitler için klasik kullanım: `public static final double PI = 3.14;`.

### b) `final` Parametre — Metot İçinde Değişmez
```java
public void process(final int count) {
    count = count + 1;   // HATA
}
```
Çok sık yapılmaz ama defensive programming ve lambda capture için bazen kullanılır. Java 8+'da "effectively final" kuralı bunun yerini büyük oranda aldı.

### c) `final` Method — Override Edilemez
```java
public class Parent {
    public final void critical() { ... }
}
public class Child extends Parent {
    public void critical() { ... }   // HATA
}
```
Davranışın değişmesini istemediğin metodlarda kullan.

### d) `final` Class — Extend Edilemez
```java
public final class String { ... }
```
Alt sınıf türetilmez. `String`, `Integer`, `LocalDate` gibi kritik sınıflar final'dır.

### Önemli Tuzak: Final Referans ≠ Immutable Nesne
```java
final List<Integer> list = new ArrayList<>();
list.add(1);               // OK — listeyi değiştiriyoruz, referansı değil
list.add(2);               // OK
list = new ArrayList<>();  // HATA — referansı değiştiremeyiz
```
`final` referansın **hangi nesneyi gösterdiğini** sabitler. Nesnenin içeriği değişebilir. Gerçek immutability için içeriği de korumak gerekir.

---

## 5) Immutability — Yaratıldıktan Sonra Değişmez Nesneler

**Immutable** nesne, yaratıldıktan sonra **hiçbir field'ı değişmeyen** nesnedir. Java'nın en güvenli veri yapılarıdır.

### Immutable Sınıf Nasıl Yazılır? 5 Kural

**1. Sınıfı `final` yap.** Kimse extend edip davranışı bozmasın.

**2. Tüm field'ları `private final` yap.** Dışarıdan erişim yok, bir kere atanır.

**3. Setter YAZMA.** Mutator yok.

**4. Mutable field'ları defansif kopyala.**
```java
public final class Person {
    private final String name;
    private final List<String> hobbies;
    
    public Person(String name, List<String> hobbies) {
        this.name = name;
        this.hobbies = List.copyOf(hobbies);   // SAVUNMACI KOPYA
    }
    
    public List<String> getHobbies() { 
        return hobbies;   // zaten unmodifiable
    }
}
```
Eğer dışarıdaki `hobbies` listesi sonradan değiştirilirse, iç kopyamız etkilenmez. Get ederken de unmodifiable liste döndük — çağıranın `list.add()` yapması mümkün değil.

**5. Eğer "değiştirme" ihtiyacı varsa yeni nesne döndür** ("with" pattern):
```java
public Person withAge(int newAge) {
    return new Person(this.name, this.hobbies, newAge);
}
```
Eski nesne değişmez, yeni nesne üretilir.

### Immutability'nin Faydaları (Bedavaya Gelen)

**a) Thread-safety otomatik gelir.**
Değişmeyen bir nesneyi kaç thread paylaşırsa paylaşsın, hiçbir senkronizasyona ihtiyaç yok. Race condition yok, deadlock yok.

**b) HashMap key'i olarak güvenli.**
Hash değeri değişmeyen bir nesneyi map'e koyduğunda sonsuza dek bulursun. `String` key'lerinin bu kadar yaygın olmasının sebebi budur.

**c) Cache/paylaşım rahat.**
`String` pool, `Integer` cache hep immutability sayesinde çalışır. Aynı nesneyi yüzlerce yerde paylaşabilirsin.

**d) Yan etkisi yok, test kolay.**
Bir metoda verdiğin immutable nesne metodun içinde değiştirilemez. "Bu metod aldığım nesneyi değiştirir mi?" diye endişelenmezsin.

**e) Akılcı kod.**
State değişimi daha takip edilebilir olur. Debug kolaydır.

### Java'nın Yerleşik Immutable Sınıfları
- `String`
- Wrapper'lar (`Integer`, `Long`, `Double`, ...)
- `LocalDate`, `LocalTime`, `LocalDateTime`, `Instant` (Java 8 time API)
- `UUID`
- `BigInteger`, `BigDecimal`
- `List.of(...)`, `Set.of(...)`, `Map.of(...)` ile üretilenler (Java 9+)
- `record` ile tanımlananlar (Java 16+)

---

## 6) `record` ile Hızlı Immutability

Java 16 ile gelen `record` immutable sınıfları tek satırda yazmanı sağlar:

```java
public record Point(int x, int y) {}
```

Otomatik üretir: `private final` field'lar, canonical constructor, getter'lar (`x()`, `y()`), `equals`, `hashCode`, `toString`. Daha önce 30 satırda yazdığın şeyi tek satırda.

Validasyon için compact constructor:
```java
public record Person(String name, int age) {
    public Person {
        if (age < 0) throw new IllegalArgumentException();
        if (name == null || name.isBlank()) throw new IllegalArgumentException();
    }
}
```

Record'lar DTO, API response model, value object için idealdir.

---

## 7) POJO ve JavaBean

**POJO** (Plain Old Java Object) — basit, kütüphane bağımsız Java nesnesi. Kısıt: hiçbir özel framework interface/class'ından türemek zorunda değil.

**JavaBean** — belirli kurallara uyan POJO:
- `public` class
- `public` no-arg constructor
- private field'lar
- public getter/setter (`getX`, `setX`, `isX` boolean için)
- Genelde `Serializable`

JavaBean konvansiyonları eski framework'ler (JSP, JSF, eski Spring) için zorunluydu. Modern Java'da kısmen gereksiz, ama hâlâ uyumluluk için yaygın.

Modern alternatif: `record` veya Lombok (`@Data`, `@Value`).

---

## 8) Private Constructor — Özel Desenler

Bazen sınıfın dışarıdan `new` ile yaratılmasını **yasaklamak** istersin.

**a) Utility class** — sadece static metodlar:
```java
public final class MathUtils {
    private MathUtils() { throw new AssertionError(); }   // asla instance üretilmez
    public static int add(int a, int b) { return a + b; }
}
```

**b) Singleton** — tek nesne:
```java
public class Database {
    private static final Database INSTANCE = new Database();
    private Database() {}
    public static Database getInstance() { return INSTANCE; }
}
```
(Modern Java'da enum ile singleton daha güvenli: `enum Database { INSTANCE; ... }`).

**c) Factory method** — constructor gizli, üretim static metod üzerinden:
```java
public class User {
    private final String name;
    private User(String name) { this.name = name; }
    
    public static User of(String name) { return new User(name); }
    public static User anonymous() { return new User("anonymous"); }
}
```

---

## 9) Yaygın Hatalar ve İpuçları

**a) Getter/setter fetişizmi**: Her field için körlemesine getter/setter yazma. İhtiyaç yoksa açma.

**b) Public field**: Sabitler (`public static final`) hariç, **asla public field kullanma**. Encapsulation'ın temelini yıkar.

**c) Leaky abstraction**: Getter'ın içindeki koleksiyonu doğrudan döndürmek kapsüllemeyi kırar.
```java
// Kötü
public List<Item> getItems() { return items; }   // dışarıdan list.add() yapabilirler

// İyi
public List<Item> getItems() { return List.copyOf(items); }
```

**d) Final ile ihtiyaç yok sanmak**: Immutable nesnelerde bile thread güvenliği için `final` şart — compiler ve JVM bu sayede doğru optimizasyonları yapar.

---

## Mülakat Soruları ve Örnek Cevaplar

**1. Encapsulation nedir, neden önemlidir?**
Veriyi sınıf içinde saklayıp sadece kontrollü yollarla (public metodlar) erişime izin vermektir. Faydaları: veri bütünlüğü (validasyon), implementation değişikliğinde API sabit kalır, güvenlik kontrolleri merkezileşir, logging/cache eklemek kolay, kod daha okunabilir.

**2. Java'daki 4 erişim seviyesini sırala.**
`private` (sadece aynı sınıf), *default* (aynı paket), `protected` (aynı paket + farklı paketteki alt sınıflar), `public` (her yer). Top-level class sadece public veya default olabilir.

**3. `protected` tam olarak ne verir?**
Aynı paketteki her sınıf + farklı paketteki alt sınıflar. Farklı paketteki alt sınıf, **sadece kendi (veya alt) tipi üzerinden** protected üyeye erişebilir — farklı nesnelerin protected'ına değil.

**4. `final` kaç anlamda kullanılır?**
Dört: `final` değişken (bir kere atanır), `final` parametre (metot içinde değişmez), `final` metod (override edilemez), `final` sınıf (extend edilemez). Ek olarak "effectively final" kuralı lambda'larda vardır.

**5. `final` bir referans immutable midir?**
Hayır! `final` referansın hangi nesneyi gösterdiğini sabitler, nesnenin içeriği değişebilir. `final List<Integer> list = new ArrayList<>(); list.add(1);` geçerlidir.

**6. Immutable sınıf nasıl yazılır?**
Class final, tüm fieldlar private final, setter yok, mutable field'lar defansif kopyalanır (constructor'da `List.copyOf(...)` gibi), getter'lar da mutable olanları unmodifiable döner, değişiklik "with" pattern ile yeni nesne üretir.

**7. String'in immutable olmasının faydaları?**
Thread-safe, HashMap anahtarı olarak güvenli (hashCode değişmez), String pool paylaşımı mümkün, güvenlik (dosya yolu/SQL parametresi sonradan değişemez), cache'lenebilir hashCode.

**8. POJO ile JavaBean arasındaki fark?**
POJO basit Java nesnesi — özel kısıt yok. JavaBean belirli konvansiyonlara uyar: no-arg constructor, private field + public getter/setter, Serializable. Her JavaBean POJO'dur, her POJO JavaBean değildir.

**9. `record` ne zaman kullanmalıyım?**
Saf veri taşıyan, immutable, küçük sınıflar için. DTO, API response, value object, koordinat/tuple gibi. Davranış ağırlıklı domain nesneleri için normal class daha uygun.

**10. Setter yazmak kapsüllemeyi bozar mı?**
Değer kontrolü yoksa bozar — public field'dan farkı kalmaz. Doğru setter validasyon içermelidir. Daha iyi yaklaşım mümkünse setter yerine **anlamsal metod** (`deposit` gibi) yazmak veya immutable tutmaktır.

**11. Utility class nasıl yazılır?**
`final` class, `private` constructor (nesne üretilemez), tüm üyeler `static`. Örnek: `java.lang.Math`, `java.util.Collections`, `java.util.Objects`.
