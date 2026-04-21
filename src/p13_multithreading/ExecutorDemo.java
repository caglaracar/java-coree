package p13_multithreading;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * ExecutorService + Future + CompletableFuture kullanımı.
 * Modern Java'da "new Thread(...)" yerine bunları tercih et.
 */
public class ExecutorDemo {

    public static void main(String[] args) throws Exception {

        // --- 1) Fixed thread pool ---
        ExecutorService pool = Executors.newFixedThreadPool(3);

        // Callable -> değer döner, Future ile al
        List<Future<Integer>> futures = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            final int n = i;
            Future<Integer> f = pool.submit(() -> {
                Thread.sleep(200);
                return n * n;   // kareyi hesapla
            });
            futures.add(f);
        }

        int sum = 0;
        for (Future<Integer> f : futures) {
            sum += f.get();   // bloklanır, sonuç beklenir
        }
        System.out.println("kareler toplamı = " + sum);

        pool.shutdown();                         // yeni task kabul etme
        pool.awaitTermination(2, TimeUnit.SECONDS);

        // --- 2) Scheduled tasks ---
        ScheduledExecutorService sched = Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> tick = sched.scheduleAtFixedRate(
                () -> System.out.println("tick @ " + System.currentTimeMillis()),
                0, 300, TimeUnit.MILLISECONDS
        );
        Thread.sleep(1000);
        tick.cancel(false);
        sched.shutdown();

        // --- 3) CompletableFuture (asenkron compose) ---
        CompletableFuture<String> cf =
                CompletableFuture.supplyAsync(() -> "merhaba")
                                 .thenApply(s -> s + " dünya")
                                 .thenApply(String::toUpperCase);
        System.out.println("CF sonucu: " + cf.get());

        // Birden fazla işi paralel birleştir
        CompletableFuture<Integer> a = CompletableFuture.supplyAsync(() -> slow(2));
        CompletableFuture<Integer> b = CompletableFuture.supplyAsync(() -> slow(3));
        CompletableFuture<Integer> both = a.thenCombine(b, Integer::sum);
        System.out.println("paralel toplam: " + both.get());

        // Hata yönetimi
        CompletableFuture<String> fail = CompletableFuture
                .<String>supplyAsync(() -> { throw new RuntimeException("boom"); })
                .exceptionally(ex -> "fallback: " + ex.getMessage());
        System.out.println(fail.get());
    }

    static int slow(int x) {
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        return x;
    }
}
