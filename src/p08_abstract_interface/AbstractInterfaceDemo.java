package p08_abstract_interface;

public class AbstractInterfaceDemo {

    // --- ABSTRACT CLASS ---
    static abstract class Shape {
        protected String color;
        Shape(String color) { this.color = color; }   // ctor var
        abstract double area();                        // alt sınıf zorunlu
        void describe() {                              // somut metot
            System.out.printf("%s, alan=%.2f%n", color, area());
        }
    }

    static class Circle extends Shape {
        double r;
        Circle(String c, double r) { super(c); this.r = r; }
        @Override double area() { return Math.PI * r * r; }
    }

    // --- INTERFACE'LER ---
    interface Drawable {
        int DEFAULT_SIZE = 10;               // public static final
        void draw();                          // public abstract

        default void drawTwice() {            // default metot — alt sınıf override etmek ZORUNDA değil
            draw();
            draw();
        }

        static Drawable empty() {             // static metot
            return () -> System.out.println("(boş)");
        }
    }

    interface Printable {
        default void print() {
            System.out.println("yazdırılıyor...");
        }
    }

    // Çoklu interface + abstract class genişletme
    static class Square extends Shape implements Drawable, Printable {
        double s;
        Square(String c, double s) { super(c); this.s = s; }
        @Override double area() { return s * s; }
        @Override public void draw() {
            System.out.println("□ " + color + " kare");
        }
    }

    // --- DIAMOND PROBLEM ---
    interface A { default void hi() { System.out.println("A hi"); } }
    interface B { default void hi() { System.out.println("B hi"); } }
    static class AB implements A, B {
        @Override public void hi() {
            A.super.hi();   // çakışmayı elle çöz
            B.super.hi();
        }
    }

    // --- FUNCTIONAL INTERFACE + LAMBDA ---
    @FunctionalInterface
    interface Calculator {
        int apply(int a, int b);
    }

    public static void main(String[] args) {

        Shape s = new Circle("Kırmızı", 5);
        s.describe();

        Square sq = new Square("Mavi", 4);
        sq.describe();
        sq.draw();
        sq.drawTwice();
        sq.print();

        Drawable empty = Drawable.empty();
        empty.draw();

        new AB().hi();

        // Lambda ile functional interface
        Calculator add = (a, b) -> a + b;
        Calculator mul = (a, b) -> a * b;
        System.out.println("5 + 3 = " + add.apply(5, 3));
        System.out.println("5 * 3 = " + mul.apply(5, 3));
    }
}
