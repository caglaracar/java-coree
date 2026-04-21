# 16 — Java I/O ve NIO

Her uygulamanın bir noktada dosya okuması, ağdan veri alması veya kullanıcı girdisi işlemesi gerekir. Java bu ihtiyaçları iki farklı API ile karşılar: eski **java.io** (Java 1.0 — stream-based, blocking) ve modern **java.nio** (Java 1.4 — buffer/channel-based, non-blocking). Çoğu yeni kod için **`java.nio.file.Files` + `Path`** kombinasyonu (Java 7 NIO.2) en iyi seçimdir. Bu paketi bitirdiğinde hangi senaryoda hangi API'yi kullanacağını, neden buffered stream'lerin hızlı olduğunu ve neden dosya açtıktan sonra mutlaka kapatman gerektiğini bileceksin.

---

## 1) İki Dünya — IO vs NIO

### java.io (Classic)
Java 1.0 ile geldi. **Stream-based**: byte/char akışlarını tek tek okur/yazar. **Blocking**: `read()` çağrısı veri gelene kadar bekler.

- `InputStream`, `OutputStream` (byte stream'ler)
- `Reader`, `Writer` (char stream'ler)
- Her çağrı OS'a gidebilir → yavaş, `BufferedX` sarmalayıcılar şart.

### java.nio (New I/O, Java 1.4)
- **Channel + Buffer** modeli.
- **Non-blocking** mode destekler (Selector ile).
- Memory-mapped file desteği.
- Daha düşük seviyeli, performans odaklı.

### java.nio.file (NIO.2, Java 7)
- `Path`, `Files`, `FileSystem` — dosya sistemi API'si.
- Modern, deklaratif.
- `try-with-resources` ile mükemmel uyum.
- **Yeni kod için default seçim.**

---

## 2) Byte Stream vs Character Stream

### Byte Stream — 8 bit, Binary
- Abstract: `InputStream`, `OutputStream`.
- Concrete: `FileInputStream`, `FileOutputStream`, `ByteArrayInputStream`.
- Buffered: `BufferedInputStream`, `BufferedOutputStream`.
- **Kullanım**: resim, video, zip, binary protokol.

### Character Stream — 16 bit (UTF-16), Text
- Abstract: `Reader`, `Writer`.
- Concrete: `FileReader`, `FileWriter`, `StringReader`.
- Buffered: `BufferedReader`, `BufferedWriter`.
- **Kullanım**: metin dosyaları, CSV, JSON, log.

### Bridge — Byte ↔ Char Geçişi
Dosyadan byte okuyup character'e çevirme köprüsü:
```java
Reader r = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
Writer w = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
```

**ÖNEMLİ**: Encoding'i açıkça belirt. Vermezsen platform default (çoğu yerde UTF-8 ama Windows'ta farklı olabilir) kullanılır ve taşınabilir olmayan kod yazmış olursun. **Her zaman UTF-8**.

---

## 3) Neden Buffered Stream'ler?

`FileInputStream.read()` çağrısı her çağrıda **OS sistem çağrısı** yapar. Her byte için OS'a gidip gelmek yavaştır.

```java
// Yavaş
FileInputStream in = new FileInputStream("big.txt");
int c;
while ((c = in.read()) != -1) { ... }   // her byte için system call
```

`BufferedInputStream` araya kendi buffer'ını (default 8192 byte) koyar. OS'tan bir defada 8 KB okur, sonra belleğinden verir:

```java
BufferedInputStream in = new BufferedInputStream(new FileInputStream("big.txt"));
```

Aynı dosyayı okuma 10x-100x hızlanabilir.

**Kural**: Stream'leri her zaman buffered sarmalayıcıya sar — açık performans farkı yoksa bile alışkanlık yap.

---

## 4) try-with-resources — Kapanışı Unutmak Geçmişte Kaldı

Stream'ler `AutoCloseable`'dır. Açtıkların mutlaka kapanmalı yoksa file handle sızıntısı. Eski yaklaşım:

```java
BufferedReader br = null;
try {
    br = new BufferedReader(new FileReader("f.txt"));
    // ...
} finally {
    if (br != null) {
        try { br.close(); } catch (IOException e) { /* ignore */ }
    }
}
```

Java 7 ile try-with-resources:
```java
try (BufferedReader br = new BufferedReader(new FileReader("f.txt"))) {
    // ...
}   // br otomatik kapanır
```

Birden fazla kaynak:
```java
try (FileReader fr = new FileReader(src);
     FileWriter fw = new FileWriter(dst)) {
    // ...
}
```
Kapanış sırası **ters** (fw önce, fr sonra — LIFO).

---

## 5) Modern Dosya İşleme — `Path` + `Files`

Java 7 NIO.2 ile gelen `Path` ve `Files` sınıfları, dosya işlemlerini deklaratif yaptı:

### Path Oluşturma
```java
Path p = Paths.get("data/users.csv");    // eski stil
Path p = Path.of("data/users.csv");      // Java 11+, tercih edilir
Path abs = Path.of("/home/user/log.txt");

// Parça parça
Path p = Path.of("home", "user", "file.txt");
```

### Yaygın Path Operasyonları
```java
p.getFileName()     // sadece isim
p.getParent()       // üst dizin
p.toAbsolutePath()  // mutlak hale getir
p.normalize()       // ".." ve "." temizle
p.resolve("sub")    // altına ekle
p.relativize(other) // göreceli path hesapla
```

### Files — Utility Goldmine
```java
// Varlık kontrolü
Files.exists(p), Files.notExists(p)
Files.isRegularFile(p), Files.isDirectory(p)
Files.isReadable(p), Files.isWritable(p)

// Oluşturma/silme
Files.createFile(p)
Files.createDirectories(p)         // tüm parent'ları da
Files.delete(p)
Files.deleteIfExists(p)

// Kopyalama/taşıma
Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING)
Files.move(src, dst)

// Meta
Files.size(p)                      // byte cinsinden
Files.getLastModifiedTime(p)
```

### Metin Okuma/Yazma — Tek Satır
```java
// Yazma
Files.writeString(p, "merhaba\ndünya\n", StandardCharsets.UTF_8);
Files.write(p, List.of("line1", "line2"), StandardCharsets.UTF_8);

// Okuma
String content = Files.readString(p, StandardCharsets.UTF_8);
List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
```

**Uyarı**: `readAllLines` tüm dosyayı belleğe yükler. Dev dosyalar (GB seviyesi) için bu kötü fikir:

```java
// Streaming — lazy
try (Stream<String> lines = Files.lines(p, StandardCharsets.UTF_8)) {
    lines.filter(l -> l.contains("ERROR"))
         .limit(100)
         .forEach(System.out::println);
}
```

---

## 6) Scanner vs BufferedReader

Kullanıcı girdisi veya dosya okumada iki seçenek:

### Scanner
```java
Scanner sc = new Scanner(System.in);
String line = sc.nextLine();
int n = sc.nextInt();
```
- Parse kabiliyeti var: `nextInt`, `nextDouble`, regex delimiter.
- Daha yavaş (parse overhead).
- Console girdisi için rahat.

### BufferedReader
```java
BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
String line = br.readLine();
int n = Integer.parseInt(line);
```
- Sadece `readLine()` — parse'ı sen yaparsın.
- Çok daha hızlı.
- Büyük dosya okumada tercih et.

---

## 7) Serialization — Kısaca

**Serialization**: Bir nesneyi byte akışına çevirme (disk/ağ için). **Deserialization**: ters işlem.

```java
public class Person implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private transient String password;    // transient = serialize EDİLMEZ
}
```

Yazma:
```java
try (ObjectOutputStream oos = new ObjectOutputStream(
        new FileOutputStream("person.ser"))) {
    oos.writeObject(person);
}
```

Okuma:
```java
try (ObjectInputStream ois = new ObjectInputStream(
        new FileInputStream("person.ser"))) {
    Person p = (Person) ois.readObject();
}
```

### `serialVersionUID`
Sınıfın "sürüm numarası". Serialize edilmiş nesne deserialize edilirken sınıf versiyon kontrolü için. Açıkça belirtmezsen derleyici otomatik üretir — sınıf değiştiğinde `InvalidClassException`.

### Java Serialization Güvenlik Zayıflığı
Uzaktan gelen serialized data'yı deserialize etmek büyük güvenlik açığı — gadget chain saldırıları. Modern yaklaşımlar: **JSON (Jackson, Gson), Protocol Buffers, Avro**. Java serialization sadece iç kullanım ve güvenli ortamlarda.

---

## 8) Character Encoding — Hiç İhmal Etme

Metin işlemede **encoding** neredeyse her bug'ın arkasındadır.

### Sorun
```java
// Platform default encoding — taşınabilir DEĞİL
FileReader fr = new FileReader("file.txt");
```

### Çözüm — Her Zaman Açıkça UTF-8
```java
BufferedReader br = Files.newBufferedReader(p, StandardCharsets.UTF_8);
String s = Files.readString(p, StandardCharsets.UTF_8);
```

`StandardCharsets` sabitleri: `UTF_8`, `UTF_16`, `ISO_8859_1`, `US_ASCII`.

Bir text dosyasında Türkçe karakter bozuksa, encoding mismatch'tir. Dosya hangi encoding ile kaydedildiyse aynısıyla açmalısın.

---

## 9) NIO Channels ve Buffers — Düşük Seviye Güç

```java
try (FileChannel channel = FileChannel.open(p, StandardOpenOption.READ)) {
    ByteBuffer buf = ByteBuffer.allocate(1024);
    int bytesRead = channel.read(buf);
    buf.flip();                             // yazma modu → okuma modu
    while (buf.hasRemaining()) {
        System.out.print((char) buf.get());
    }
}
```

- **Channel**: bidirectional, hem okur hem yazar.
- **Buffer**: veri tutucu, position/limit/capacity/mark yönetir.
- `flip()`, `clear()`, `rewind()` — buffer mod geçişleri.

### Memory-Mapped File
Çok büyük dosyaları belleğe map et, pointer gibi kullan:
```java
try (FileChannel ch = FileChannel.open(p, StandardOpenOption.READ)) {
    MappedByteBuffer mapped = ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());
    // mapped üzerinden erişim — OS disk I/O'yu lazy yapar
}
```

### Selector — Non-blocking Multi-IO
Bir thread birden fazla bağlantıyı izler. Klasik örnek: web server.
```java
Selector selector = Selector.open();
channel.configureBlocking(false);
channel.register(selector, SelectionKey.OP_READ);
// selector.select() ile hazır olanları al
```
Bu çok spesifik bir alan — Netty, Jetty gibi framework'ler bunu zaten soyutladı.

---

## 10) Dizin Gezme — `Files.walk`

```java
try (Stream<Path> paths = Files.walk(Path.of("."), 3)) {
    paths.filter(Files::isRegularFile)
         .filter(p -> p.toString().endsWith(".java"))
         .forEach(System.out::println);
}
```

`walk(root, depth)` — recursive, tembel. Maksimum derinlik verebilirsin. Hata yakala: `IOException`.

Alternatif: `Files.walkFileTree(root, visitor)` — custom visitor pattern, daha fazla kontrol.

---

## 11) Watching Files — `WatchService`

Dosya değişikliklerini izlemek için:
```java
WatchService watcher = FileSystems.getDefault().newWatchService();
Path dir = Path.of("/tmp");
dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

WatchKey key = watcher.take();   // bloklar
for (WatchEvent<?> event : key.pollEvents()) {
    System.out.println(event.kind() + ": " + event.context());
}
```

Hot-reload, config dosyası izleme, build tool'ları için kullanılır.

---

## 12) Yaygın Hatalar ve İpuçları

**a) Stream kapatmamak.** File handle sızıntısı. Try-with-resources kullan.

