# 10 — Exception Handling (Hata Yönetimi)

Hatalar, kodu yazarken değil **kullanıcı yazılımı çalıştırırken** ortaya çıkar. Dosya bulunamaz, ağ koparı, kullanıcı yanlış veri girer, bellek yetmez... Java, bu beklenmedik durumları dilin içinde **birinci sınıf vatandaş** haline getirmiş: **exception** mekanizması. Doğru kullanıldığında kodu okunaklı ve dayanıklı yapar; yanlış kullanıldığında sebep bulunamayan sessiz hatalar ve karmakarışık stack trace'ler üretir. Bu paketi iyi sindir — hata yönetimi yazılımın %80'i olabilir.

---

## 1) Exception Nedir, Neden Var?

**Exception** (istisna), programın **normal akışını bozacak** beklenmedik bir durumdur. İşlem yarıda kalır, hata yukarıya doğru fırlatılır, biri onu yakalayana kadar çağrı yığınını tırmanır.

Alternatif yaklaşımlar ve neden Java onları seçmedi:

**a) Return code (C tarzı).**
```c
int result = openFile(&file);
if (result != 0) { /* hata */ }
```
Her çağrıdan sonra if kontrolü gerekiyor. Kontrol unutulursa hata sessizce yitiyor. Hata kodunun anlamı hatalıktan hatalığa farklı.

**b) Global errno.**
Hatayı global bir değişkene koy. Thread-safe değil, anlamsal karmaşa.

