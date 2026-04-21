package project_1;

public interface Rentable {
    void rent(int days);
    void returnVehicle();
    boolean isAvailable();
    double calculateCost(int days);
    String getPlate();
    String getBrand();
    String getModel();
}