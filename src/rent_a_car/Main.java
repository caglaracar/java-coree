package rent_a_car;

public class Main {
    public static void main(String[] args) {

        Car car = new Car("Toyota", "Corolla", "34 ABC 123", true);
        Motorcycle motorcycle = new Motorcycle("Honda", "CBR", "06 XYZ 456", true, true);

        // Araçları kirala
        car.rent(3);
        System.out.println("----------------------------");


        motorcycle.rent(2);
        System.out.println("----------------------------");

        // Aynı arabayı tekrar kiralama
        car.rent(1); // müsait değil
        System.out.println("----------------------------");

        // İade et
        car.returnVehicle();
        System.out.println("----------------------------");

        // Polimorfizm — hepsini aynı dizide tut
        Rentable[] vehicles = { car, motorcycle };
        double total = 0;

        System.out.println("\n--- TÜM KİRALAMALAR ---");
        for (Rentable vehicle : vehicles) {
            System.out.println(vehicle.getBrand() + " " + vehicle.getModel()
                    + " | Müsait: " + vehicle.isAvailable()
                    + " | 5 günlük: " + vehicle.calculateCost(5) + " TL");
            total += vehicle.calculateCost(5);
        }
        System.out.println("Toplam 5 günlük: " + total + " TL");
    }
}
