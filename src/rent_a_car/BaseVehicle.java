package rent_a_car;

public abstract class BaseVehicle implements Rentable {
    private final String brand;
    private final String model;
    private final String plate;
    private final double dailyPrice;
    private boolean available;

    public BaseVehicle(String brand, String model, String plate, double dailyPrice, boolean available) {
        this.brand = brand;
        this.model = model;
        this.plate = plate;
        this.dailyPrice = dailyPrice;
        this.available = available;
    }

    @Override
    public String getBrand() { return brand; }

    @Override
    public String getModel() { return model; }

    @Override
    public String getPlate() { return plate; }

    @Override
    public boolean isAvailable() { return available; }

    @Override
    public double calculateCost(int days) { return dailyPrice * days; }

    protected void setAvailable(boolean available) { this.available = available; }
}
