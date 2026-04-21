package p13_multithreading;

public class ThreadBasics {

    public static void main(String[] args) throws InterruptedException {

        // --- 1) Runnable + Thread ---
        Runnable r = () -> {
            for (int i = 1; i <= 3; i++) {
                System.out.println(Thread.currentThread().getName() + " i=" + i);
                try { Thread.sleep(100); } catch (InterruptedException e) { return; }
            }
        };

        Thread t1 = new Thread(r, "thread-1");
        Thread t2 = new Thread(r, "thread-2");

        t1.start();   // start() çağır; run() çağırma!
        t2.start();

        // --- 2) join: t1 ve t2 bitene kadar bekle ---
        t1.join();
        t2.join();
        System.out.println("main: tüm thread'ler bitti");

        // --- 3) Daemon thread: arka plan; tüm non-daemon'lar bitince JVM kapanır ---
        Thread daemon = new Thread(() -> {
            while (true) {
                System.out.println("daemon tick");
                try { Thread.sleep(200); } catch (InterruptedException e) { return; }
            }
        });
        daemon.setDaemon(true);
        daemon.start();

        // --- 4) interrupt ile durdurma ---
        Thread worker = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("çalışıyorum...");
                try { Thread.sleep(150); }
                catch (InterruptedException e) {
                    System.out.println("interrupt yakalandı, çıkıyorum");
                    Thread.currentThread().interrupt(); // bayrağı yeniden set et
                    return;
                }
            }
        }, "worker");
        worker.start();
        Thread.sleep(500);
        worker.interrupt();
        worker.join();

        System.out.println("main bitti");
    }
}
