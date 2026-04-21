package rent_a_car;

public class Car extends BaseVehicle {
    private final int seatCount;

    public Car(String brand, String model, String plate, boolean available) {
        super(brand, model, plate, 1500, available); // günlük 1500 TL
        this.seatCount = 5;
    }

    @Override
    public void rent(int days) {
        if (!isAvailable()) {
            System.out.println("❌ " + getBrand() + " " + getModel() + " müsait değil!");
            return;
        }
        setAvailable(false);
        System.out.println("🚗 Araç kiralandı!");
        System.out.println("Araç    : " + getBrand() + " " + getModel());
        System.out.println("Plaka   : " + getPlate());
        System.out.println("Koltuk  : " + seatCount);
        System.out.println("Süre    : " + days + " gün");
        System.out.println("Ücret   : " + calculateCost(days) + " TL");
    }

    @Override
    public void returnVehicle() {
        setAvailable(true);
        System.out.println("✅ " + getBrand() + " " + getModel() + " iade edildi, müsait.");
    }
}