**b) Encoding belirtmemek.** Platform farklılığı bug'ı. Her zaman UTF-8 açıkça.

**c) Büyük dosyayı `readAllLines` ile okumak.** Bellek sıkıntısı. `Files.lines` stream ile oku.

**d) `new File("x").exists()` sonra `new FileInputStream("x")` — race condition.** Dosya aradan kaybolabilir. Try-catch ile doğrudan aç, hata yakala.

**e) Relative path problemi.** Çalıştırma dizini ne olursa path farklı yorumlanır. Genelde `Path.of(...).toAbsolutePath()` veya classpath kaynakları kullan.

**f) `BufferedWriter.flush()` unutmak.** close() zaten flush yapar ama middle-of-flow data için manuel flush gerekebilir.

---

## Mülakat Soruları ve Örnek Cevaplar

**1. Byte stream ile character stream farkı?**
Byte stream 8 bit, binary data için (resim, zip, protokol). Character stream 16 bit UTF-16, text için. Character stream internally byte stream kullanıp encoding dönüşümü yapar. Metin dosyası için Reader/Writer, binary için InputStream/OutputStream.

**2. `BufferedReader` neden daha hızlı?**
Her `read()` sistem çağrısı pahalıdır. BufferedReader kendi buffer'ına (default 8192 byte) bir defada büyük blok okur, sonra yerel olarak tek tek verir. Disk I/O sayısı azalır, hız 10x-100x artabilir.

