# 02 — Operatörler ve Kontrol Akışı

Bu bölümde programın akışını yönlendiren araçları öğreneceksin: operatörler (artı, eksi, karşılaştırma, mantık), koşullar (`if`, `switch`), döngüler (`for`, `while`). Basit görünürler ama içlerinde öyle incelikler var ki mülakatlarda senelik Java yazan insanları bile tökezletir.

---

## 1) Aritmetik Operatörler — Göründüğünden Karmaşık

Temel dört işlem ve biraz fazlası:

```java
int a = 7, b = 2;
a + b  // 9
a - b  // 5
a * b  // 14
a / b  // 3  -- dikkat: int bölmesi!
a % b  // 1  -- mod (kalan)
```

En çok yanılgıya düşülen nokta: **`/` operatörü her iki operandı da tam sayı ise tam sayı bölmesi yapar ve kesirli kısmı çöpe atar**. `7 / 2` iki değil, **üç** döner. Sonucu kesirli istiyorsan en az bir tarafın kesirli olmalı:

```java
7 / 2      // 3 (int)
7 / 2.0    // 3.5 (double)
(double) 7 / 2  // 3.5
7 / (double) 2  // 3.5
```

**Mod operatörü (`%`)** bölümden kalanı verir. Negatif sayılarda ilginç davranabilir: `-7 % 3` Java'da `-1`'dir, bazı dillerdeki gibi pozitife sarmaz. Bir sayının çift olup olmadığını `n % 2 == 0` ile kontrol etmek klasik kullanımdır.

**Artırma/Azaltma (`++` ve `--`)**: Bunlar masum görünen ama tuzaklı operatörler. İki formu var:

- **Prefix (ön-ek)**: `++a` → önce artır, sonra değeri kullan.
- **Postfix (son-ek)**: `a++` → önce değeri kullan, sonra artır.

```java
int a = 5;
int b = a++;   // Adımlar: b = a olur (b=5), sonra a++ olur (a=6).  Sonuç: b=5, a=6
int c = ++a;   // Adımlar: ++a olur (a=7), sonra c = a olur.       Sonuç: c=7, a=7
```

Bu hiçbir zaman anlaşılır kod yazmana yaramaz. Tek başına bir satırda `i++;` yazmak güvenli, ama `arr[i++] = b[--j];` gibi karmaşık ifadeler içine gömersen kendini bile şaşırtırsın. **Net yazmak her zaman zekice yazmaktan iyidir.**

Klasik mülakat tuzağı:
```java
int x = 1;
x = x++ + ++x;  // Sonuç kaç?
```
Analiz: `x++` ifadesi **1** değerini verir (ve sonra x=2 olur). `++x` ifadesi önce x'i 3 yapar ve **3** değerini verir. Toplam `1 + 3 = 4`. Atama sonunda `x = 4`.

---

## 2) Karşılaştırma Operatörleri

`==`, `!=`, `<`, `>`, `<=`, `>=` operatörleri bool (doğru/yanlış) döner.

Primitive'lerde beklediğin şekilde çalışırlar: değer karşılaştırır. Ama **nesnelerde `==` referans (bellek adresi) karşılaştırır**, içerik değil! Bu, Java'nın en sık tökezletici yanı:

```java
String a = new String("java");
String b = new String("java");
System.out.println(a == b);          // false — iki ayrı nesne
System.out.println(a.equals(b));      // true  — içerik aynı
```

String literal'lerinde (`"java"`) istisna gibi görünür çünkü **String pool** yüzünden aynı referansı paylaşabilirler ama buna güvenme. Kural: **içerik karşılaştırması için daima `.equals()` kullan**.

---

## 3) Mantıksal Operatörler ve Kısa Devre (Short-Circuit)

Üç mantıksal operatör var:
- `&&` — mantıksal VE (her iki taraf da true ise true)
- `||` — mantıksal VEYA (en az biri true ise true)
- `!` — değil (true'yu false yapar)

Kritik özellik: `&&` ve `||` **kısa devre** yapar. Yani `a && b` yazdığında, `a` false çıkarsa `b` hiç değerlendirilmez (çünkü sonuç zaten false). Aynı şekilde `a || b`'de `a` true ise `b`'ye bakılmaz.

Bu iki sebepten önemlidir:

**a) NullPointerException'dan korunma**:
```java
String s = getSomething();
if (s != null && s.length() > 0) {   // Güvenli!
    ...
}
```
Eğer `s` null ise `s != null` false çıkar, `s.length()` hiç çağrılmaz. Eğer `&&` yerine `&` (bit-and) kullansaydın her iki taraf da değerlendirilirdi ve NPE alırdın.

