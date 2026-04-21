package p10_exceptions;

import java.io.IOException;

public class ExceptionDemo {

    // --- Custom exception (unchecked) ---
    static class InsufficientBalanceException extends RuntimeException {
        public InsufficientBalanceException(String msg) { super(msg); }
    }

    // --- Custom checked exception ---
    static class ConfigException extends Exception {
        public ConfigException(String msg, Throwable cause) { super(msg, cause); }
    }

    // --- throws ilan eden metot ---
    static void loadConfig() throws ConfigException {
        try {
            throw new IOException("config.yml bulunamadı");
        } catch (IOException e) {
            // chaining: orijinal hatayı cause olarak sar
            throw new ConfigException("Konfigürasyon yüklenemedi", e);
        }
    }

    static double divide(int a, int b) {
        if (b == 0) throw new IllegalArgumentException("b sıfır olamaz");
        return (double) a / b;
    }

    static String riskyReturn() {
        try {
            return "try";
        } finally {
            // Her durumda çalışır. Burada return YAZMAYIN (try return'ü ezer).
            System.out.println("finally çalıştı");
        }
    }

    public static void main(String[] args) {

        // --- try-catch-finally ---
        try {
            int[] arr = {1, 2, 3};
            System.out.println(arr[5]);   // ArrayIndexOutOfBoundsException
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("yakalandı: " + e.getMessage());
        } finally {
            System.out.println("finally her halükârda");
        }

        // --- multi-catch ---
        try {
            if (Math.random() > 2) throw new IOException("io");
            else throw new ArithmeticException("math");
        } catch (IOException | ArithmeticException e) {
            System.out.println("multi-catch: " + e.getClass().getSimpleName());
        }

        // --- custom exception + chaining ---
        try {
            loadConfig();
        } catch (ConfigException e) {
            System.out.println("üst: " + e.getMessage());
            System.out.println("cause: " + e.getCause());
        }

        // --- throw ile argüman doğrulama ---
        try {
            divide(10, 0);
        } catch (IllegalArgumentException e) {
            System.out.println("div hata: " + e.getMessage());
        }

        // --- finally davranışı ---
        System.out.println("riskyReturn -> " + riskyReturn());

        // --- try-with-resources ---
        try (MyResource r1 = new MyResource("R1");
             MyResource r2 = new MyResource("R2")) {
            r1.use();
            r2.use();
        } // kapanış TERS sırada: R2.close(), R1.close()
        // Suppressed exception'ları görmek için close sırasında hata atın.
    }

    // AutoCloseable implement eden sınıf -> try-with-resources ile otomatik kapanır
    static class MyResource implements AutoCloseable {
        String name;
        MyResource(String n) { this.name = n; System.out.println(n + " açıldı"); }
        void use() { System.out.println(name + " kullanılıyor"); }
        @Override public void close() { System.out.println(name + " kapandı"); }
    }
}
