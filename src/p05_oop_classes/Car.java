package p05_oop_classes;

public class Car {

    // --- Field'lar ---
    private String brand;
    private String color;
    private int speed;                 // varsayılan 0
    private static int productionCount = 0;  // tüm Car'lar paylaşır

    // --- Constructor'lar (overload) ---
    public Car() {
        this("Generic", "Black");  // this(...) ilk satırda, başka ctor'a delege
    }

    public Car(String brand) {
        this(brand, "Black");
    }

    public Car(String brand, String color) {
        this.brand = brand;   // 'this.' ayrımı
        this.color = color;
        productionCount++;
    }

    // --- Instance metotlar ---
    public Car accelerate(int amount) {
        this.speed += amount;
        return this;   // builder-style zincirleme için
    }

    public Car brake(int amount) {
        this.speed = Math.max(0, this.speed - amount);
        return this;
    }

    public void status() {
        System.out.printf("%s %s - hız: %d km/h%n", color, brand, speed);
    }

    // --- Static metot ---
    public static int getProductionCount() {
        return productionCount;
    }

    // --- Getters / Setters ---
    public String getBrand() { return brand; }
    public String getColor() { return color; }
    public int getSpeed() { return speed; }
}
