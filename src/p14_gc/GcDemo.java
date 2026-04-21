package p14_gc;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * Reference tipleri ve GC davranışı.
 * System.gc() sadece bir RİCA'dır, çalışma garantisi yoktur;
 * ama demo için çoğu JVM onu işler.
 */
public class GcDemo {

    static class Heavy {
        byte[] payload = new byte[1_000_000]; // 1 MB
        final String label;
        Heavy(String label) { this.label = label; }
        @Override public String toString() { return "Heavy(" + label + ")"; }
    }

    public static void main(String[] args) throws InterruptedException {

        // --- STRONG REFERENCE ---
        Heavy strong = new Heavy("strong");
        System.gc();                 // toplanmaz, strong yaşıyor
        System.out.println("strong yaşıyor mu? " + (strong != null));

        // --- WEAK REFERENCE ---
        WeakReference<Heavy> weak = new WeakReference<>(new Heavy("weak"));
        System.out.println("weak (GC öncesi): " + weak.get());
        System.gc(); Thread.sleep(100);
        System.out.println("weak (GC sonrası): " + weak.get()); // büyük ihtimal null

        // --- SOFT REFERENCE ---
        // Memory baskısı gelmedikçe korunur; cache için idealdir.
        SoftReference<Heavy> soft = new SoftReference<>(new Heavy("soft"));
        System.out.println("soft: " + soft.get());

        // --- PHANTOM REFERENCE ---
        // get() HER ZAMAN null döner; nesne tamamen yok olduğunda queue'ya düşer.
        ReferenceQueue<Heavy> q = new ReferenceQueue<>();
        PhantomReference<Heavy> phantom = new PhantomReference<>(new Heavy("phantom"), q);
        System.gc(); Thread.sleep(100);
        System.out.println("phantom.get (her zaman null): " + phantom.get());
        System.out.println("phantom queue'da mı? " + (q.poll() != null));

        // --- WEAKHASHMAP ---
        // Key için güçlü referans kalmadığında entry otomatik silinir.
        WeakHashMap<Object, String> cache = new WeakHashMap<>();
        Object key = new Object();
        cache.put(key, "değer");
        System.out.println("whm before: " + cache.size());

        key = null;                  // strong ref yok
        System.gc(); Thread.sleep(100);
        // whm implementasyonuna göre entry temizlenir
        System.out.println("whm after:  " + cache.size());

        // --- HEAP KULLANIMI ---
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        long max  = rt.maxMemory() / (1024 * 1024);
        System.out.printf("Heap kullanımı ~ %d MB / max %d MB%n", used, max);
    }
}
