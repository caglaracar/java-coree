package p17_reflection;

import java.util.List;

/** Reflection örneklerinde hedef olarak kullanılan demo sınıfı. */
public class Person {

    @Info(label = "isim", required = true)
    private String name;

    @Info(label = "yas")
    private int age;

    public static final String SPECIES = "Homo sapiens";

    private List<String> hobbies;

    public Person() {
        this("anonim", 0);
    }

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName()       { return name; }
    public int    getAge()        { return age; }
    public void   setName(String n) { this.name = n; }
    public void   setAge(int a)     { this.age = a; }

    private String secret(String prefix) {
        return prefix + ":" + name + "(" + age + ")";
    }

    @Override
    public String toString() {
        return "Person{name='" + name + "', age=" + age + "}";
    }
}
