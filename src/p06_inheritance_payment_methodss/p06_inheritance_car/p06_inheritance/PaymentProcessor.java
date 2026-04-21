package p06_inheritance_payment_methodss.p06_inheritance_car.p06_inheritance;

// Hangi ödeme yöntemi gelirse gelsin işle — polimorfizm
public class PaymentProcessor {

    private final PaymentMethod paymentMethod; // interface tipi

    public PaymentProcessor(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void processPayment(double amount) {
        System.out.println("\n🚀 Ödeme başlatılıyor...");
        paymentMethod.pay(amount);
    }

    public void processRefund(double amount) {
        System.out.println("\n🔄 İade başlatılıyor...");
        paymentMethod.refund(amount);
    }
}