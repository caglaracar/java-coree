package p01_basics;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper sınıflar ve autoboxing/unboxing tuzakları.
 *
 * KRİTİK MÜLAKAT BİLGİSİ:
 *  - Integer cache: -128..127 arası Integer nesneleri önbelleklenir.
 *    Bu yüzden '==' bu aralıkta true, dışında false dönebilir.
 *    Nesne eşitliği için HER ZAMAN .equals() kullan!
 */
public class WrappersAndAutoboxing {
    public static void main(String[] args) {

        // --- AUTOBOXING: primitive -> wrapper ---
        Integer a = 5;            // int -> Integer.valueOf(5)
        // --- UNBOXING: wrapper -> primitive ---
        int b = a;                // a.intValue()

        // Koleksiyonlar primitive alamaz, wrapper gerekir:
        List<Integer> numbers = new ArrayList<>();
        numbers.add(1);           // autobox
        numbers.add(2);
        int sum = 0;
        for (int n : numbers) {   // unbox
            sum += n;
        }
        System.out.println("sum = " + sum);

        // --- INTEGER CACHE TUZAĞI ---
        Integer x1 = 100;
        Integer x2 = 100;
        System.out.println("100 == 100 ? " + (x1 == x2));  // true (cached)

        Integer y1 = 200;
        Integer y2 = 200;
        System.out.println("200 == 200 ? " + (y1 == y2));  // false (yeni nesne)
        System.out.println("200.equals(200) ? " + y1.equals(y2)); // true

        // --- NULL UNBOXING = NullPointerException ---
        Integer maybe = null;
        try {
            int unboxed = maybe;   // NPE!
            System.out.println(unboxed);
        } catch (NullPointerException e) {
            System.out.println("NPE yakalandı: null unbox edilemez");
        }

        // --- YARARLI STATIC METODLAR ---
        System.out.println(Integer.parseInt("42"));     // String -> int
        System.out.println(Integer.toBinaryString(10)); // "1010"
        System.out.println(Integer.max(3, 7));          // 7
        System.out.println(Double.parseDouble("3.14"));
        System.out.println(Boolean.parseBoolean("true"));
    }
}
