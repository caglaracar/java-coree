package project_3;

public class Main {
    public static void main(String[] args) {

        // 1. Doktorları oluştur
        Cardiologist ahmet = new Cardiologist("Dr. Ahmet Yılmaz");
        Dentist ayse = new Dentist("Dr. Ayşe Demir");

        // 2. Hasta muayene et
        ahmet.examine("Mehmet Kaya");  // çıktı 1
        ayse.examine("Ali Veli");      // çıktı 2

        // 3. İzne gönder
        ahmet.takeLeave();

        // 4. Tekrar muayene dene
        ahmet.examine("Yeni Hasta");   // ❌ izinli

        // 5. İzinden dön
        ahmet.returnFromLeave();       // ✅

        // 6. Hepsini listele
        Doctor[] doctors = {ahmet, ayse};
        for (Doctor doctor : doctors) {
            System.out.println(doctor.getName() + " | " + doctor.getSpeciality());
        }
    }
}

