# 12 — Generics

Generics, Java 5 ile gelen ve dili köklü şekilde değiştiren özellik. Öncesinde `List list = new ArrayList(); list.add("str"); Integer i = (Integer) list.get(0);` gibi cast cehennemiyle yaşardık. Generics bu sorunu **derleme zamanında tip güvenliği** getirerek çözdü. Bu paketi bitirdiğinde generic sınıf/metod yazabilecek, wildcard'ları doğru kullanabilecek ve "type erasure neden önemli?" sorusunu rahatça cevaplayabileceksin.

---

## 1) Neden Generics? Önce/Sonra

### Öncesi (Java 1.4)
```java
List list = new ArrayList();
list.add("merhaba");
list.add(42);                    // List her şeyi kabul ediyor
String s = (String) list.get(0); // cast ZORUNLU
Integer n = (Integer) list.get(1);
```
Problemler:
- Her erişimde **cast** yazmak zorundasın.
- Cast yanlışsa `ClassCastException` — runtime'da patlar, derlemede değil.
- Listede farklı tipler karışabilir, tip güvenliği sıfır.

### Sonrası (Java 5+)
```java
List<String> list = new ArrayList<>();
list.add("merhaba");
// list.add(42);                   // DERLEME HATASI — artık tip kontrolü var
String s = list.get(0);            // cast gerek yok
```
Faydalar:
- **Compile-time type safety** — yanlış tipi eklemeye çalışırsan IDE/derleyici engeller.
- **Cast'siz kod** — okunur, güvenli.
- **API dokümantasyonu** — `List<User>` gördüğünde ne taşıdığı belli.

---

## 2) Sözdizimi Temelleri

### Generic Kullanma
```java
List<String> strings = new ArrayList<>();
Map<String, Integer> ages = new HashMap<>();
```
`<>` içine tip parametresi yazarsın. `ArrayList<>` boş diamond — Java 7+ sağ tarafta tekrar yazmana gerek yok.

### Generic Sınıf Tanımlama
```java
public class Box<T> {              // T bir tip parametresi
    private T value;
    public void set(T v) { this.value = v; }
    public T get() { return value; }
}

Box<String> sb = new Box<>();
sb.set("hi");
String x = sb.get();
```
`T` parametresi sınıf adından hemen sonra `<>` içinde tanımlanır ve sınıf içinde gerçek tipmiş gibi kullanılır.

### Birden Fazla Tip Parametresi
```java
public class Pair<K, V> {
    K key; V value;
    Pair(K k, V v) { key = k; value = v; }
}
Pair<String, Integer> p = new Pair<>("yaş", 30);
```

### Generic Metot Tanımlama
```java
public static <T> T firstOrNull(List<T> list) {
    return list.isEmpty() ? null : list.get(0);
}

String s = firstOrNull(stringList);
Integer i = firstOrNull(intList);
```
Return tipinden önce `<T>` — bu "bu metot generic, T parametresi var" bildirimidir.

### Konvansiyonlar (Tek Harfli İsimler)
- `T` — Type (genel)
- `E` — Element (koleksiyonlar)
- `K`, `V` — Key, Value (maps)
- `R` — Return
- `N` — Number
- `S`, `U`, `V` — ikinci, üçüncü parametreler

Tek harfli ama anlamlıdır. Daha açıklayıcı istiyorsan `TPayload`, `TKey` gibi Pascal case.

---

## 3) Bounded Type Parameters — Sınırlanmış Tipler

Bazen "T herhangi bir şey olabilir" yerine "T, Number veya alt tipi olmalı" demek istersin:

### Upper Bound — `extends`
```java
public static <T extends Number> double sum(List<T> xs) {
    double total = 0;
    for (T x : xs) total += x.doubleValue();   // T, Number metodlarına sahip
    return total;
}
```
`T extends Number` = T, Number ya da alt tipi (Integer, Double, Long...). Sınıflandır, tip bu sınırın altında olmalı.

