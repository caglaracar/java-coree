package p06_inheritance_payment_methodss.p06_inheritance_car.p06_inheritance_payment_methodss.p06_inheritance;

// PayPal
public class PayPal extends BasePayment {

    private final String email;
    private boolean isVerified; // hesap doğrulandı mı

    public PayPal(String ownerName, double balance, String email) {
        super(ownerName, balance, 30000); // PayPal limiti 30000
        this.email = email;
        this.isVerified = false; // başlangıçta doğrulanmamış
    }

    @Override
    public void pay(double amount) {
        if (!isVerified) {
            System.out.println("❌ PayPal hesabı doğrulanmamış! Önce doğrulama yapın.");
            return;
        }
        if (!hasEnoughBalance(amount)) {
            System.out.println("❌ PayPal yetersiz bakiye!");
            return;
        }
        deductBalance(amount);
        logTransaction("ÖDEME", amount);
        printReceipt(amount);
        System.out.println("✅ PayPal ile ödeme başarılı! Bildirim: " + email);
    }

    @Override
    public void refund(double amount) {
        addBalance(amount);
        logTransaction("İADE", amount);
        System.out.println("✅ " + amount + " TL PayPal hesabına iade edildi! Email: " + email);
    }

    @Override
    public String getPaymentType() { return "PAYPAL"; }

    // PayPal'a özel metod — başka ödeme yönteminde yok
    public void verifyAccount() {
        this.isVerified = true;
        System.out.println("✅ PayPal hesabı doğrulandı: " + email);
    }

    public boolean isVerified() { return isVerified; }
}