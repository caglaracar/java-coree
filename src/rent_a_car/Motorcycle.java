package rent_a_car;

public class Motorcycle extends BaseVehicle {
    private final int engineCC;
    private final boolean hasLicenseA;

    public Motorcycle(String brand, String model, String plate, boolean available, boolean hasLicenseA) {
        super(brand, model, plate, 500, available); // günlük 500 TL
        this.engineCC = 600;
        this.hasLicenseA = hasLicenseA;
    }

    @Override
    public void rent(int days) {
        if (!hasLicenseA) {
            System.out.println("❌ A sınıfı ehliyet gerekli!");
            return;
        }
        if (!isAvailable()) {
            System.out.println("❌ " + getBrand() + " " + getModel() + " müsait değil!");
            return;
        }
        setAvailable(false);
        System.out.println("🏍️ Araç kiralandı!");
        System.out.println("Araç    : " + getBrand() + " " + getModel());
        System.out.println("Plaka   : " + getPlate());
        System.out.println("Motor   : " + engineCC + " cc");
        System.out.println("Süre    : " + days + " gün");
        System.out.println("Ücret   : " + calculateCost(days) + " TL");
    }

    @Override
    public void returnVehicle() {
        setAvailable(true);
        System.out.println("✅ " + getBrand() + " " + getModel() + " iade edildi, müsait.");
    }
}