### Birden Fazla Bound
```java
public static <T extends Number & Comparable<T>> T max(List<T> list) {
    T best = list.get(0);
    for (T x : list) if (x.compareTo(best) > 0) best = x;
    return best;
}
```
`&` ile birden fazla kısıt. Sınıf varsa tek olmalı ve başta gelmeli, sonra interface'ler.

> **Not**: Generics'te `extends` hem sınıf hem interface için kullanılır; `implements` burada yok.

---

## 4) Wildcards (`?`) — Bilinmeyen Tip

Bir metoda tipi "önemli değil" geçirmek istersen wildcard kullanırsın. Üç form var:

### Unbounded — `<?>`
```java
public void printAll(List<?> list) {
    for (Object o : list) System.out.println(o);
}
```
Herhangi bir `List` kabul eder: `List<String>`, `List<Integer>`, `List<Object>`. İçeriğini sadece `Object` olarak okursun, **yazamazsın** (null hariç) çünkü tipini bilmiyorsun.

### Upper Bound — `<? extends T>`
```java
public double sum(List<? extends Number> list) {
    double total = 0;
    for (Number n : list) total += n.doubleValue();
    return total;
}
```
`List<Integer>`, `List<Double>`, `List<Number>` kabul eder. Okumak güvenli (her eleman Number), **yazmak yasak** (hangi alt tip bilmiyorsun — Integer listesine Double yazamazsın).

### Lower Bound — `<? super T>`
```java
public void fillWithIntegers(List<? super Integer> list) {
    list.add(1); list.add(2); list.add(3);
}
```
`List<Integer>`, `List<Number>`, `List<Object>` kabul eder. **Yazmak güvenli** (hepsine Integer konulabilir), okuma sadece Object olarak.

---

## 5) PECS Kuralı — Producer Extends, Consumer Super

Hatırlanması zor olan wildcard kuralının akronimi: **PECS**.

- **P**roducer **E**xtends — Eğer bir liste sana eleman **sağlıyorsa** (okuyorsan), `<? extends T>` kullan.
- **C**onsumer **S**uper — Eğer bir listeye eleman **koyuyorsan** (yazıyorsan), `<? super T>` kullan.

### Tam Örnek — `Collections.copy`
```java
public static <T> void copy(List<? super T> dest, List<? extends T> src) {
    for (T t : src) dest.add(t);       // src'den oku, dest'e yaz
}
```
`src` bize T veriyor → `extends` (producer).
`dest`'e T yazıyoruz → `super` (consumer).

Bu imzayla `copy(list<Object>, list<String>)` legal. Genel amaçlı bir copy fonksiyonu yazmak ancak bu şekilde mümkün.

---

## 6) Type Erasure — Generics'in Sırrı ve Sınırlaması

Java generics **derleme zamanı** kontrolüdür. Derleme bittikten sonra bytecode'da tip parametresi **silinir**:
- `List<String>` → `List` (raw)
- `Box<T>` → `Box` (T yerine Object)
- `<T extends Number>` → T yerine Number

Derleyici erasure yaparken gerekli cast'leri otomatik ekler:
```java
List<String> list = new ArrayList<>();
list.add("x");
String s = list.get(0);
```
Bytecode seviyesinde aslında:
```java
List list = new ArrayList();
list.add("x");
String s = (String) list.get(0);   // cast otomatik eklendi
```

### Erasure'ın Yarattığı Kısıtlar

**a) Runtime'da tip parametresi yok**:
```java
if (list instanceof List<String>) { ... }   // HATA
if (list instanceof List<?>) { ... }         // OK (wildcard)
```
Çünkü runtime'da `List<String>` ve `List<Integer>` aynı tiptir (`List`).

**b) Generic tip ile instanceof yasak**:
```java
class Box<T> {
    void check(Object o) {
        if (o instanceof T) { ... }   // HATA
    }
}
```

**c) `new T()` yasak** — JVM T'yi bilmediği için nesne yaratamaz. Çözüm: `Supplier<T>` parametre olarak al.
```java
public <T> T create(Supplier<T> factory) {
    return factory.get();
}
// Kullanım: create(ArrayList::new);
```

