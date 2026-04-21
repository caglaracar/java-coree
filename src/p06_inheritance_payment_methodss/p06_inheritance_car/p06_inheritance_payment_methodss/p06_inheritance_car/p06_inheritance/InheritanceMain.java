package p06_inheritance_payment_methodss.p06_inheritance_car.p06_inheritance_payment_methodss.p06_inheritance_car.p06_inheritance;

public class InheritanceMain {
    public static void main(String[] args) {

        // Kredi Kartı
        CreditCard creditCard = new CreditCard("Caglar Acar", 10000, "1234567890123456", "12/27", 123);
        PaymentProcessor cardProcessor = new PaymentProcessor(creditCard);
        cardProcessor.processPayment(2500);
        cardProcessor.processRefund(500);
        System.out.println("Maskeli kart: " + creditCard.getMaskedCardNumber());

        System.out.println("\n============================\n");

        // PayPal — doğrulanmadan ödeme dene
        PayPal paypal = new PayPal("Caglar Acar", 5000, "caglar@gmail.com");
        PaymentProcessor paypalProcessor = new PaymentProcessor(paypal);
        paypalProcessor.processPayment(1000); // ❌ doğrulanmamış

        paypal.verifyAccount(); // doğrula
        paypalProcessor.processPayment(1000); // ✅ çalışır

        System.out.println("\n============================\n");

        // Apple Pay
        ApplePay applePay = new ApplePay("Caglar Acar", 8000, "iPhone-15-Pro");
        PaymentProcessor appleProcessor = new PaymentProcessor(applePay);
        appleProcessor.processPayment(3000);

        System.out.println("\n============================\n");

        // Polimorfizm — hepsini aynı listede tut
        PaymentMethod[] payments = {creditCard, paypal, applePay};

        System.out.println("--- TÜM ÖDEMELER ---");
        for (PaymentMethod payment : payments) {
            System.out.println(payment.getPaymentType() + " | Bakiye: " + ((BasePayment) payment).getBalance() + " TL");
        }
    }
}

