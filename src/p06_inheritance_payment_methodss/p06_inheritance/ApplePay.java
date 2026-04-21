package p06_inheritance_payment_methodss.p06_inheritance;


// Apple Pay
public class ApplePay extends BasePayment {

    private final String deviceId;
    private boolean isFaceIdVerified;

    public ApplePay(String ownerName, double balance, String deviceId) {
        super(ownerName, balance, 20000); // Apple Pay limiti 20000
        this.deviceId = deviceId;
        this.isFaceIdVerified = false;
    }

    @Override
    public void pay(double amount) {
        if (!authenticateWithFaceId()) {
            System.out.println("❌ Face ID doğrulaması başarısız!");
            return;
        }
        if (!hasEnoughBalance(amount)) {
            System.out.println("❌ Apple Pay yetersiz bakiye!");
            return;
        }
        deductBalance(amount);
        logTransaction("ÖDEME", amount);
        printReceipt(amount);
        System.out.println("✅ Apple Pay ile ödeme başarılı! Cihaz: " + deviceId);
    }

    @Override
    public void refund(double amount) {
        addBalance(amount);
        logTransaction("İADE", amount);
        System.out.println("✅ " + amount + " TL Apple Pay'e iade edildi!");
    }

    @Override
    public String getPaymentType() { return "APPLE PAY"; }

    // Apple Pay'e özel — Face ID
    private boolean authenticateWithFaceId() {
        System.out.println("🔐 Face ID kontrol ediliyor...");
        this.isFaceIdVerified = true; // gerçekte donanımdan gelir
        return isFaceIdVerified;
    }
}