**b) Performans**: `expensive() && cheap()` yerine `cheap() && expensive()` yazarsan, çoğu durumda pahalı çağrıdan kaçarsın.

Bit operatörleri `&` ve `|` boolean'da da çalışır, ama kısa devre **yapmazlar** (her iki tarafı da değerlendirirler). Genelde kısa devreli versiyonları (`&&`, `||`) tercih et.

---

## 4) Bit Operatörleri — Alçak Seviye Gücü

Bu operatörler sayıların ikili (binary) bit'leriyle çalışır. Günlük iş kodunda nadir, ama bayraklar (flags), performans kritik kod ve mülakatlar için bil:

- `&` (bit AND) — her iki bit de 1 ise 1.
- `|` (bit OR) — en az bir bit 1 ise 1.
- `^` (bit XOR) — bitler farklıysa 1.
- `~` (bit NOT) — tersine çevir (0 → 1, 1 → 0).
- `<<` (sola kaydırma) — bitleri n kadar sola kaydır. Matematiksel olarak `x << n == x * 2^n`.
- `>>` (sağa işaretli kaydırma) — bitleri sağa kaydır, soldaki boş yeri **işaret bitiyle** doldur (negatif sayılarda 1).
- `>>>` (sağa işaretsiz kaydırma) — soldaki boş yeri **0** ile doldurur.

Pratik örnek: Bir `int` birçok boolean bayrak tutabilir. `READ = 1, WRITE = 2, EXEC = 4` gibi değerler tanımlayıp `permissions = READ | WRITE` ile kombine edersin. Kontrol: `(permissions & WRITE) != 0` ise WRITE izni var. Unix dosya izinleri tam böyle çalışır.

`2` ile çarpmak/bölmek isteyenler eskiden `x << 1` / `x >> 1` yazarlardı. Modern JIT bu optimizasyonu zaten yaptığı için anlam farkı yoksa **normal yaz**.

---

## 5) Atama Operatörleri

`=` basit atama. Kısa yollar:

| Yazılış | Anlamı |
|---|---|
| `x += y` | `x = x + y` |
| `x -= y` | `x = x - y` |
| `x *= y` | `x = x * y` |
| `x /= y` | `x = x / y` |
| `x %= y` | `x = x % y` |
| `x <<= y`, `x >>= y`, `x >>>= y` | bit kaydırma ataması |
| `x &= y`, `x \|= y`, `x ^= y` | bit atamaları |

Bir ince ayrıntı: `x += y` içinde **örtük cast** vardır. Yani `byte b = 10; b += 100_000;` derlenir (çünkü `b = (byte)(b + 100_000)` gibi davranır ve sessizce truncate eder), ama `b = b + 100_000;` yazarsan derlenmez. Bu aslında bir tuzak, çünkü veri kaybı fark etmeden olur.

---

## 6) Üçlü (Ternary) Operatör

Tek satırlık `if-else` gibi:
```java
int max = (a > b) ? a : b;
String label = (age >= 18) ? "yetişkin" : "çocuk";
```

Karmaşık mantık için uygun değil (iç içe ternary kâbus olur), ama basit iki-yollu değer seçiminde okunaklıdır. Bir de `null`-safe string formatlaması gibi durumlarda işe yarar: `String s = obj != null ? obj.toString() : "yok";`

---

## 7) `instanceof` Operatörü

Bir referansın hangi sınıfın/arayüzün örneği olduğunu kontrol eder:

```java
if (obj instanceof String) {
    String s = (String) obj;
    System.out.println(s.length());
}
```

Java 16'dan itibaren **pattern matching** ile bu iki adım birleşti:

```java
if (obj instanceof String s) {
    // s burada otomatik String tipinde, cast'e gerek yok
    System.out.println(s.length());
}
```

`null instanceof AnySınıf` her zaman **false** döner. Bu yüzden `obj != null && obj instanceof X` yazmana gerek yok.

---

## 8) Operatör Önceliği

Hangi operatör önce çalışır? Tablonun tamamını ezberlemek yerine **parantez kullan**. Karanlıkta kalmayı sevenler için sıra (üsttekiler önce):