**c) Exception'lar.**
Hata fırlatılır, normal akış yarıda kalır, uygun yerde yakalanır. Kontrol unutulamaz (checked exception'larda derleyici zorlar). Hata bilgisi kendi nesnesinde — tip, mesaj, stack trace, cause.

Java tasarımcıları exception'ları **dilin içine işlemişler**: `try`, `catch`, `finally`, `throw`, `throws` anahtar kelimeleri, ve `java.lang.Throwable` hiyerarşisi.

---

## 2) Throwable Hiyerarşisi

Java'da fırlatılabilir her şey `Throwable`'ın torunudur:

```
Throwable
├── Error                    ← sistem seviyesi, YAKALAMA genelde
│   ├── OutOfMemoryError
│   ├── StackOverflowError
│   └── VirtualMachineError
└── Exception
    ├── IOException          ← checked
    ├── SQLException         ← checked
    ├── ClassNotFoundException ← checked
    │   ...
    └── RuntimeException     ← unchecked
        ├── NullPointerException
        ├── ArithmeticException
        ├── IllegalArgumentException
        ├── IllegalStateException
        ├── IndexOutOfBoundsException
        ├── ClassCastException
        ├── ConcurrentModificationException
        │   ...
```

Üç ana kamp:

**`Error`**: Virtual machine seviyesinde, programın toparlanamayacağı şeyler. `OutOfMemoryError`, `StackOverflowError` — yakalamaya çalışmak genelde yanlıştır. Loglayıp çökmek daha güvenli.

**`Exception` (non-runtime)** = **Checked exceptions**: Derleyici zorlar, ya yakalarsın ya imzaya `throws` ile ekleyip üst çağırana devredersin. Sebep: "Programcı bu durumla **baş etmeye düşünmelidir**."

**`RuntimeException`** = **Unchecked exceptions**: Derleyici zorlamaz. Genelde **programlama hatalarıdır** (null deref, yanlış index, yanlış argüman). Teoride her yerde olabilirler.

---

## 3) Checked vs Unchecked — Kavga Konusu

| | Checked | Unchecked (Runtime) |
|---|---|---|
| Derleyici zorlar mı? | **Evet** — yakala veya `throws` ile bildir | Hayır |
| Nerede kullanılır? | Kurtarılabilir dış dünya hataları (I/O, ağ, DB) | Programlama hataları (null, bad argument) |
| Örnek | `IOException`, `SQLException` | `NullPointerException`, `IllegalArgumentException` |
| Felsefe | Programcı baş etmek zorunda | Programcı hatasını düzeltmeli |

### Checked Exception Örneği
```java
public void readFile() throws IOException {     // "throws" ile ilan ettim
    BufferedReader br = new BufferedReader(new FileReader("f.txt"));
    ...
}
```
Çağıran ya try-catch ile yakalayacak, ya kendi imzasına `throws IOException` koyacak.

### Modern Tartışma: Checked Exceptions İyi mi?
Java'nın tasarım kararlarından en çok eleştirileni. Lehte argümanlar: dokümante edilmiş hata sözleşmesi, zorlayıcı. Aleyhte argümanlar: boilerplate, genelde yanlış yere "yakala ve yut" yapar, lambda'larla uyumsuz. Kotlin, C#, Scala gibi modern diller checked exception'ı atmış.

Pratik tavsiye: Yeni exception tanımlayacaksan **runtime** (unchecked) yap, özellikle iş kuralı hataları için. Checked'ı sadece gerçekten "çağıran baş etmek zorunda" durumlarda kullan.

---

## 4) try / catch / finally — Temel Yapı

```java
try {
    // riskli işlem
    risky();
} catch (SpecificException e) {
    // spesifik hata
    log(e);
} catch (GeneralException e) {
    // daha genel
    log(e);
} finally {
    // her durumda çalışır
    cleanup();
}
```

### catch Sırası
Java alt tipleri önce sıralamanı **zorunlu kılar**:
```java
try { ... }
catch (Exception e) { ... }          // GENEL
catch (IOException e) { ... }        // HATA — zaten yukarıda Exception yakalanırdı
```
Derleyici bu hatayı yakalar çünkü daha spesifik `IOException` hiçbir zaman çalışmaz.

### Multi-catch (Java 7+)
Aynı işi yapacağın birden fazla tipi `|` ile birleştirebilirsin:
```java
try { ... }
catch (IOException | SQLException e) {
    log(e);
}
```
`e` burada effectively final — atama yapamazsın.

### `finally` — Her Durumda Çalışır
`try` veya `catch`'te ne olursa olsun `finally` çalışır. Exception yakalansa da yakalanmasa da, hatta `return` olsa bile:
```java
String test() {
    try { return "try"; }
    finally { System.out.println("finally çalıştı"); }
}
// Çıktı: "finally çalıştı" yazılır, sonra "try" döner
```

**`finally` istisnaları (çalışmadığı durumlar):**
- `System.exit(0)` çağrılırsa — JVM anında kapanır.
- Thread crash olursa (Error'da).
- İşletim sistemi process'i öldürürse.

### `finally` ile `return` Karışımı — Anti-Pattern
```java
int weird() {
    try { return 1; }
    finally { return 2; }   // ASLA YAZMA — try'daki return'ü ezer, 2 döner
}
```
`finally` içinde return yazmak rahatsız edici sessiz davranışlara yol açar. Yapma.

---

## 5) try-with-resources — Otomatik Kaynak Kapatma (Java 7+)

Eski Java'da stream/connection kapatmak tipik şöyleydi:
```java
BufferedReader br = null;
try {
    br = new BufferedReader(new FileReader("f.txt"));
    ...
} catch (IOException e) { ... }
finally {
    if (br != null) {
        try { br.close(); }
        catch (IOException e) { /* bastır */ }
    }
}
```
Sekiz satır boilerplate, üstüne close sırasında hata yakalanırsa orijinal exception kayboluyor.

Java 7 ile **try-with-resources** geldi:
```java
try (BufferedReader br = new BufferedReader(new FileReader("f.txt"))) {
    ...
} catch (IOException e) { ... }
```

Parantez içindeki kaynak `AutoCloseable` implement ediyorsa, try bloğu biterken **otomatik close()** çağrılır — başarı olsa da olmasa da. Daha temiz, daha güvenli.

Birden fazla kaynak `;` ile ayrılır:
```java
try (FileReader fr = new FileReader(src);
     FileWriter fw = new FileWriter(dst)) {
    ...
}
```
Kapanış sırası **ters**tir: fw önce, fr sonra (LIFO).

### Suppressed Exceptions
Hem `try` hem `close()` hata fırlatırsa, **primary exception** `try`'dakidir; close'un attığı **suppressed** olarak eklenir. `.getSuppressed()` ile erişilir.

---

## 6) throw ve throws — Benzer İsim, Farklı İş

İki kelime de "exception atmak"la alakalı ama tamamen farklı roller oynar:

**`throw`** — exception fırlatma **eylemi**:
```java
if (age < 0) {
    throw new IllegalArgumentException("age negatif olamaz");
}
```

**`throws`** — metot imzasında, fırlatabileceği **checked** exception'ları ilan:
```java
public void read() throws IOException, SQLException {
    ...
}
```

Karıştırma! `throw new ...` tek seferlik eylem, `throws Xyz` imza deklarasyonu. Aynı metotta ikisini de görmek yaygın:
```java
public void processFile(String path) throws IOException {
    if (path == null) throw new IllegalArgumentException();
    // ...
}
```

---

## 7) Custom Exception — Kendi Hatalarını Yaz

İş domain'inize özel hatalar için custom exception tanımlayın. Seçim: checked mi unchecked mi?

### Unchecked Custom Exception
```java
public class InsufficientBalanceException extends RuntimeException {
    private final double attemptedAmount;
    
    public InsufficientBalanceException(String msg, double amount) {
        super(msg);
        this.attemptedAmount = amount;
    }
    public double getAttemptedAmount() { return attemptedAmount; }
}
```

### Checked Custom Exception
```java
public class ConfigLoadException extends Exception {
    public ConfigLoadException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
```

### Ne Zaman Hangisi?
- İş kuralı ihlali, validation hataları → **unchecked**.
- Dış dünya hatası (dosya, ağ, DB) ve çağıranın mutlaka baş etmesi lazımsa → **checked**.
- Şüphelendiğinde **unchecked**'a kaç. Modern Java eğilimi bu yönde.

---

## 8) Exception Chaining — Nedeni (Cause) Sakla

Bazen düşük seviye bir exception yakalayıp daha üst seviye bir exception fırlatmak istersin. Ama orijinal hatayı kaybetmek istemezsin — debug için gerekir. **Cause** mekanizması bunun içindir:

```java
try {
    file.open();
} catch (IOException e) {
    throw new ServiceException("Yapılandırma okunamadı", e);   // e = cause
}
```

`e` parametresi olarak yeni exception'a geçer. Stack trace'te:
```
ServiceException: Yapılandırma okunamadı
    at ...
Caused by: IOException: config.yml not found
    at ...
```

Orijinal hata kaybolmaz, zincir oluşur. Her katman kendi domain'ine özel exception atar ama altta gerçek sebep görülür. Enterprise uygulamalarının klasik örüntüsüdür.

---

## 9) Best Practices — İyi Hata Yönetimi

**a) Yutma (exception swallowing) büyük günahtır**:
```java
// Çok kötü
try { risky(); }
catch (Exception e) { }   // HİÇBİR ŞEY YAPMADI
```
Hata bildirildi, kaybettin. En azından logla.

**b) `catch (Exception e)` fetişizmi**:
Çok geniş yakalar, gerçekten beklediğin hatayı da kapsar. Mümkün olduğunca spesifik yakala.

