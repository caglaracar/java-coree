package p05_oop_classes;

public class Main {
    public static void main(String[] args) {

        Car a = new Car();                    // default ctor
        Car b = new Car("Audi");              // 1 arg ctor
        Car c = new Car("BMW", "Red");        // 2 arg ctor

        // Zincirleme (method chaining) — her metot 'this' döner
        c.accelerate(50).accelerate(30).brake(20).status();

        a.status();
        b.status();

        System.out.println("Toplam üretilen Car sayısı: " + Car.getProductionCount());

        // --- record örneği ---
        Point p = new Point(3, 4);
        System.out.println("x=" + p.x() + " y=" + p.y());
        System.out.println(p); // otomatik toString

        Point p2 = new Point(3, 4);
        System.out.println("equals: " + p.equals(p2));   // true (record otomatik)
    }

    // Record: küçük, immutable veri sınıfı
    public record Point(int x, int y) {}
}
