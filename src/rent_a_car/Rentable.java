package rent_a_car;

public interface Rentable {
    void rent(int days);
    void returnVehicle();
    boolean isAvailable();
    double calculateCost(int days);
    String getPlate();
    String getBrand();
    String getModel();
}