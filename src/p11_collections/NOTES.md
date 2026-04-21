# 11 — Array ve Collections Framework (List, Set, Map)

Java'da veri yapıları iki büyük kampa ayrılır: **sabit boyutlu array**'ler ve **dinamik Collections Framework**. Günlük kodun neredeyse %70'i koleksiyon işlemleridir — hangisini ne zaman seçeceğin, iç yapılarının nasıl çalıştığı, hangi operasyonun ne maliyetli olduğu mülakatların ekmek-suyudur. Bu paketi hem derinlemesine hem pratik açıdan öğreneceksin.

---

## 1) Array — En İlkel, En Hızlı Yapı

Array, Java'nın en temel koleksiyon yapısıdır:
- **Sabit boyutlu** (yaratıldıktan sonra boyutu değişmez).
- **Aynı tipte** elemanlar tutar (int[], String[], Person[]).
- **Hızlıdır** — bellek ardışık, CPU cache dostu.
- İndex'le erişim **O(1)** (sabit zaman).

```java
int[] a = new int[5];             // {0, 0, 0, 0, 0} (default değerler)
int[] b = {1, 2, 3, 4, 5};        // array literal
String[] names = new String[]{"Ali", "Veli"};
int[][] matrix = new int[3][4];   // 2D
```

Uzunluğa **`a.length`** ile erişilir (parantezsiz! bu bir field). String'de `s.length()` metoddu, array'de `arr.length` field.

### Sık Kullanılan Yardımcılar (`Arrays` sınıfı)
```java
Arrays.sort(arr)                 // yerinde sıralar
Arrays.toString(arr)             // "[1, 2, 3]" formatında string
Arrays.copyOf(arr, newLen)       // kopyala, boyut değiştir
Arrays.equals(a, b)              // içerik karşılaştırma (== YAPMA)
Arrays.fill(arr, val)            // tüm hücreleri doldur
Arrays.binarySearch(sorted, key) // SIRALI array'de arama O(log n)
Arrays.stream(arr)               // Stream<> (15. paket)
```

### Array'in Kısıtları
- Boyut sabit — yeni eleman eklemek için yeni array + kopyalama.
- `arr == arr2` referans karşılaştırır (içerik için `Arrays.equals`).
- Generic'lerle çalışmaz iyi: `T[]` yaratmak sorunlu, "array of generic type" yasaktır.
- Multi-dimensional array aslında "array of arrays"tır; satırlar farklı uzunlukta olabilir (jagged).

Bu kısıtlar yüzünden günlük kodda çoğunlukla **`ArrayList`** tercih edilir. Array sadece performans kritik yerlerde veya primitive (int[], double[]) ihtiyacı olduğunda kullanılır.

---

## 2) Collections Framework — Büyük Resim

Java Collections Framework, tüm dinamik koleksiyonların çatısıdır. Dört ana "köşe taşı":

```
Iterable (her şeyin atası — for-each için)
└── Collection
    ├── List       — sıralı, tekrar olabilir, index'li erişim
    ├── Set        — tekrar yok, benzersizlik garantisi
    └── Queue/Deque — FIFO/LIFO, uçlardan erişim

Map              — Collection'dan BAĞIMSIZ — key/value
```

Map, `Collection` hiyerarşisinin **parçası değildir** (bu bilgiyi mülakatta sor). Ayrı bir arayüzdür ama ekosistemin parçasıdır.

Her ana arayüzün somut implementasyonları:
- **List** → ArrayList, LinkedList, Vector (legacy), Stack (legacy)
- **Set** → HashSet, LinkedHashSet, TreeSet
- **Queue/Deque** → ArrayDeque, LinkedList, PriorityQueue
- **Map** → HashMap, LinkedHashMap, TreeMap, Hashtable (legacy), ConcurrentHashMap

---

## 3) List — Sıralı, İndex'li

List kullanıcılarından bekledikleri:
- Elemanlara **eklenme sırası** korunur.
- **İndex'le erişim** var (`get(i)`).
- **Tekrar eden** elemanlara izin var.
- Aynı eleman birden fazla olabilir.

### ArrayList — Array'in Dinamik Versiyonu
- İçte bir `Object[]` tutar.
- Dolduğunda **1.5 katına** büyür (Java 8+), yeni array'e kopyalar.
- `get(i)`, `set(i, v)` — **O(1)**.
- `add(v)` sonuna — **amortize O(1)** (bazen resize yaşar).
- `add(i, v)` ortadaki pozisyona — **O(n)** (sağındakileri kaydırmak gerek).
- `remove(i)` — **O(n)** (aynı sebepten).

