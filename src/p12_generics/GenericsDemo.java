package p12_generics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenericsDemo {

    // --- Generic sınıf ---
    static class Box<T> {
        private T value;
        public void set(T v) { this.value = v; }
        public T get() { return value; }
    }

    // --- İki parametreli generic ---
    static class Pair<K, V> {
        final K key; final V value;
        Pair(K k, V v) { this.key = k; this.value = v; }
        @Override public String toString() { return "(" + key + ", " + value + ")"; }
    }

    // --- Generic metot ---
    static <T> T firstOrNull(List<T> list) {
        return list.isEmpty() ? null : list.get(0);
    }

    // --- Bounded type: sayı topla ---
    static <T extends Number> double sum(List<T> xs) {
        double total = 0;
        for (T x : xs) total += x.doubleValue();
        return total;
    }

    // --- PECS: Producer Extends ---
    static double sumProducer(List<? extends Number> xs) {
        double s = 0;
        for (Number n : xs) s += n.doubleValue();
        return s;
    }

    // --- PECS: Consumer Super ---
    static void addIntegers(List<? super Integer> dst) {
        dst.add(1); dst.add(2); dst.add(3);
        // dst.get(0) -> sadece Object olarak okunur
    }

    public static void main(String[] args) {

        Box<String> sb = new Box<>();
        sb.set("merhaba");
        System.out.println("Box<String>: " + sb.get());

        Box<Integer> ib = new Box<>();
        ib.set(42);
        System.out.println("Box<Integer>: " + ib.get());

        Pair<String, Integer> p = new Pair<>("yas", 30);
        System.out.println("Pair: " + p);

        List<String> names = Arrays.asList("Ali", "Veli");
        System.out.println("first: " + firstOrNull(names));

        List<Integer> ints = Arrays.asList(1, 2, 3);
        List<Double> dbls = Arrays.asList(1.5, 2.5, 3.5);
        System.out.println("sum ints: " + sum(ints));
        System.out.println("sum dbls: " + sum(dbls));

        // PECS
        System.out.println("producer: " + sumProducer(ints));
        List<Number> nums = new ArrayList<>();
        addIntegers(nums);   // List<Number> List<? super Integer> tatmin eder
        System.out.println("consumer after add: " + nums);

        // --- INVARIANCE ---
        // List<String> DEĞİLDİR List<Object>  — derlenmez:
        // List<Object> bad = new ArrayList<String>();

        // Ama şu OK:
        List<?> anyList = names;
        System.out.println("anyList size: " + anyList.size());
        // anyList.add("x");  // HATA — ? tipine yazamayız

        // --- TYPE ERASURE İSPATI ---
        List<String> ls = new ArrayList<>();
        List<Integer> li = new ArrayList<>();
        System.out.println("aynı Class? " + (ls.getClass() == li.getClass())); // true
        // İkisi de ArrayList -- generic parametre silinmiş
    }
}
