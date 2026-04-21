# 17 — Reflection (Yansıma)

Java **Reflection API**, çalışma zamanında (runtime) sınıfların **metadatasını** (class, field, method, constructor, annotation, modifier, generics) **okumayı** ve **üzerinde iş yapmayı** (nesne yaratma, metod çağırma, field okuma/yazma) sağlar. Kısaca: derleme zamanında bilmediğin bir tipi *runtime*'da kontrol edip kullanabilirsin.

Kulağa sihirli geliyor — sihirli değildir. Framework'lerin (Spring, Hibernate, JUnit, Jackson, Gson, Mockito, Lombok'un *bazı* kısımları) arkasındaki temel mekanik budur. Ama **güçlü silah güçlü dezavantajla gelir**: tip güvenliğini kaybedersin, performans düşer, `SecurityManager`/`module` kuralları seni sınırlayabilir ve kod okunaksız hale gelir. Bu yüzden reflection "son çare" aletidir: önce annotation processing / interface / factory düşün, çözemediğinde reflection'a in.

---

## 1) Neden Reflection?

Somut kullanım alanları:
- **IoC / DI konteynerleri** (Spring): `@Component` işaretli sınıfları tarayıp otomatik örneklemek, `@Autowired` field'ları enjekte etmek.
- **ORM** (Hibernate, JPA): `@Entity`, `@Column` okuyup SQL üretmek ve `ResultSet` → nesne dönüşümü yapmak.
- **Serializer/Deserializer** (Jackson, Gson): JSON alanlarını Java field'larına map'lemek.
- **Test framework'leri** (JUnit): `@Test` metotlarını bulup çağırmak; `private` metot testi.
- **Plugin / modüler sistemler**: `Class.forName("com.acme.Plugin")` ile dinamik yükleme.
- **Mock/Proxy kütüphaneleri** (Mockito, JDK `Proxy`, CGLIB): interface/sınıf için çalışma-zamanı proxy üretmek.
- **IDE / debug araçları**, `toString()` otomatikleri, generic alan tarama.

Reflection'a gerek **olmayan** durumlar:
- Derleme zamanında tip biliniyorsa normal kodu yaz.
- Annotation *compile-time*'da işlenecekse **Annotation Processing** (APT, Lombok, Dagger) daha performanslı.
- "Sadece alan kopyalayacağım" için `MapStruct` gibi kod üreticiler daha temiz.

---

## 2) `Class<?>` Nesnesi — Her Şeyin Başı

JVM her yüklenen sınıf için tek bir `Class<T>` nesnesi tutar. Bu nesne sınıfın **aynasıdır**.

Elde etme yolları:
```java
Class<String> c1 = String.class;                  // class literal (compile-time)
Class<?>      c2 = "abc".getClass();              // instance üzerinden
Class<?>      c3 = Class.forName("java.util.ArrayList"); // FQN string (ClassLoader kullanır)
```

Farkları:
- `String.class` → en hızlı, compile-time çözülür, `ClassNotFoundException` atmaz.
- `getClass()` → instance'ın **runtime** tipini verir (polymorphism'de gerçek tip).
- `Class.forName(...)` → çalışma zamanında string ile yükler, `ClassNotFoundException` atar; genelde default olarak **caller'ın ClassLoader'ı**yla çalışır.

Neler sorulabilir bir `Class`'a:
- `getName()` / `getSimpleName()` / `getCanonicalName()` / `getPackageName()`
- `getSuperclass()`, `getInterfaces()`
- `getModifiers()` → `Modifier.isPublic(...)`, `isAbstract(...)`, `isFinal(...)`
- `isInterface()`, `isEnum()`, `isRecord()`, `isArray()`, `isAnnotation()`, `isPrimitive()`
- `getDeclaredFields()` vs `getFields()` (aşağıda)

---

## 3) `getXxx` vs `getDeclaredXxx` — En Çok Karıştırılan

| Metot | Neyi Döner | Erişim |
|---|---|---|
| `getFields()` / `getMethods()` / `getConstructors()` | Yalnız **`public`** üyeler, **miras alınanlar dâhil** | Public her şeye |
| `getDeclaredFields()` / `getDeclaredMethods()` / `getDeclaredConstructors()` | **Bu sınıfta bildirilen** her şey (private/protected/package dâhil), **miras yok** | Hepsine ama `setAccessible(true)` lazım non-public için |

