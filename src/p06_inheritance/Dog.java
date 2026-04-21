package p06_inheritance;

public class Dog extends Animal {

    private String breed;

    public Dog(String name, int age, String breed) {
        super(name, age);         // ÜST sınıf ctor'u — İLK satır olmalı
        this.breed = breed;
        System.out.println("Dog ctor: " + name);
    }

    // Override: aynı imza, davranış farklı
    @Override
    public void eat() {
        super.eat();  // önce üst davranışı çağır
        System.out.println(name + " (köpek) kemik kemiriyor");
    }

    public void bark() {
        System.out.println(name + ": Hav hav!");
    }

    public String getBreed() { return breed; }
}
