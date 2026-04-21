package p06_inheritance_payment_methodss.p06_inheritance_car.p06_inheritance_payment_methodss.p06_inheritance;

public abstract class BasePayment implements PaymentMethod {

    private final String ownerName;  // kart/hesap sahibi
    private double balance;           // bakiye
    private final double limit;       // maksimum limit

    public BasePayment(String ownerName, double balance, double limit) {
        this.ownerName = ownerName;
        this.balance = balance;
        this.limit = limit;
    }

    // ✅ Ortak davranış — hepsi aynı log atar
    protected void logTransaction(String type, double amount) {
        System.out.println("LOG | " + type + " | " + ownerName + " | " + amount + " TL | " + getPaymentType());
    }

    // ✅ Ortak davranış — hepsi aynı makbuzu yazdırır
    protected void printReceipt(double amount) {
        System.out.println("---------- MAKBUZ ----------");
        System.out.println("Sahip     : " + ownerName);
        System.out.println("Yöntem    : " + getPaymentType());
        System.out.println("Tutar     : " + amount + " TL");
        System.out.println("Kalan     : " + (balance - amount) + " TL");
        System.out.println("----------------------------");
    }

    // ✅ Ortak davranış — hepsi aynı bakiye kontrolü yapar
    @Override
    public boolean hasEnoughBalance(double amount) {
        return balance >= amount && amount <= limit;
    }

    // bakiyeyi güncelle — protected, sadece alt class kullanır
    protected void deductBalance(double amount) {
        this.balance -= amount;
    }

    protected void addBalance(double amount) {
        this.balance += amount;
    }

    public double getBalance() { return balance; }
    public String getOwnerName() { return ownerName; }
    public double getLimit() { return limit; }

    // ❌ Abstract — her ödeme yöntemi kendisi yazar
    @Override
    public abstract void pay(double amount);

    @Override
    public abstract void refund(double amount);

    @Override
    public abstract String getPaymentType();
}