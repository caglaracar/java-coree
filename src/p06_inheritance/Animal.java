package p06_inheritance;

public class Animal {
    protected String name;
    protected int age;

    public Animal(String name, int age) {
        this.name = name;
        this.age = age;
        System.out.println("Animal ctor: " + name);
    }

    public void eat() {
        System.out.println(name + " yemek yiyor");
    }

    public void sleep() {
        System.out.println(name + " uyuyor");
    }

    // final metod: override edilemez
    public final String species() { return "Animal"; }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + name + ", " + age + ")";
    }
}
