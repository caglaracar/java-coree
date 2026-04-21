package p09_encapsulation_access;

/**
 * Encapsulation örneği: balance field'ına dışarıdan ERİŞİM YOK.
 * Sadece kontrollü deposit/withdraw metotlarıyla değiştirilir.
 */
public class BankAccount {

    private final String owner;      // final: bir kez atanır
    private double balance;          // private: dış dünya göremez
    private static long nextId = 1;  // static counter
    private final long id;

    public BankAccount(String owner, double initial) {
        if (initial < 0) throw new IllegalArgumentException("initial < 0");
        this.owner = owner;
        this.balance = initial;
        this.id = nextId++;
    }

    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0");
        balance += amount;
    }

    public void withdraw(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0");
        if (amount > balance) throw new IllegalStateException("yetersiz bakiye");
        balance -= amount;
    }

    // Getter'lar var, setter YOK (balance doğrudan set edilemez)
    public double getBalance() { return balance; }
    public String getOwner()   { return owner; }
    public long getId()        { return id; }

    @Override
    public String toString() {
        return String.format("Account#%d owner=%s balance=%.2f", id, owner, balance);
    }

    public static void main(String[] args) {
        BankAccount a = new BankAccount("Ali", 100);
        a.deposit(50);
        a.withdraw(30);
        System.out.println(a);

        // a.balance = 9_999_999; // HATA: private
        // a.owner   = "Hacker";  // HATA: final

        try {
            a.withdraw(10_000);
        } catch (IllegalStateException ex) {
            System.out.println("yakalandı: " + ex.getMessage());
        }
    }
}
