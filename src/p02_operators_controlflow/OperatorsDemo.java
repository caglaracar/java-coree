package p02_operators_controlflow;

public class OperatorsDemo {
    public static void main(String[] args) {

        // --- ARİTMETİK ---
        System.out.println("7 / 2 = " + (7 / 2));     // 3 (int bölme)
        System.out.println("7 / 2.0 = " + (7 / 2.0)); // 3.5
        System.out.println("7 % 2 = " + (7 % 2));     // 1

        // --- ++ / -- PREFIX vs POSTFIX ---
        int a = 5;
        int b = a++;   // b = 5, a = 6
        int c = ++a;   // a = 7, c = 7
        System.out.println("a=" + a + " b=" + b + " c=" + c);

        // Klasik mülakat tuzağı:
        int x = 1;
        x = x++ + ++x;
        // x++ kullanıldığında değer 1, sonra x=2.
        // ++x kullanıldığında x=3, değer 3.
        // Toplam = 1 + 3 = 4. Atamadan sonra x = 4.
        System.out.println("x = " + x);

        // --- SHORT-CIRCUIT ---
        // && sol taraf false olursa sağa bakmaz (metot çağrılarında önemli!)
        String s = null;
        if (s != null && s.length() > 0) {   // güvenli: NPE atmaz
            System.out.println("dolu");
        } else {
            System.out.println("boş/null");
        }
        // Eğer '&&' yerine '&' kullansaydık NPE alırdık!

        // --- BIT OPERATÖRLERİ ---
        int m = 0b1100; // 12
        int n = 0b1010; // 10
        System.out.println("m & n = " + (m & n)); // 1000 = 8
        System.out.println("m | n = " + (m | n)); // 1110 = 14
        System.out.println("m ^ n = " + (m ^ n)); // 0110 = 6 (XOR)
        System.out.println("~m    = " + (~m));     // -13 (2's complement)
        System.out.println("m << 2= " + (m << 2)); // 48 (12 * 4)
        System.out.println("m >> 1= " + (m >> 1)); // 6

        // --- TERNARY ---
        int age = 20;
        String status = (age >= 18) ? "yetişkin" : "çocuk";
        System.out.println(status);

        // --- == vs equals ---
        String s1 = new String("java");
        String s2 = new String("java");
        System.out.println("== : " + (s1 == s2));       // false (farklı nesneler)
        System.out.println("equals: " + s1.equals(s2)); // true  (içerik aynı)
    }
}
