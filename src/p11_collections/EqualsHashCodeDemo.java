package p11_collections;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * equals/hashCode sözleşmesi neden önemli?
 * Kendi sınıfınla HashSet/HashMap kullanacaksan ikisini de override ET.
 */
public class EqualsHashCodeDemo {

    // --- KÖTÜ ÖRNEK: sadece equals override edildi ---
    static class BadUser {
        String name; int id;
        BadUser(String n, int i) { name = n; id = i; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof BadUser b)) return false;
            return id == b.id && Objects.equals(name, b.name);
        }
        // hashCode() override EDİLMEDİ!
    }

    // --- DOĞRU ÖRNEK ---
    static class User {
        String name; int id;
        User(String n, int i) { name = n; id = i; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof User u)) return false;
            return id == u.id && Objects.equals(name, u.name);
        }
        @Override public int hashCode() {
            return Objects.hash(name, id);  // IDE'nin otomatik üreteceği
        }
    }

    public static void main(String[] args) {

        // --- Bozuk kontrat: aynı kullanıcı iki kere ---
        Set<BadUser> bad = new HashSet<>();
        bad.add(new BadUser("Ali", 1));
        System.out.println("bad contains? " + bad.contains(new BadUser("Ali", 1)));
        // Muhtemelen FALSE — farklı hashCode'lar farklı bucket'lar -> contains bulamaz
        System.out.println("bad size: " + bad.size());

        // --- Doğru ---
        Set<User> good = new HashSet<>();
        good.add(new User("Ali", 1));
        good.add(new User("Ali", 1));  // duplicate sayılır, eklenmez
        System.out.println("good contains? " + good.contains(new User("Ali", 1))); // true
        System.out.println("good size: " + good.size());  // 1
    }
}
