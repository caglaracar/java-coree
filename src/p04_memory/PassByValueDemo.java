package p04_memory;

/**
 * Java'nın pass-by-value davranışını ispatlar.
 *
 * Kural: Metoda geçenler HER ZAMAN kopyadır.
 *  - Primitive -> değerin kopyası
 *  - Nesne    -> REFERANSIN kopyası (aynı heap nesnesini gösterir)
 */
public class PassByValueDemo {

    static class Person {
        String name;
        Person(String name) { this.name = name; }
        @Override public String toString() { return "Person(" + name + ")"; }
    }

    // --- Primitive ---
    static void inc(int x) {
        x = x + 1;   // sadece lokal kopyayı arttırır
    }

    // --- Nesnenin İÇİNİ değiştirmek ---
    static void rename(Person p) {
        p.name = "Yeni";   // dışarıdan GÖRÜNÜR
    }

    // --- Nesnenin REFERANSINI değiştirmek ---
    static void reassign(Person p) {
        p = new Person("Başka"); // SADECE lokal kopya yeni nesneye bağlanır
    }

    public static void main(String[] args) {
        int a = 10;
        inc(a);
        System.out.println("a = " + a); // 10  (kopya değişti, orijinal değişmedi)

        Person p = new Person("Ali");
        rename(p);
        System.out.println(p); // Person(Yeni)  (içi değişti)

        reassign(p);
        System.out.println(p); // Person(Yeni)  (referans değişmedi!)
    }
}
