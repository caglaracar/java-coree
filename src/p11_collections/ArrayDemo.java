package p11_collections;

import java.util.Arrays;

public class ArrayDemo {
    public static void main(String[] args) {

        // --- Tanımlama ---
        int[] a = new int[5];                // {0,0,0,0,0}
        int[] b = {5, 3, 9, 1, 7};
        String[] names = new String[]{"Ali", "Veli", "Ayşe"};
        int[][] matrix = { {1,2,3}, {4,5,6} }; // 2D

        // --- Boyut ---
        System.out.println("length = " + b.length);
        System.out.println("matrix.length = " + matrix.length + " x " + matrix[0].length);

        // --- Sıralama ---
        Arrays.sort(b);
        System.out.println("sorted: " + Arrays.toString(b));

        // --- Kopyalama ---
        int[] c = Arrays.copyOf(b, 8);        // sağı 0 ile doldurur
        System.out.println("copy: " + Arrays.toString(c));

        // --- Arama (sorted dizide) ---
        int idx = Arrays.binarySearch(b, 5);
        System.out.println("binarySearch(5) idx = " + idx);

        // --- Stream ile (Java 8+) ---
        int sum = Arrays.stream(b).sum();
        double avg = Arrays.stream(b).average().orElse(0);
        int max = Arrays.stream(b).max().orElse(Integer.MIN_VALUE);
        System.out.printf("sum=%d avg=%.2f max=%d%n", sum, avg, max);

        // --- Dolaşma ---
        for (int i = 0; i < b.length; i++) System.out.print(b[i] + " ");
        System.out.println();
        for (int x : b) System.out.print(x + " ");
        System.out.println();

        // --- 2D dolaşma ---
        for (int[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }

        // --- Dizi eşitliği ---
        System.out.println("equals? " + Arrays.equals(new int[]{1,2}, new int[]{1,2})); // true
        // == YAPMA: referans karşılaştırır!

        // --- Array'i List'e çevirme ---
        // Arrays.asList fixed-size liste döner (add/remove çalışmaz)
        var fixed = Arrays.asList("a", "b", "c");
        System.out.println(fixed);
        // fixed.add("d"); // UnsupportedOperationException
    }
}