**d) Generic array yaratmak yasak**:
```java
T[] arr = new T[10];                  // HATA
List<String>[] listArr = new List<String>[10];   // HATA
```
Çözüm: `@SuppressWarnings("unchecked") T[] arr = (T[]) new Object[10];` veya `List<T>` kullan.

**e) Overload belirsizliği**:
```java
void foo(List<String> list) { ... }
void foo(List<Integer> list) { ... }   // HATA — aynı erasure imzası
```
İki metot bytecode'da aynı `foo(List)` olur.

**f) Static context'te type parameter yasak**:
```java
class Box<T> {
    static T value;            // HATA — static sınıfın T'sini kullanamaz
    static void foo(T x) {}    // HATA
}
```

### Erasure'ın Yan Etkisi: `List<String>` ve `List<Integer>` Aynı Sınıf
```java
List<String> a = new ArrayList<>();
List<Integer> b = new ArrayList<>();
System.out.println(a.getClass() == b.getClass());   // true
```

---

## 7) Invariance — Generic'ler "Kovaryant" Değil

Normal referans tiplerinde: `Object o = "string";` legal (String, Object'in alt tipi).

Generic'lerde: `List<Object> list = new ArrayList<String>();` **ILLEGAL**. Neden?

Varsayalım olsaydı:
```java
List<String> strings = new ArrayList<>();
List<Object> objects = strings;    // varsayımsal
objects.add(42);                   // Object listesine Integer ekle — legal
String s = strings.get(0);         // ClassCastException!
```
Tip güvenliği çöker. Bu yüzden Java generics **invariant**'tır — `List<String>`, `List<Object>` değildir.

**Array'ler ise covariant'tır** (Java 1.0 tasarım hatası):
```java
Object[] arr = new String[3];
arr[0] = 42;                       // ArrayStoreException (runtime!)
```
Generic'ler derleme zamanında yasaklar, array'ler runtime'a bırakır. Bu yüzden generic tercih et.

---

## 8) Diamond Operator ve Type Inference

Java 7 ile diamond operator (`<>`) geldi:
```java
Map<String, List<Integer>> map = new HashMap<>();   // sağ <> boş OK
```
Derleyici sol taraftan tip çıkarır.

Java 10 ile `var`:
```java
var list = new ArrayList<String>();   // list: ArrayList<String>
```

Java 8'de **target typing**:
```java
List<String> empty = Collections.emptyList();   // <String> çıkarıldı
```

---

## 9) Varargs + Generics — `@SafeVarargs`

Generic varargs **heap pollution** uyarısı verir:
```java
public <T> List<T> of(T... items) {    // uyarı
    return Arrays.asList(items);
}
```

Üretim için güvenliyse `@SafeVarargs` ile susturabilirsin:
```java
@SafeVarargs
public static <T> List<T> of(T... items) {
    return Arrays.asList(items);
}
```
**Yalnızca static veya final metotlara koy** — alt sınıf override ederse güvenlik garantisi düşer.

---

## 10) Generic Singleton Factory

```java
@SuppressWarnings("rawtypes")
private static final Comparator NATURAL = Comparator.naturalOrder();

@SuppressWarnings("unchecked")
public static <T extends Comparable<T>> Comparator<T> naturalOrder() {
    return (Comparator<T>) NATURAL;
}
```

Tek bir singleton nesne tüm generic kullanımlar için yeniden kullanılır. Erasure'ın pozitif yanı.

---

## 11) Recursive Type Bounds

Kendini referans eden tip kısıtı:
```java
public static <T extends Comparable<T>> T max(List<T> list) { ... }
```
"T, kendisiyle karşılaştırılabilir olmalı." Comparable klasik örnektir.

---

## 12) Wildcard Capture

Derleyici bazen wildcard'ı içe bir generic method ile yakalar:
```java
public static void swap(List<?> list, int i, int j) {
    swapHelper(list, i, j);
}

private static <T> void swapHelper(List<T> list, int i, int j) {
    T tmp = list.get(i);
    list.set(i, list.get(j));
    list.set(j, tmp);
}
```
`<?>` direkt swap yapmaya yetmez (set için tip lazım). Private helper ile `<T>` yakalanır. Bu yaygın bir teknik.

---

## Mülakat Soruları ve Örnek Cevaplar

**1. Generics'in amacı nedir?**
Compile-time tip güvenliği sağlamak ve cast zorunluluğundan kurtulmak. Yanlış tiplerin listeye eklenmesi gibi hataları runtime yerine derleme zamanında yakalar. Ayrıca API'larda hangi tipin geçeceğini dokümante eder.

**2. Type erasure nedir?**
Derleyicinin generic tip bilgisini bytecode'a çevirirken silmesi. `List<String>` bytecode'da `List` olur, `T` yerine `Object` (veya bound varsa o sınıf) konur. Runtime'da tip parametresi bilinmez. Backward compatibility için seçildi.

**3. `List<Object>` ile `List<?>` farkı?**
`List<Object>` "Object veya alt tipi listesi" — ama `List<String>` **bu tipte değil** (invariance). `List<?>` "bilinmeyen parametreli list" — her tipte list'i kabul eder ama yazamazsın (null hariç).

**4. PECS nedir?**
Producer Extends, Consumer Super. Liste sana eleman **sağlıyorsa** (iterate ediyorsan) `<? extends T>`. Liste elemanı **kabul ediyorsa** (add ediyorsan) `<? super T>`. `Collections.copy(dst, src)` klasik örnek.

**5. Generic sınıfta neden `new T()` yazamam?**
Type erasure yüzünden runtime'da T'nin ne olduğu bilinmez. JVM hangi sınıfın constructor'ını çağıracağını bilemez. Çözüm: `Class<T>` veya `Supplier<T>` parametre olarak al.

**6. `List<String>` `List<Object>`'e atanabilir mi?**
Hayır. Generics invariant'tır — `List<String>` ile `List<Object>` arasında alt/üst tip ilişkisi yoktur. Atanabilseydi tip güvenliği çökerdi.

**7. Array ile generic'in farkı neden önemli?**
Array'ler covariant (`String[]` → `Object[]` legal) ama runtime'da kontrol eder — yanlış tip koyarsan `ArrayStoreException`. Generic'ler invariant + compile-time kontrol yapar. Generic daha güvenli çünkü hataları derlemede yakalar.

**8. Bounded type parameter ne için?**
`<T extends Number>` gibi — T'nin sahip olması gereken özellikleri kısıtlar. Generic metot içinde T'nin Number metodlarına (`doubleValue()` gibi) erişebilmeni sağlar. Aksi halde T'yi sadece Object metodları ile kullanabilirdin.

**9. Generic metot ile generic sınıf farkı?**
Generic sınıf: `class Box<T>` — T sınıf genelinde. Generic metot: `<T> T first(List<T>)` — T sadece o metoda ait, sınıf generic olmasa da metot generic olabilir.

**10. `@SafeVarargs` ne yapar?**
Generic varargs (`T... items`) heap pollution uyarısı verir. Eğer metodun gerçekten güvenli olduğunu biliyorsan bu anotasyonla uyarıyı susturursun. Static veya final metotlarda kullan.

**11. Wildcard capture nedir?**
`<?>` kullanan bir metot, yardımcı generic metod tarafından T yakalanır. Örneğin `swap(List<?>)` doğrudan implement edilemez, private `<T> swapHelper(List<T>)` ile yakalanır. Derleyici otomatik halleder.

**12. `List<? extends Number>` üzerinden add edebilir miyim?**
Hayır (null hariç). Bilinmeyen alt tipte add yapmak tip güvenliğini bozar. `List<Integer>` gönderilse, sen Double ekleyemezdin. Sadece okuma güvenli (her eleman Number olarak erişilir).