**c) Stack trace'i asla kaybetme**:
```java
catch (IOException e) {
    throw new ServiceException("boom");   // cause YOK — e kaybolur
}
// Doğrusu:
throw new ServiceException("boom", e);    // cause korunur
```

**d) `printStackTrace` production'da yasak**:
Sistem.err'e yazdırmak loglama değildir. `logger.error("mesaj", e)` kullan (SLF4J, Log4j).

**e) Exception'ı kontrol akışı için kullanma**:
```java
// Kötü: exception normal kontrol akışında
try {
    int i = Integer.parseInt(str);
    // kullan
} catch (NumberFormatException e) {
    // boş default
}
```
Exception pahalı bir mekanizma (stack trace üretmek maliyetli). Normal akış için if/else yeterliyse onu kullan.

**f) Kaynak sızıntısını önle**: Her `open` için eşleşen `close` olmalı. Try-with-resources mümkün olduğunca kullan.

**g) Anlamlı mesaj ver**:
```java
throw new IllegalArgumentException("age must be >= 0, got: " + age);
```
Log'a düştüğünde "age must be >= 0" diye görmek, "age invalid" görmekten bin kat değerli.

**h) Spesifik yakala, genel fırlat — DEĞİL TAM TERSİ**:
```java
// Doğru
try { ... }
catch (FileNotFoundException e) { /* özel davran */ }
catch (IOException e) { /* genel IO */ }
```
Spesifik'ten genele doğru yakala.

---

## 10) Exception Performansı

Exception atmak ve yakalamak **ucuz değildir**. Stack trace üretmek (stack'i tüm frame'leriyle dolaşıp dump almak) pahalı işlemdir.

Ölçümler (yaklaşık):
- Normal return: ~1ns
- Exception atma: ~1-10μs (1000-10000 kat yavaş)

Yani exception atmak nadir durumlar için. "Hata yoksa fırlatılmaz" prensibine sadık kal. Performans kritik yollarda exception'a güvenme; validasyon yap.

---

## 11) Java 7+: Try-With-Resources Detayları

Kaynak hem `AutoCloseable` (Java 7) hem `Closeable` (Java 5) interface'ini implement edebilir.

`AutoCloseable` daha geneldir:
```java
public interface AutoCloseable {
    void close() throws Exception;
}
```

