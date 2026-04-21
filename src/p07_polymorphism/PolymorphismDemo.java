package p07_polymorphism;

public class PolymorphismDemo {

    // --- Hiyerarşi ---
    static class Shape {
        String type = "Shape";
        double area() { return 0; }
        static void info() { System.out.println("Shape.info (static)"); }
    }

    static class Circle extends Shape {
        String type = "Circle";   // SHADOW (field polimorfik DEĞİL)
        double r;
        Circle(double r) { this.r = r; }
        @Override double area() { return Math.PI * r * r; }
        static void info() { System.out.println("Circle.info (static)"); }
    }

    static class Square extends Shape {
        double s;
        Square(double s) { this.s = s; }
        @Override double area() { return s * s; }
    }

    // --- Overloading (compile-time) ---
    static int add(int a, int b)          { return a + b; }
    static double add(double a, double b) { return a + b; }
    static int add(int a, int b, int c)   { return a + b + c; }

    // --- Polimorfik metot: Shape alır, alt tür fark etmez ---
    static void printArea(Shape s) {
        System.out.println("area = " + s.area());
    }

    public static void main(String[] args) {

        // --- OVERLOADING ---
        System.out.println(add(2, 3));          // int
        System.out.println(add(2.5, 3.5));      // double
        System.out.println(add(1, 2, 3));       // 3-arg

        // --- OVERRIDING + DYNAMIC DISPATCH ---
        Shape[] shapes = { new Circle(5), new Square(4), new Circle(1) };
        for (Shape s : shapes) {
            printArea(s);   // runtime'da doğru area() çağrılır
        }

        // --- FIELD SHADOWING (polimorfik DEĞİL!) ---
        Shape ref = new Circle(1);
        System.out.println("ref.type = " + ref.type);   // "Shape" — referans tipine göre!
        // ama metot çağrılarında davranış alt sınıftan gelir:
        System.out.println("ref.area() = " + ref.area());

        // --- STATIC METHOD HIDING ---
        Shape.info();    // Shape.info
        Circle.info();   // Circle.info
        Shape r2 = new Circle(1);
        r2.info();       // HALA Shape.info! (static hiding, dinamik DEĞİL)
        // IDE burada uyarır: "should be accessed statically"

        // --- instanceof & PATTERN MATCHING ---
        Shape s = new Circle(2);
        if (s instanceof Circle c) {
            System.out.println("yarıçap = " + c.r);
        }
    }
}
