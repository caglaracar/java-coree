package p04_memory;

/**
 * static field/method davranışı.
 */
public class StaticDemo {

    static int totalCount = 0;   // tüm nesneler paylaşır
    int id;                      // nesneye özel

    public StaticDemo() {
        totalCount++;
        this.id = totalCount;
    }

    // Static blok: sınıf ilk yüklendiğinde 1 kere çalışır.
    static {
        System.out.println(">> StaticDemo sınıfı yüklendi");
        totalCount = 0;
    }

    // Instance initializer blok: her new'de, constructor'dan ÖNCE çalışır.
    {
        System.out.println(">> instance init blok");
    }

    static void staticMethod() {
        System.out.println("static metod — nesne gerekmez");
        // this.id  // HATA: static içinde 'this' yok
    }

    void instanceMethod() {
        System.out.println("instance metod id=" + id);
    }

    public static void main(String[] args) {
        staticMethod();                   // nesne üretmeden çağrı

        StaticDemo d1 = new StaticDemo();
        StaticDemo d2 = new StaticDemo();
        StaticDemo d3 = new StaticDemo();

        System.out.println("total = " + StaticDemo.totalCount); // 3
        System.out.println("d1.id = " + d1.id + " | d2.id = " + d2.id + " | d3.id = " + d3.id);

        d1.instanceMethod();
    }
}