**3. `Files.readAllLines` ile `Files.lines` farkı?**
`readAllLines` tüm satırları belleğe yükler, `List<String>` döner — küçük dosyalar için rahat. `lines` `Stream<String>` döner, lazy ve memory-friendly — büyük dosyalar için kullan. `lines`'ın kullanımı try-with-resources gerektirir (stream dosyaya bağlı).

**4. try-with-resources nasıl çalışır?**
Parantez içinde tanımlanan `AutoCloseable`'ların `close()` metodu blok bittiğinde otomatik çağrılır — başarılı bitiş veya exception olsun. Birden fazla kaynak ters sırada kapanır (LIFO). Close sırasında hata olursa primary exception'a suppressed olarak eklenir.

**5. Serialization ve `serialVersionUID` ne işe yarar?**
Serialization nesneyi byte akışına çevirir. `serialVersionUID` sınıf versiyonunu belirten long sayıdır. Deserialize ederken byte akışındaki UID ile sınıfın UID'si uyuşmalı; değilse `InvalidClassException`. Ezmezsen derleyici üretir ama sınıf her değişikliğinde değişir — açıkça `private static final long serialVersionUID = 1L;` yazmak tercih edilir.

**6. `transient` ne yapar?**
Field'ı serialization dışında tutar. Şifre, cache, geçici durum gibi saklanmaması gereken field'lar için. Deserialize edildiğinde field default değerine (0/null/false) döner.

