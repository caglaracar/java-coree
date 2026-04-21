package p06_inheritance_2.p06_inheritance_payment.p06_inheritance;

public class Dog extends Animal {

    private String breed;

    public Dog() {
        System.out.println("Dog constructor called");
    }

    public Dog(String breed) {
        this.breed = breed;
    }

    public Dog(String breed, String name) {
        this.name = name;
        this.breed = breed;
        System.out.println(name + breed);
    }


}
