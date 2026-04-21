# Java Core — Sıfırdan İleri Seviyeye Rehber

Bu proje, Java Core konularını **paket paket** ele alan, her paketin içinde **teorik notlar (NOTES.md)** + **çalışan örnek kodlar** bulunan mülakat hazırlık rehberidir.

## Nasıl Kullanılır?
1. Sırayla her paketin `NOTES.md` dosyasını oku.
2. Yanındaki `.java` dosyalarını IntelliJ'de **Run** ile çalıştır, çıktıları gözlemle.
3. Kod üzerinde oyna — değiştir, kır, çalıştır, anla.
4. Her NOTES.md'nin sonunda **Mülakat Soruları** bölümü var; kendini yokla.

## İçindekiler

| # | Paket | Konu |
|---|---|---|
| 01 | [`p01_basics`](src/p01_basics/NOTES.md) | JVM/JDK/JRE, primitive types, casting, `var`, wrapper |
| 02 | [`p02_operators_controlflow`](src/p02_operators_controlflow/NOTES.md) | Operatörler, if/else, switch expression, döngüler |
| 03 | [`p03_strings`](src/p03_strings/NOTES.md) | String immutability, String pool, StringBuilder/Buffer |
| 04 | [`p04_memory`](src/p04_memory/NOTES.md) | Stack vs Heap, Metaspace, static, pass-by-value |
| 05 | [`p05_oop_classes`](src/p05_oop_classes/NOTES.md) | Class, object, constructor, `this`, record |
| 06 | [`p06_inheritance`](src/p06_inheritance/NOTES.md) | extends, super, Object sınıfı, up/down cast |
| 07 | [`p07_polymorphism`](src/p07_polymorphism/NOTES.md) | Overloading, overriding, dynamic dispatch |
| 08 | [`p08_abstract_interface`](src/p08_abstract_interface/NOTES.md) | Abstract class vs interface, default/static method, diamond |
| 09 | [`p09_encapsulation_access`](src/p09_encapsulation_access/NOTES.md) | Access modifiers, `final`, immutability |
| 10 | [`p10_exceptions`](src/p10_exceptions/NOTES.md) | Checked/unchecked, try-with-resources, custom exception |
| 11 | [`p11_collections`](src/p11_collections/NOTES.md) | Array, List, Set, Map, equals/hashCode |
| 12 | [`p12_generics`](src/p12_generics/NOTES.md) | Generics, PECS, wildcards, type erasure |
| 13 | [`p13_multithreading`](src/p13_multithreading/NOTES.md) | Thread, synchronized, volatile, Executor, CompletableFuture |
| 14 | [`p14_gc`](src/p14_gc/NOTES.md) | Garbage Collector, generations, reference tipleri |
| 15 | [`p15_lambda_streams`](src/p15_lambda_streams/NOTES.md) | Lambda, functional interface, Stream API, Optional |
| 16 | [`p16_io_nio`](src/p16_io_nio/NOTES.md) | java.io, NIO, Files, Path, Scanner/BufferedReader |
| 17 | [`p17_reflection`](src/p17_reflection/NOTES.md) | Reflection, `Class<?>`, annotation, dynamic proxy, MethodHandle |

## Uygulama Projeleri (deneme/egzersizler)

| # | Klasör | İçerik |
|---|---|---|
| project_1 | [`src/project_1`](src/project_1) | Araç kiralama (rent-a-car) — `BaseVehicle`, `Car`, `Motorcycle`, `Rentable` interface |
| project_2 | [`src/project_2`](src/project_2) | Basit `Parent` / `Child` inheritance denemesi |
| project_3 | [`src/project_3`](src/project_3) | `Doctor` hiyerarşisi — `Dentist`, `Cardiologist` (abstract/override) |

## Önerilen Okuma Sırası
- **Sıfırdan başlıyorum:** 01 → 02 → 03 → 04 → 05 → 06 → 07 → 08 → 09 → 10 → 11 → 12 → 15 → 13 → 14 → 16 → 17
- **Mülakat tazeleme (1 gün):** 04 (memory), 11 (collections), 13 (thread), 14 (GC), 15 (stream) — klasik sorular buralardan gelir.

## Mülakat için 20 Kritik Konu (hızlı tazeleme)
1. JDK / JRE / JVM farkı
2. `==` vs `.equals()` & String pool
3. Java pass-by-value (her zaman!)
4. Stack vs Heap vs Metaspace
5. `final` `finally` `finalize` üçlüsü
6. Overloading vs Overriding
7. Abstract class vs Interface
8. `equals` / `hashCode` sözleşmesi
9. `HashMap` iç yapısı (array + LL/tree, load factor)
10. `ConcurrentHashMap` neden thread-safe
11. `ArrayList` vs `LinkedList`
12. `synchronized` vs `volatile` vs `Atomic`
13. Deadlock ve önleme
14. `Executor` vs doğrudan `Thread`
15. `CompletableFuture` ile async
16. Generics ve type erasure
17. Lambda ve functional interface
18. Stream API (map/filter/reduce/collect)
19. `Optional` doğru kullanımı
20. GC nesil modeli, strong/weak/soft/phantom reference
21. Reflection: `getFields` vs `getDeclaredFields`, `InvocationTargetException`, dynamic proxy

## Çalıştırma
IntelliJ IDEA'da her `.java` dosyasının `main` metodunu doğrudan çalıştırabilirsin.
Terminal ile:
```bash
# proje kök dizininde
javac -d out src/p01_basics/*.java
java -cp out p01_basics.PrimitiveTypes
```

İyi çalışmalar. Sorudan kaçma, kaynağı oku.