**7. Scanner mı BufferedReader mı?**
Parse etme ihtiyacın varsa (nextInt, nextDouble, regex) Scanner rahat. Hız kritik, büyük veri okuyorsan BufferedReader — 2-3 kat hızlıdır. Console girdisi için Scanner popüler, competitive programming'de BufferedReader.

**8. Encoding belirtmemek ne tür sorunlara yol açar?**
Platform farklılığı: Linux'ta UTF-8 çalışır, Windows'ta MS-1252 olabilir. Türkçe karakterler `?` olarak görünür, hash'ler değişir, diff testleri başarısız olur. Her zaman `StandardCharsets.UTF_8` belirt.

**9. Memory-mapped file ne zaman kullanılır?**
Çok büyük (GB+) dosyalarda rastgele erişim yapacaksan. OS disk sayfalamasını kullanır, gerçek I/O lazy yapılır. Database engine'ler, büyük log okuyucular bu tekniği kullanır. Küçük dosyalarda overhead yaratır, normal okuma yeter.

**10. Java serialization güvenlik riski?**
Güvensiz kaynaktan gelen serialized data deserialize etmek, "gadget chain" saldırılarına açık — saldırgan sınıf hiyerarşisindeki metod zincirlerini tetikleyerek remote code execution yapabilir. Equifax breach gibi büyük ihlallerin kaynağı. Modern yaklaşım JSON/Protobuf ile veri transferi, Java serialization sadece güvenli iç kullanımda.

**11. `Path.of` ile `Paths.get` farkı?**
Aynı işi yapar. `Paths.get` Java 7'den beri var, `Path.of` Java 11'de eklendi. Factory method modern pattern. Yeni kodda `Path.of` tercih et.

**12. `WatchService` ne için kullanılır?**
Dizin ve dosya değişikliklerini izlemek. Hot-reload (config değiştiğinde reload), build tool'lar (dosya değişince yeniden derle), sync uygulamalar. `take()` bloklayıcı, değişiklik gelene kadar bekler. OS-level watchlerde performans iyidir ama bazı network filesystem'larda güvenilir değildir.
