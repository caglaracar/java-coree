package p02_operators_controlflow;

public class ControlFlowDemo {
    public static void main(String[] args) {

        // --- IF / ELSE IF / ELSE ---
        int score = 75;
        if (score >= 90)      System.out.println("AA");
        else if (score >= 80) System.out.println("BA");
        else if (score >= 70) System.out.println("BB");
        else                  System.out.println("FF");

        // --- KLASİK SWITCH ---
        int day = 3;
        switch (day) {
            case 1:
                System.out.println("Pazartesi");
                break;
            case 2:
                System.out.println("Salı");
                break;
            case 3:
            case 4:   // fall-through: 3 VEYA 4
                System.out.println("Çarşamba ya da Perşembe");
                break;
            default:
                System.out.println("Hafta sonu / bilinmeyen");
        }

        // --- MODERN SWITCH EXPRESSION (Java 14+) ---
        String name = switch (day) {
            case 1 -> "Pazartesi";
            case 2 -> "Salı";
            case 3, 4 -> "Çarş/Perş";
            case 5 -> "Cuma";
            case 6, 7 -> {
                // Blok formu: yield ile değer dönülür
                String tag = "Hafta sonu";
                yield tag + " (" + day + ")";
            }
            default -> "?";
        };
        System.out.println("switch expr: " + name);

        // --- FOR ---
        int sum = 0;
        for (int i = 1; i <= 10; i++) sum += i;
        System.out.println("1..10 toplam = " + sum);

        // --- FOR-EACH ---
        int[] arr = {3, 1, 4, 1, 5, 9, 2, 6};
        int total = 0;
        for (int n : arr) total += n;
        System.out.println("arr toplam = " + total);

        // --- WHILE ---
        int i = 1;
        while (i <= 3) {
            System.out.println("while: " + i);
            i++;
        }

        // --- DO-WHILE (en az 1 kere çalışır) ---
        int j = 10;
        do {
            System.out.println("do-while: " + j);
            j++;
        } while (j < 10);

        // --- LABELED BREAK ---
        dis:
        for (int r = 0; r < 3; r++) {
            for (int k = 0; k < 3; k++) {
                if (r == 1 && k == 1) {
                    System.out.println("dış döngüyü kırıyorum");
                    break dis;
                }
                System.out.println("r=" + r + " k=" + k);
            }
        }

        // --- CONTINUE ---
        System.out.print("Tekler: ");
        for (int t = 1; t <= 10; t++) {
            if (t % 2 == 0) continue;
            System.out.print(t + " ");
        }
        System.out.println();
    }
}
