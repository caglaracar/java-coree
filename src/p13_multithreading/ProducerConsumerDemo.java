package p13_multithreading;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Klasik Producer-Consumer örüntüsü.
 * BlockingQueue kullanırsak wait/notify'a ihtiyaç kalmaz.
 */
public class ProducerConsumerDemo {

    static final int POISON = -1;  // consumer'a "bitti" sinyali

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);

        // Producer
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    queue.put(i);   // dolu ise bekler
                    System.out.println("üretti: " + i);
                    Thread.sleep(50);
                }
                queue.put(POISON);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "producer");

        // Consumer
        Thread consumer = new Thread(() -> {
            try {
                while (true) {
                    int v = queue.take();  // boş ise bekler
                    if (v == POISON) { System.out.println("consumer çıkıyor"); return; }
                    System.out.println("           tüketti: " + v);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "consumer");

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();
        System.out.println("tamam");
    }
}
