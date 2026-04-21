package p06_inheritance;

public class InheritanceMain {
    public static void main(String[] args) {

        Dog karabas = new Dog("Karabaş", 3, "Kangal");
        // Çıktı sırası:
        //   Animal ctor: Karabaş
        //   Dog    ctor: Karabaş
        // -> Üst sınıf ctor önce çalışır (constructor zinciri)

        karabas.eat();   // override edilmiş → önce super.eat, sonra köpek davranışı
        karabas.sleep(); // miras
        karabas.bark();  // kendi
        System.out.println(karabas);  // toString miras + getClass düzgün isim verir

        // --- UPCASTING ---
        Animal a = karabas;   // Dog -> Animal, otomatik ve güvenli
        a.eat();              // DİNAMİK DAĞITIM: Dog.eat() çalışır! (polymorphism)
        // a.bark();          // HATA: Animal'da yok

        // --- DOWNCASTING ---
        if (a instanceof Dog d) {     // pattern matching (Java 16+)
            d.bark();                  // güvenli
        }

        // Yanlış cast'a dikkat:
        Animal simple = new Animal("Bilinmeyen", 1);
        try {
            Dog fake = (Dog) simple;   // runtime hata
            fake.bark();
        } catch (ClassCastException ex) {
            System.out.println("ClassCastException yakalandı");
        }
    }
}