Özet kural:
- **"Bu sınıfın kendi" üyelerini mi istiyorum?** → `getDeclared*`
- **"Erişebildiğim public sözleşme" mi?** → `get*` (parent'ları da dolaşır)

Miras alınan private field'lara erişmek için superclass zincirini elle dolaşman gerekir:
```java
for (Class<?> k = obj.getClass(); k != null; k = k.getSuperclass()) {
    for (Field f : k.getDeclaredFields()) { ... }
}
```

---

## 4) Nesne Üretme (Dinamik Instantiation)

Eskiden: `clazz.newInstance()` → **deprecated (Java 9+)** çünkü checked exception'ları "yutar".

Doğru yol:
```java
Constructor<?> ctor = clazz.getDeclaredConstructor(String.class, int.class);
ctor.setAccessible(true); // private ctor'u çağırmak için
Object instance = ctor.newInstance("Ali", 30);
```

Constructor aramak istiyorsan parametre tipleri **tam eşleşmeli** (`int.class` ≠ `Integer.class`).

---

## 5) Metod Çağırma

```java
Method m = clazz.getDeclaredMethod("setName", String.class);
m.setAccessible(true);
Object result = m.invoke(instance, "Veli"); // static ise instance = null
```

Dikkat:
- `invoke` fırlatır: `IllegalAccessException`, `InvocationTargetException`, `IllegalArgumentException`.
- **`InvocationTargetException`**: *çağrılan metodun içinde* atılan exception'ı sarmalar. Gerçek sebebe `getCause()` ile ulaşırsın. Bu ayrım kritiktir: reflection'ın kendi hatası değil, hedef metodun hatası.
- Primitive'ler için wrapper otomatik boxing olur (`invoke` parametreleri `Object...`).
- Varargs metotları çağırırken array'i `(Object) new String[]{...}` şeklinde **cast etmek** gerekebilir, yoksa "spread" edilir.

---

## 6) Field Okuma / Yazma

```java
Field f = clazz.getDeclaredField("age");
f.setAccessible(true);
int age = (int) f.get(instance);   // okuma (static ise instance = null)
f.set(instance, 42);               // yazma
```

`final` field'a yazmak:
- Java 12 öncesi: `Field#setAccessible(true)` + modifiers hack'i ile mümkündü.
- Java 17+ ve güçlü encapsulation ile: çoğu JDK içi final field'a yazma **artık yasak** (`InaccessibleObjectException`).
- Kendi `final` field'larına `setAccessible(true)` ile yazabilirsin ama JIT bazı değerleri sabit kabul edip **inline** etmiş olabilir → eski değer görünür. **Yapma.**

---

## 7) Annotation'ları Okuma

Annotation runtime'da görünür olsun diye tanımına `@Retention(RetentionPolicy.RUNTIME)` gerekir.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface MyTest {}

for (Method m : clazz.getDeclaredMethods()) {
    if (m.isAnnotationPresent(MyTest.class)) {
        m.invoke(instance);
    }
}
```

`getAnnotation(X.class)` → tek annotation instance'ı; alanlarını okuyabilirsin (örn. `@Column(name="username")` → `col.name()`).

---

## 8) Generics — Tip Silinmişse Nasıl Okunur?

Normal type erasure sebebiyle `List<String>` runtime'da `List` gibi görünür. **Ama** field/metot/sınıf *deklarasyonundaki* generic bilgisi bytecode'da `Signature` olarak saklanır ve reflection ile okunabilir:

```java
Field f = clazz.getDeclaredField("names"); // List<String> names
ParameterizedType pt = (ParameterizedType) f.getGenericType();
Type arg = pt.getActualTypeArguments()[0]; // class java.lang.String
```

`Type`, `ParameterizedType`, `WildcardType`, `TypeVariable`, `GenericArrayType` — generic keşfi için hiyerarşi.

Jackson'ın `TypeReference`, Guava'nın `TypeToken` bu "generic'i anonim sınıfla yakalama" hilesini kullanır.

---

## 9) `setAccessible(true)` ve Modül Sistemi (JPMS)

Java 9 ile modül sistemi gelince `setAccessible` her kilidi açmaz.
- Paket `exports`/`opens` edilmemişse reflection → **`InaccessibleObjectException`**.
- "Deep reflection" için modül `opens <pkg> to <module>` demeli ya da `--add-opens` VM argümanı verilmelidir:
  ```
  --add-opens java.base/java.lang=ALL-UNNAMED
  ```
- Classpath üzerindeki (unnamed module) kod genelde az sorun yaşar, ancak JDK iç paketlerinde (örn. `java.lang`) engel aynen geçerlidir.

Bu yüzden modern kodda **kendi paketlerinde** reflection çalışır ama JDK'nın içine girmek isteyen "legacy hack"ler çöker.

---

## 10) Dinamik Proxy — Reflection'ın Asıl Gücü

`java.lang.reflect.Proxy` ile **interface**'e runtime'da implementasyon üretirsin. Spring AOP, Mockito, dinamik logging, transaction sarmalama — hepsi bu.

```java
MyService real = new MyServiceImpl();
MyService proxy = (MyService) Proxy.newProxyInstance(
    MyService.class.getClassLoader(),
    new Class<?>[]{ MyService.class },
    (prx, method, args) -> {
        System.out.println("BEFORE " + method.getName());
        Object ret = method.invoke(real, args); // gerçeğe delege
        System.out.println("AFTER  " + method.getName());
        return ret;
    }
);
proxy.doWork();
```

Sınırı: **sadece interface**. Sınıf için bytecode üretmek isteyince **CGLIB** / **ByteBuddy** devreye girer.

---

## 11) `MethodHandle` & `VarHandle` — Modern, Hızlı Alternatif

Java 7 ile `java.lang.invoke.MethodHandle`, Java 9 ile `VarHandle` geldi:
- Reflection'dan **daha hızlı** (JIT tarafından iyi optimize edilir; çağrı neredeyse native).
- Tip güvenliği biraz daha iyi (`MethodType` ile).
- Dezavantajı: API daha düşük seviye, öğrenme eğrisi var.

Kural: "hot path"te çok sık çağıracaksan `MethodHandle` tercih et; ara sıra meta işlem için Reflection yeterli.

---

## 12) Performans

Reflection yavaştır çünkü:
- Her çağrıda erişim kontrolü, tip kontrolü, boxing/unboxing.
- JIT normal `invokevirtual` kadar agresif inline edemez.
- Exception sarmalama maliyeti.

İyileştirme ipuçları:
- `Method`/`Field` nesnelerini **cache'le** (her seferinde `getDeclaredMethod` çağırma).
- `setAccessible(true)`'yi bir kere yap, tut.
- Gerçekten hot path ise `MethodHandle`'a veya annotation processor ile üretilmiş koda geç.

---

## 13) Güvenlik ve Tehlikeler

- `private` alanlara erişim → invariantları kırabilir (immutable sınıf artık immutable değil!).
- `SecurityManager` (Java 17'de deprecate edilmiş olsa da) `suppressAccessChecks` iznini kısıtlayabilir.
- Serialization saldırı yüzeyi (Jackson gadget chain'leri) büyük oranda reflection + `setAccessible` üzerine kuruludur.
- Kodun refactor'a dayanıklılığı azalır: metot adını string'le arıyorsan IDE rename fark edemez.

---

## 14) Mülakat Soruları

1. `getFields()` ile `getDeclaredFields()` arasındaki fark nedir?
2. `Class.forName("X")` ile `X.class` arasındaki semantik fark nedir?
3. Reflection ile `private` metoda erişmek için ne yapılır, hangi exception'lar fırlayabilir?
4. `InvocationTargetException` ne zaman atılır? `getCause()` neden önemlidir?
5. Java 9 sonrasında `setAccessible(true)` neden her zaman yeterli değildir?
6. `final` bir field'a reflection ile yazmak doğru bir fikir midir? Neden?
7. `newInstance()` neden deprecate oldu? Yerine ne kullanmalı?
8. Dinamik Proxy ile CGLIB proxy'sinin farkı nedir?
9. Reflection'ın performans maliyeti nedir, nasıl azaltırsın?
10. Generic tipler type erasure ile silinirken reflection `List<String>`'in string olduğunu nasıl öğrenebiliyor?
11. Spring ve Hibernate gibi framework'ler reflection'ı nerelerde kullanır?
12. `MethodHandle`'ı reflection'a tercih ettiren avantajlar nelerdir?
13. JPMS (modül sistemi) reflection'ı nasıl etkiler? `--add-opens` ne işe yarar?

---

## Özet
Reflection = **runtime metadata + runtime eylem**. `Class<?>` merkez; `Constructor`, `Method`, `Field`, `Annotation` onun etrafında. Güçlü ama pahalı ve kırılgan; framework yazıyorsan vazgeçilmez, uygulama yazıyorsan genelde alternatif vardır. Modern Java'da ek olarak **MethodHandle** ve **modül sistemi** farkındalığı şart.
