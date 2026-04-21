package p13_multithreading;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Race condition + synchronized + AtomicInteger karşılaştırması.
 */
public class SyncAndAtomic {

    static int unsafe = 0;        // kritik bölge korumasız
    static int guarded = 0;       // synchronized ile korunur
    static AtomicInteger atomic = new AtomicInteger(0);

    static synchronized void incGuarded() {
        guarded++;
    }

    public static void main(String[] args) throws InterruptedException {
        final int THREADS = 10;
        final int ITER    = 100_000;

        Thread[] ts = new Thread[THREADS];

        for (int i = 0; i < THREADS; i++) {
            ts[i] = new Thread(() -> {
                for (int j = 0; j < ITER; j++) {
                    unsafe++;             // RACE CONDITION!
                    incGuarded();         // synchronized — doğru ama yavaş
                    atomic.incrementAndGet(); // CAS tabanlı — hızlı ve doğru
                }
            });
            ts[i].start();
        }

        for (Thread t : ts) t.join();

        int expected = THREADS * ITER;
        System.out.println("expected = " + expected);
        System.out.println("unsafe   = " + unsafe    + "   (muhtemelen yanlış)");
        System.out.println("guarded  = " + guarded   + "   (doğru)");
        System.out.println("atomic   = " + atomic.get() + " (doğru, hızlı)");
    }
}
