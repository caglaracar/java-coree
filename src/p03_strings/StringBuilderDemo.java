package p03_strings;

/**
 * Performans farkını gözle gör.
 *
 * Not: Çıktı süreleri makineye göre değişir; AMA ORAN hep benzerdir.
 */
public class StringBuilderDemo {

    static final int N = 50_000;

    public static void main(String[] args) {

        // --- 1) String ile "+=" — O(N^2), YAVAŞ ---
        long t1 = System.currentTimeMillis();
        String s = "";
        for (int i = 0; i < N; i++) {
            s += i;    // her turda YENİ String üretir
        }
        long t2 = System.currentTimeMillis();
        System.out.println("String +=      : " + (t2 - t1) + " ms, uzunluk=" + s.length());

        // --- 2) StringBuilder — O(N), HIZLI ---
        long t3 = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < N; i++) {
            sb.append(i);
        }
        String r = sb.toString();
        long t4 = System.currentTimeMillis();
        System.out.println("StringBuilder  : " + (t4 - t3) + " ms, uzunluk=" + r.length());

        // --- StringBuilder API ---
        StringBuilder demo = new StringBuilder("Merhaba");
        demo.append(" Dünya")
            .insert(0, ">> ")
            .replace(3, 10, "SELAM")
            .reverse();
        System.out.println(demo);

        // StringBuilder kapasite yönetir (initial 16).
        // Daha iyi performans için tahmini boyut verebilirsin:
        StringBuilder big = new StringBuilder(1024);
        big.append("ön-tahsisli buffer");
        System.out.println(big);
    }
}
