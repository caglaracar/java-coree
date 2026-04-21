package p06_inheritance_payment_methodss.p06_inheritance;

public interface PaymentMethod {
    void pay(double amount);        // ödeme yap
    void refund(double amount);     // iade et
    boolean hasEnoughBalance(double amount); // bakiye yeterli mi
    String getPaymentType();        // ödeme tipi ne
}