`ArrayList` günlük Java kodunun **en sık kullanılan** koleksiyonudur. Çoğu durumda default seçimin olsun.

Kapasite yönetimi:
```java
ArrayList<Integer> list = new ArrayList<>(1000);   // başlangıç kapasitesi
```
Toplam eleman sayısını biliyorsan başta ver; yeniden boyutlandırmalardan kaçın.

### LinkedList — Çift Yönlü Bağlı Liste
- Her eleman bir **node**: {değer, prev, next}.
- `add(0, v)` ve `addFirst(v)` — **O(1)** (baş/son ekleme ucuz).
- `get(i)` — **O(n)** (baştan veya sondan yürümek gerek).
- `Deque` interface'ini de implement eder — stack/queue gibi de kullanılabilir.

Popüler inanışın aksine LinkedList **çoğu durumda ArrayList'ten yavaştır**. Sebep:
- Her node için ekstra pointer overhead (prev, next).
- Node'lar bellekte dağınık → cache miss.
- `get(i)` yavaş.

Gerçekten "baş/son ağırlıklı ekleme/çıkarma" yapıyorsan tercih et (queue implementasyonu gibi). Diğer durumlarda ArrayList.

### Vector ve Stack — Legacy
`Vector` = synchronized ArrayList (Java 1.0). Tüm metodları `synchronized`, yavaştır. Modern kodda **kullanma**. `Stack` da Vector'dan türer; modern stack için `ArrayDeque` kullan.

### List Performans Kıyaslama

| Operasyon | ArrayList | LinkedList |
|---|---|---|
| `get(i)` | O(1) | O(n) |
| `add` (sona) | amortize O(1) | O(1) |
| `add(0, v)` (başa) | O(n) | O(1) |
| `remove(i)` | O(n) | O(n) — bulmak + unlink |
| İç bellek | Kompakt | Node overhead |
| Cache | Dost | Dağınık |

---

## 4) Set — Benzersizlik

`Set`'in tek kuralı: **aynı eleman iki kez olamaz**. `add` ettiğinde varsa yutulur, eklenmez.

### HashSet
- İçte bir `HashMap` kullanır (key = eleman, value = DUMMY).
- `add`, `contains`, `remove` — **O(1)** (hashCode/equals kalitesine bağlı).
- **Sıra korunmaz** — iteration sırası deterministik değil.
- Null bir eleman ekleyebilirsin.

Kullanım: "Listede var mı yok mu" kontrolleri, unique değerler topla.

### LinkedHashSet
HashSet + ekleme sırası korunur (linked list ile). `HashSet`'in `insertion order` versiyonu. Maliyet biraz daha fazla ama sıra istiyorsan bedava gibi.