1. Postfix: `expr++`, `expr--`
2. Prefix & unary: `++expr`, `--expr`, `+expr`, `-expr`, `!`, `~`
3. Multiplicative: `*`, `/`, `%`
4. Additive: `+`, `-`
5. Shift: `<<`, `>>`, `>>>`
6. Relational: `<`, `<=`, `>`, `>=`, `instanceof`
7. Equality: `==`, `!=`
8. Bitwise AND: `&`
9. Bitwise XOR: `^`
10. Bitwise OR: `|`
11. Logical AND: `&&`
12. Logical OR: `||`
13. Ternary: `?:`
14. Assignment: `=`, `+=`, `-=`, ...

---

## 9) `if / else if / else`

Koşullu akışın temel taşı:

```java
if (score >= 90) {
    System.out.println("AA");
} else if (score >= 80) {
    System.out.println("BA");
} else if (score >= 70) {
    System.out.println("BB");
} else {
    System.out.println("FF");
}
```

**Önerim**: Tek satırlık bloklar için bile parantez `{}` kullan. Bu bir stil değil **güvenlik** meselesidir. Şu ünlü Apple "goto fail" bug'ı parantezsiz if'ler yüzünden oldu. Parantez yazmak 0.5 saniye alır, olmaması saatlerce hata aramana mal olabilir.

**`if` zinciri çok uzarsa** (5+ `else if`) dur ve yapını sorgula. Muhtemelen:
- `switch` daha uygun,
- ya da `Map<Key, Value>` veya polymorphism işi daha güzel çözer.

---

## 10) `switch` — Klasik Form

`switch` bir değeri birçok sabite karşılaştırır:

```java
switch (gun) {
    case 1:
        System.out.println("Pazartesi");
        break;
    case 2:
        System.out.println("Salı");
        break;
    default:
        System.out.println("?");
}
```

**Fall-through tuzağı**: `break` unutursan, bir case yakalandıktan sonra aşağıdaki case'ler **otomatik çalışır**. Bu bazen kasıtlı yapılır (iki case aynı davranışı paylaşır), ama çoğu zaman hatadır:

```java
switch (gun) {
    case 1:
    case 2:
    case 3:
    case 4:
    case 5:
        System.out.println("Hafta içi"); break;
    case 6:
    case 7:
        System.out.println("Hafta sonu"); break;
}
```

Klasik switch destekleyen tipler: `int` (ve daha küçük tam sayılar), `char`, `String` (Java 7+), `enum`.

---

## 11) Switch Expression (Java 14+) — Modern Form

Java 14 ile switch artık **bir değer üretebilir** (expression olarak). Yeni `->` sözdizimi çok daha temiz:

```java
String ad = switch (gun) {
    case 1 -> "Pazartesi";
    case 2, 3 -> "Salı/Çarşamba";
    case 4, 5 -> "Perşembe/Cuma";
    case 6, 7 -> "Hafta sonu";
    default   -> "?";
};
```

