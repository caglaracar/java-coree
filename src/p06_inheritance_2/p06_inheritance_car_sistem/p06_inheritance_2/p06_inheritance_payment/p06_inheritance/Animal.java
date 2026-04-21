package p06_inheritance_2.p06_inheritance_car_sistem.p06_inheritance_2.p06_inheritance_payment.p06_inheritance;

public class Animal {
    protected String name;
    protected int age;

    public Animal() {
        System.out.println("Animal constructor called.");
    }

    public Animal(String name, int age) {
        this.name = name;
        this.age = age;
        System.out.println("Animal ctor: " + name);
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + name + ", " + age + ")";
    }
}