### TreeSet
- İçte `TreeMap` (Red-Black tree) kullanır.
- Elemanlar **sıralı** tutulur (`compareTo` veya Comparator'a göre).
- `add`, `contains`, `remove` — **O(log n)** (hashlemek yerine ağaç gezmek).
- Range sorguları: `subSet(from, to)`, `headSet(limit)`, `tailSet(start)`.
- `first()`, `last()` — en küçük, en büyük.

`TreeSet`'e koyduğun elemanlar **Comparable** olmalı veya `Comparator` verilmeli.

---

## 5) Queue ve Deque

### Queue — FIFO (First In, First Out)
```java
Queue<Integer> q = new LinkedList<>();
q.offer(1); q.offer(2); q.offer(3);
q.poll();   // 1 (başta)
q.peek();   // 2 (bakar ama çıkarmaz)
```

### Deque — Double-Ended Queue
Her iki uçtan ekleme/çıkarma:
```java
Deque<Integer> dq = new ArrayDeque<>();
dq.addFirst(1); dq.addLast(2);
dq.removeFirst(); dq.removeLast();
```

`ArrayDeque` stack olarak da kullanılır — `Stack` sınıfından daha hızlıdır:
```java
Deque<Integer> stack = new ArrayDeque<>();
stack.push(1); stack.push(2);
stack.pop();   // 2
stack.peek();  // 1
```

### PriorityQueue — Öncelik Kuyruğu (Min-Heap)
```java
PriorityQueue<Integer> pq = new PriorityQueue<>();
pq.offer(5); pq.offer(1); pq.offer(3);
pq.poll();   // 1 — her zaman en küçük
pq.poll();   // 3
```
Max-heap için: `new PriorityQueue<>(Comparator.reverseOrder())`.

---

## 6) Map — Anahtar-Değer Eşlemesi

`Map` en önemli koleksiyon ailesidir. `Collection`'dan türemez ama koleksiyon çatısının parçasıdır.

### HashMap — En Yaygın (Java 8 Sonrası İç Yapı)
- İçte bir array (bucket'lar) var.
- Key'in `hashCode`'u bucket index'ine dönüştürülür.
- Aynı bucket'a düşenler (**collision**) — önce linked list olarak tutulur.
- Bucket'taki eleman sayısı **8**'i geçerse (ve toplam bucket ≥ 64), o bucket **Red-Black Tree**'ye dönüştürülür (TREEIFY_THRESHOLD). Bu Java 8'in büyük iyileştirmesi — collision durumunda bile O(log n) garantisi.
- Default capacity 16, load factor 0.75. `size > capacity * 0.75` olunca **resize** (2x).

Operasyonlar:
- `put`, `get`, `remove`, `containsKey` — **O(1) amortize** (hash iyiyse).
- En kötü durum tree sayesinde **O(log n)**.

Özellikleri:
- **Sıra korunmaz**.
- Null key **bir tane** olabilir, null value birden çok.
- Thread-safe **değil**.

### LinkedHashMap
`HashMap` + ekleme sırası (veya access order). Ekleme sırasını korumak istersen veya **LRU cache** inşa etmek istersen:
```java
new LinkedHashMap<>(16, 0.75f, true) {    // access-order = true
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return size() > 100;               // en eski 100'ü aşınca sil
    }
}
```

### TreeMap
- Red-Black Tree üzerine kurulu.
- Key'ler **sıralı**.
- `put`, `get`, `remove` — **O(log n)**.
- Range sorguları: `subMap`, `headMap`, `tailMap`, `firstKey`, `lastKey`.
- Key'ler Comparable olmalı veya Comparator verilmeli.

### Hashtable — Legacy
Java 1.0. Synchronized ama ilkel. Modern kodda **ConcurrentHashMap** kullan.

### ConcurrentHashMap
Thread-safe HashMap. Java 8 öncesi segmented locking; Java 8+ bucket-level locking + CAS. `synchronizedMap(hashMap)`'ten çok daha hızlı.

**Özellik**: null key/value kabul etmez (thread-safe kontrol için).

---

## 7) `equals` ve `hashCode` Sözleşmesi — HAYATİ

Kendi sınıfını `HashSet`/`HashMap` içinde key olarak kullanmak istiyorsan, `equals` ve `hashCode`'u **beraber** override etmek zorundasın.

**Sözleşmenin Kuralları**:
1. `a.equals(b)` true ise `a.hashCode() == b.hashCode()` olmalı (zorunlu).
2. `a.hashCode() == b.hashCode()` olması `a.equals(b)`'yi gerektirmez (collision olabilir).
3. `hashCode()` aynı nesnede, equals-relevant field'lar değişmediği sürece **tutarlı** olmalı.
4. `equals` refleksif (`a.equals(a)` true), simetrik, transitif, tutarlı, null için false olmalı.

### Neden Önemli?
HashMap/HashSet bir nesneyi arar:
1. `hashCode()` → bucket index bulunur.
2. O bucket'taki her elemanla `equals()` kontrolü.
3. Eşit bulunursa dönüş.

Sözleşme bozulursa: aynı kabul ettiğin iki nesne farklı bucket'larda durur. `set.add(x)` → `set.contains(x)` → **false**!

### Bozuk Örnek
```java
class User {
    String name;
    @Override public boolean equals(Object o) {    // hashCode EKSİK
        return o instanceof User u && name.equals(u.name);
    }
}

Set<User> set = new HashSet<>();
set.add(new User("Ali"));
set.contains(new User("Ali"));   // muhtemelen false!
```

### Doğrusu
```java
@Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User u)) return false;
    return Objects.equals(name, u.name);
}
@Override public int hashCode() {
    return Objects.hash(name);
}
```

IDE'nin otomatik üretmesine izin ver. `record` kullanırsan otomatik doğru olur.

---

## 8) Iterator ve ConcurrentModificationException

Koleksiyon içinde gezerken koleksiyonu **değiştirmek** (add/remove) fail-fast iteration'da `ConcurrentModificationException` atar:

```java
List<Integer> list = new ArrayList<>(List.of(1,2,3,4,5));
for (Integer n : list) {
    if (n == 3) list.remove(n);   // CME!
}
```

### Doğru Yollar

**a) Iterator.remove**:
```java
Iterator<Integer> it = list.iterator();
while (it.hasNext()) {
    if (it.next() == 3) it.remove();
}
```

**b) `removeIf` (Java 8+)**:
```java
list.removeIf(n -> n == 3);
```