Farkları:
- `->` kullanıldığında **fall-through yok** (break gerekmez, otomatik her case bağımsız).
- `case a, b, c` ile birden fazla değer tek kolda.
- Sonuç bir değişkene atanabilir (expression).
- **Exhaustive** olmalı: tüm olası değerler kapanmalı, yoksa derleyici zorlar (özellikle enum ve sealed class'larda).

Blok gövdesi gerekirse `yield` ile değer döndür:
```java
String s = switch (tip) {
    case "A" -> "tek";
    case "B" -> {
        // karmaşık hesap
        int x = 10;
        yield "çift-" + x;
    }
    default -> "bilinmez";
};
```

Java 21 ile **pattern matching** switch'e de geldi:
```java
Object obj = 42;
String s = switch (obj) {
    case Integer i when i > 0 -> "pozitif int: " + i;
    case Integer i             -> "int: " + i;
    case String str            -> "string: " + str;
    case null                  -> "null";
    default                    -> "bilinmeyen";
};
```

---

## 12) Döngüler

### `for` — klasik sayaçlı
```java
for (int i = 0; i < 10; i++) {
    System.out.println(i);
}
```
Üç parça: **başlangıç**, **koşul**, **adım**. Her üçü de opsiyonel. `for (;;)` sonsuz döngüdür.

### `for-each` (enhanced for) — koleksiyon ve dizi için
```java
for (String s : list) {
    System.out.println(s);
}
```
İndeks'e ihtiyacın yoksa bunu tercih et. Dahili olarak `Iterator` kullanır ve `list.remove()` yaparsan `ConcurrentModificationException` alırsın — bunun için `iterator.remove()` veya `list.removeIf(...)` kullan.

### `while` — koşul önce
```java
while (!done) {
    ...
}
```
Koşul false ise hiç girmez.

### `do-while` — en az bir kere
```java
do {
    menuGoster();
    secim = oku();
} while (secim != 0);
```

### `break` ve `continue`
- `break` döngüden **tamamen çıkar**.
- `continue` bu iterasyonu atla, sonrakine geç.

### Etiketli (Labeled) Break/Continue
İç içe döngülerde dış döngüyü kırmak istersen:
```java
dis:
for (int i = 0; i < n; i++) {
    for (int j = 0; j < m; j++) {
        if (matrix[i][j] == hedef) {
            bulundu = true;
            break dis;   // sadece iç değil, "dis" etiketli dış döngüden de çık
        }
    }
}
```
Bu özellik kullanışlıdır ama çok sık rastlanmaz. Genelde fonksiyon çıkışı (`return`) daha temiz olur.

### Sonsuz Döngü
```java
while (true) { ... }
for (;;) { ... }
```
Server/event-loop tarzı kodda görülür. Her sonsuz döngünün bir **çıkış koşulu** olmalı (break, return, exception) — yoksa program takılır.

---

## Mülakat Soruları ve Örnek Cevaplar

**1. `==` ile `.equals()` arasındaki fark nedir?**
`==` primitive'lerde değer, nesnelerde referans (bellek adresi) karşılaştırır. `.equals()` ise nesnenin içeriğini karşılaştırır (sınıf kendi `equals` metodunu override ettiği sürece). String, Integer gibi sınıflar doğru override etmiştir. Kendi yazdığın sınıflarda `equals` override etmezsen `Object.equals` kullanılır ki o `==` ile aynıdır.

**2. Short-circuit evaluation nedir?**
`&&` ve `||` operatörlerinin sol taraf sonucu belli olduğunda sağ tarafı **hiç değerlendirmemesi**. `false && x` → x çağrılmaz. `true || x` → x çağrılmaz. Hem NPE'den korur hem performans kazandırır.

**3. `switch` statement ile `switch` expression arasındaki fark?**
Statement formu (klasik) değer üretmez, sadece akışı yönlendirir; `break` gerekir, fall-through mümkündür. Expression formu (`->` ile) **değer üretir**, fall-through yoktur, birden fazla case tek kolda yazılabilir, exhaustive (tüm dallar kapanmış) olmak zorundadır. Java 14+ ile geldi.

**4. `&` ile `&&` farkı?**
`&` bit operatörü, ama boolean'da da kullanılabilir; **her iki tarafı da değerlendirir**. `&&` mantıksal AND, **kısa devre** yapar. NPE riskini önlemek ve performans için `&&` tercih edilir.

**5. `i = i++;` ifadesi ne yapar?**
`i` başlangıçta ne ise aynı kalır! Adım adım: sağ taraftaki `i++` önce `i`'nin eski değerini okur, sonra `i`'yi arttırır. Ama sol tarafa atama eski değeri geri yazar. Yani `i` önce 5 → 6 olur, sonra atama 5'i geri yazar → `i = 5`. Tipik tuzak. Asla yazma.

**6. `for-each` içinde koleksiyonu nasıl değiştiririz?**
Doğrudan `list.remove()` çağırmak `ConcurrentModificationException` atar. Çözümler: (a) Klasik `Iterator` kullan ve `iterator.remove()` çağır. (b) `list.removeIf(predicate)` kullan. (c) Yeni bir listeye toplayıp eski listeyi değiştir.

**7. `break` ve `return` farkı?**
`break` sadece bulunduğu döngüden (veya switch'ten) çıkar, metod çalışmaya devam eder. `return` metodun tamamından çıkar. Etiketli `break label` birden fazla döngüyü atlayabilir.

**8. `switch` hangi tiplerle çalışır?**
`byte`, `short`, `int`, `char` (ve wrapper'ları), `String` (Java 7+), `enum`. Java 21+'da pattern matching ile her tip.
