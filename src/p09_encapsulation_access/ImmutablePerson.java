package p09_encapsulation_access;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable sınıf örneği.
 * - final sınıf
 * - final field'lar
 * - setter YOK
 * - mutable tipler defansif kopyalanır
 */
public final class ImmutablePerson {

    private final String name;
    private final int age;
    private final List<String> hobbies;

    public ImmutablePerson(String name, int age, List<String> hobbies) {
        this.name = name;
        this.age = age;
        // Dışarıdaki liste sonradan değiştirilse bile bizim kopyamız korunur.
        this.hobbies = List.copyOf(hobbies);
    }

    public String getName() { return name; }
    public int getAge()     { return age; }
    public List<String> getHobbies() { return hobbies; /* unmodifiable */ }

    // Değiştirilmiş YENİ bir nesne üret (String.substring gibi)
    public ImmutablePerson withAge(int newAge) {
        return new ImmutablePerson(this.name, newAge, this.hobbies);
    }

    @Override public String toString() {
        return "ImmutablePerson{" + name + ", " + age + ", " + hobbies + "}";
    }

    public static void main(String[] args) {
        List<String> h = new ArrayList<>();
        h.add("Kitap");
        h.add("Koşu");

        ImmutablePerson p = new ImmutablePerson("Ayşe", 30, h);

        h.add("Hacker ekledi");       // dışarıdaki listeyi değiştir
        System.out.println(p);        // p'nin hobbies'i ETKİLENMEZ

        try {
            p.getHobbies().add("x");  // unmodifiable -> hata
        } catch (UnsupportedOperationException e) {
            System.out.println("unmodifiable list: değiştirilemez");
        }

        ImmutablePerson older = p.withAge(31);
        System.out.println("yeni nesne: " + older);
        System.out.println("eski nesne: " + p);
    }
}