**c) Yeni koleksiyon oluştur** (stream veya loop):
```java
List<Integer> filtered = list.stream()
    .filter(n -> n != 3)
    .toList();
```

### Fail-Fast vs Fail-Safe Iterator
- **Fail-fast**: modifikasyonu fark eder ve CME atar (ArrayList, HashMap vs.).
- **Fail-safe**: iterasyon başladığında **snapshot** alır, modifikasyonu fark etmez (CopyOnWriteArrayList, ConcurrentHashMap). Consistency'yi feda eder, güvenlik kazanır.

---

## 9) Immutable Koleksiyonlar (Java 9+)

Değiştirilemeyen koleksiyonlar `List.of()`, `Set.of()`, `Map.of()` ile:
```java
List<Integer> nums = List.of(1, 2, 3);
Set<String> colors = Set.of("red", "green", "blue");
Map<String, Integer> scores = Map.of("Ali", 90, "Veli", 85);
```
`add`, `remove`, `put` → `UnsupportedOperationException`. Null kabul etmez.

Eski yaklaşım: `Collections.unmodifiableList(list)` — wrapper'dır, altta hâlâ mutable, wrapper değiştirmez.

---

## 10) `Collections` vs `Collection` — Küçük Harf Devi

Sık karıştırılır:
- **`Collection`** — arayüz. Tüm Collections Framework'ün atası.
- **`Collections`** — static utility class. Yardımcı metodlar burada.

Faydalı `Collections` metodları:
```java
Collections.sort(list)                // yerinde sıralar
Collections.reverse(list)
Collections.shuffle(list)
Collections.min(list), max(list)
Collections.unmodifiableList(list)    // read-only wrapper
Collections.synchronizedList(list)    // thread-safe wrapper
Collections.emptyList(), emptySet(), emptyMap()
Collections.singletonList(x)
Collections.frequency(list, elem)     // kaç kere geçiyor
```

---

## 11) Seçim Rehberi — Hangi Koleksiyon Ne Zaman?

| İhtiyaç | Seçim |
|---|---|
| Sıralı, index'li, genel amaçlı | `ArrayList` |
| Baş/son ağırlıklı ekleme/çıkarma | `ArrayDeque` |
| Unique elemanlar | `HashSet` |
| Unique + ekleme sırası korunsun | `LinkedHashSet` |
| Unique + sıralı | `TreeSet` |
| Key-value, hız öncelik | `HashMap` |
| Key-value + key sıralı | `TreeMap` |
| Key-value + ekleme sırası | `LinkedHashMap` |
| Thread-safe key-value | `ConcurrentHashMap` |
| LRU cache | `LinkedHashMap` (accessOrder=true) |
| Öncelikli kuyruk | `PriorityQueue` |
| Read-only snapshot | `List.of(...)`, `Set.copyOf(...)` |

---

## 12) Yaygın Hatalar

**a) `Arrays.asList(arr)` fixed-size**:
```java
List<Integer> list = Arrays.asList(1, 2, 3);
list.add(4);   // UnsupportedOperationException
```
Modifikasyon istiyorsan: `new ArrayList<>(Arrays.asList(1, 2, 3))`.

**b) `HashMap` null key ile çağrı şaşırtıcı davranabilir** — ConcurrentHashMap'te null kabul edilmez.

**c) `List.of(...)` null içermez** — `null` elemanı eklersen `NullPointerException`.

**d) `LinkedList`'e körü körüne güvenme** — çoğu durumda ArrayList hızlıdır.

**e) HashSet'te key olarak kullanılan nesneyi sonradan değiştirmek**:
```java
Set<MyKey> set = new HashSet<>();
MyKey k = new MyKey("a");
set.add(k);
k.setValue("b");   // hashCode değişti! set'te kayıp oldu artık
```

