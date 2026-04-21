package p06_inheritance_payment_methodss.p06_inheritance_car.p06_inheritance;

// Kredi Kartı
public class CreditCard extends BasePayment {

    private final String cardNumber;
    private final String expiryDate;
    private final int cvv;

    public CreditCard(String ownerName, double balance, String cardNumber, String expiryDate, int cvv) {
        super(ownerName, balance, 50000); // kredi kartı limiti 50000
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
    }

    @Override
    public void pay(double amount) {
        if (!hasEnoughBalance(amount)) {
            System.out.println("❌ Kredi kartı yetersiz bakiye veya limit aşıldı!");
            return;
        }
        if (!isCardValid()) {
            System.out.println("❌ Kart geçersiz!");
            return;
        }
        deductBalance(amount);
        logTransaction("ÖDEME", amount);
        printReceipt(amount);
        System.out.println("✅ Kredi kartı ile ödeme başarılı!");
    }

    @Override
    public void refund(double amount) {
        addBalance(amount);
        logTransaction("İADE", amount);
        System.out.println("✅ " + amount + " TL kredi kartına iade edildi!");
    }

    @Override
    public String getPaymentType() { return "KREDİ KARTI"; }

    // Kredi kartına özel metod — başka ödeme yönteminde yok
    private boolean isCardValid() {
        return cardNumber != null && cardNumber.length() == 16;
    }

    // Kredi kartına özel — maskelenmiş kart numarası
    public String getMaskedCardNumber() {
        return "**** **** **** " + cardNumber.substring(12);
    }
}