`Closeable` özel olarak IO kaynakları için:
```java
public interface Closeable extends AutoCloseable {
    void close() throws IOException;   // daha dar exception
}
```

Kendi kaynak sınıfın için `AutoCloseable` implement et:
```java
public class MyResource implements AutoCloseable {
    public void use() { ... }
    @Override public void close() {
        // temizlik
    }
}

try (MyResource r = new MyResource()) {
    r.use();
}
```

---

## 12) Java 9+: Effective Final ile Resource
Java 9'dan itibaren try-with-resources bloğunun **dışında** tanımlanmış effectively final bir değişkeni de kullanabilirsin:
```java
MyResource r = new MyResource();
try (r) {
    r.use();
}
```

---

## Mülakat Soruları ve Örnek Cevaplar

**1. Checked ve unchecked exception farkı?**
Checked exception derleyici tarafından kontrol edilir — ya yakalarsın ya `throws` ile ilan edersin (IOException, SQLException). Unchecked (RuntimeException) derleyici zorlamaz, genelde programlama hatalarıdır (NullPointer, IllegalArgument). Modern eğilim unchecked yönündedir çünkü checked boilerplate ve lambda uyumsuzluğu yaratır.

**2. `final`, `finally`, `finalize` arasındaki fark?**
`final` anahtar sözcük — değişken/metod/sınıfı sabitler. `finally` try-catch bloğunun her durumda çalışan kısmı. `finalize` Object'in GC öncesi çağrılan metodu — **deprecated**, kullanma.

**3. try-with-resources nasıl çalışır?**
`AutoCloseable` implement eden kaynaklar parantez içinde tanımlanır. Try bloğu biterken (normal veya exception'la) kaynakların `close()` metodu **otomatik** çağrılır. Birden fazla kaynak ters sırada kapanır. Close sırasında hata olursa orijinal exception'a suppressed olarak eklenir.

**4. Multiple catch sıralaması nasıl olmalı?**
Spesifikten genele. Önce alt tipler, sonra üst tipler. Aksi halde üstteki genel catch alt tipi yakalar ve alt catch ölü kod olur — derleyici hata verir.

**5. Kendi exception'ınızı ne zaman yazarsın?**
Domain'e özel anlam taşıyan hatalar için (`InsufficientBalanceException`, `OrderNotFoundException`). Modern pratik unchecked (RuntimeException) türeterek yapmaktır. Checked sadece çağıranın mutlaka baş etmesi gerektiğinde.

**6. `throw` ile `throws` farkı?**
`throw` bir eylem — "şu anda bu exception'ı fırlat". `throws` metot imzasında bildirim — "bu metod şu checked exception'ları fırlatabilir, dikkat et". Aynı metotta ikisi birlikte yaygın.

**7. `finally` her zaman çalışır mı?**
Neredeyse her zaman — normal bitiş, exception yakalanma, return. Çalışmadığı durumlar: `System.exit(n)`, JVM crash, Error (bazı durumlar), OS'un process'i öldürmesi.

**8. Exception chaining ne için kullanılır?**
Alt katmanda yakalanan exception'ı üst katman kendi domain'ine özel exception'a sararak fırlatır, orijinali cause olarak taşır. Bu sayede her katman kendi soyutlamasında konuşur ama debugging için gerçek sebep kaybolmaz. `new MyException("msg", originalException)`.

**9. Exception yutmak (`catch { }`) neden tehlikelidir?**
Hata bildirildi ama kimse bilmiyor. Program yarım veriyle devam eder, bulunması çok zor bug'lar çıkar. En azından `logger.error(..., e)` yap.

**10. Exception performans açısından pahalı mıdır?**
Evet, özellikle stack trace üretimi. Exception atmak normal return'den 1000-10000 kat yavaş olabilir. Bu yüzden normal kontrol akışı için exception kullanma — if/else veya Optional gibi alternatifler tercih edilmeli.

**11. Runtime exception'ı `throws` ile ilan etmek zorunlu mu?**
Hayır. Sadece checked exception'lar için zorunlu. Runtime exception atan metot imzasına yazman opsiyoneldir ama bazen dokümantasyon amacıyla yazılır.

**12. Error'ları neden yakalamamalıyız?**
Error'lar genelde JVM-seviyesi, kurtarılamaz durumlardır (OutOfMemoryError, StackOverflowError). Yakalamak durumu düzeltmez, programın çökmüş gibi devam etmesine yol açar. Loglayıp çökmek daha güvenlidir.