---

## Mülakat Soruları ve Örnek Cevaplar

**1. ArrayList ile LinkedList arasındaki fark?**
ArrayList Array tabanlıdır — random access O(1), ortaya ekleme/silme O(n), cache-friendly, çoğu durumda hızlı. LinkedList çift yönlü bağlı liste — baş/son ekleme O(1), random access O(n), her node pointer overhead taşır. Modern Java'da ArrayList çoğu senaryo için daha iyi performans verir.

**2. HashMap nasıl çalışır? (Java 8+)**
Array + linked list (veya tree) yapısı. Key'in `hashCode()`'u bucket index'ine dönüşür. Aynı bucket'a düşenler linked list olarak tutulur; 8'i aşarsa Red-Black Tree'ye dönüşür (O(log n) garantisi). Default capacity 16, load factor 0.75 — doluluk bu oranı geçince bucket sayısı 2x büyür ve rehashing yapılır.

**3. equals/hashCode sözleşmesi nedir?**
`a.equals(b)` true ise `a.hashCode() == b.hashCode()` **olmak zorunda**. Tersi zorunlu değil — collision olabilir. Bozulursa HashMap/HashSet'te aynı kabul ettiğin nesneler farklı bucket'lara düşer, `contains` false döner, `get` null döner.

**4. HashMap'te null key ve value mümkün mü?**
Null key **bir tane** kabul edilir (index 0 bucket'ında tutulur). Null value birden fazla olabilir. `ConcurrentHashMap` ise ne null key ne null value kabul eder (thread-safe ayrımı için).

**5. ConcurrentHashMap nasıl thread-safe sağlar?**
Java 8 öncesi **segment-level locking** (16 segment, her biri ayrı lock). Java 8+ **bucket-level locking + CAS** (Compare-And-Swap). Read'ler genelde lock'suz (volatile + happens-before). Yazma sadece ilgili bucket'ı kilitler. `Hashtable` veya `synchronizedMap`'ten çok daha hızlı.

**6. Fail-fast vs fail-safe iterator?**
Fail-fast iterator koleksiyon değiştiğinde `ConcurrentModificationException` atar (ArrayList, HashMap). Fail-safe iterator snapshot üzerinde çalışır, modifikasyonu görmez (ConcurrentHashMap, CopyOnWriteArrayList). Biri consistency, diğeri güvenlik önceliği.

**7. Stack sınıfı neden kullanılmamalı?**
`Vector`'dan türer, tüm metodlar synchronized (performans), Java 1.0 tasarımıdır (iç tutarsızlıklar). Modern stack için **`ArrayDeque`** kullan — `push`, `pop`, `peek` metodları vardır ve synchronized değildir.

**8. TreeMap ne zaman kullanılır?**
Key'lerin sıralı olması gerektiğinde. Range sorguları (`subMap`, `headMap`), `firstKey`/`lastKey` ihtiyaçlarında. Sıralı iterasyon. Hızı O(log n) — HashMap'ten (O(1)) yavaştır ama sıra garantisi var.

**9. Iterator.remove ile list.remove farkı?**
Iterator.remove güvenli — iteration sırasında CME atmaz. list.remove (direkt çağrı) fail-fast iterator tarafından CME olarak algılanır. Ayrıca `removeIf` predicate alır, daha okunur.

**10. Immutable list nasıl oluşturulur?**
Java 9+: `List.of(...)`. Daha eski: `Collections.unmodifiableList(...)` (wrapper, orijinal değişirse görünür). `List.copyOf(collection)` kopya alıp immutable döner. `record`'larda getter otomatik unmodifiable dönüyor olabilir.

**11. HashMap resize ne zaman olur?**
`size > capacity * loadFactor` koşulunda. Default 16 * 0.75 = 12. 13. eleman eklendiğinde capacity 32'ye çıkar ve tüm entry'ler rehash edilir (Java 8'de optimize: yeni bit'e göre ya aynı bucket'ta ya da eski + capacity bucket'ında).

**12. Vector, ArrayList, ConcurrentHashMap farkı?**
Vector synchronized, legacy, yavaş. ArrayList not synchronized, hızlı, modern. Collections.synchronizedList wrapper — Vector'dan daha az yavaş ama hâlâ kaba. ConcurrentHashMap akıllı locking ile paralelizm destekler, modern concurrent durumlar için doğru seçim.
