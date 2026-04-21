package p15_lambda_streams;

import java.util.Arrays;
import java.util.List;
import java.util.function.*;

public class LambdaDemo {

    @FunctionalInterface
    interface Calculator { int apply(int a, int b); }

    public static void main(String[] args) {

        // --- Custom functional interface ---
        Calculator add = (a, b) -> a + b;
        Calculator mul = (a, b) -> a * b;
        System.out.println("add: " + add.apply(2, 3));
        System.out.println("mul: " + mul.apply(2, 3));

        // --- Built-in functional interfaces ---
        Function<String, Integer> len = s -> s.length();
        Predicate<Integer> isEven = n -> n % 2 == 0;
        Consumer<String> printer = System.out::println;
        Supplier<Double> randomer = Math::random;
        BiFunction<Integer, Integer, Integer> sum = Integer::sum;
        UnaryOperator<Integer> square = n -> n * n;

        System.out.println("len(merhaba) = " + len.apply("merhaba"));
        System.out.println("isEven(4) = " + isEven.test(4));
        printer.accept("consumer ile yazdırıldı");
        System.out.println("random = " + randomer.get());
        System.out.println("sum(3,4) = " + sum.apply(3, 4));
        System.out.println("square(5) = " + square.apply(5));

        // --- Function composition ---
        Function<Integer, Integer> plus1 = x -> x + 1;
        Function<Integer, Integer> times2 = x -> x * 2;
        System.out.println("plus1.andThen(times2)(3) = " + plus1.andThen(times2).apply(3)); // (3+1)*2=8
        System.out.println("plus1.compose(times2)(3) = " + plus1.compose(times2).apply(3)); // (3*2)+1=7

        // --- Predicate combine ---
        Predicate<Integer> positive = n -> n > 0;
        Predicate<Integer> evenAndPos = isEven.and(positive);
        Predicate<Integer> negOrOdd  = isEven.negate().or(positive.negate());
        System.out.println("evenAndPos(4)=" + evenAndPos.test(4) + " evenAndPos(-4)=" + evenAndPos.test(-4));

        // --- 4 Tip Method Reference ---
        Function<String, Integer> f1 = Integer::parseInt;      // static
        System.out.println(f1.apply("42"));

        String greeting = "Merhaba";
        Supplier<Integer> f2 = greeting::length;               // bound instance
        System.out.println(f2.get());

        Function<String, Integer> f3 = String::length;         // unbound instance
        System.out.println(f3.apply("abcdef"));

        Supplier<java.util.ArrayList<Integer>> f4 = java.util.ArrayList::new; // constructor
        System.out.println("yeni liste: " + f4.get());

        // --- CLOSURE ---
        int factor = 10;   // effectively final
        Function<Integer, Integer> scale = x -> x * factor;
        System.out.println("scale(5)=" + scale.apply(5));
        // factor = 20;  // bunu yaparsan yukarıdaki lambda derlenmez

        // Eski tarz vs lambda — bir koleksiyonu sıralama
        List<String> names = new java.util.ArrayList<>(Arrays.asList("Veli", "Ali", "Ayşe"));
        names.sort((a, b) -> a.compareTo(b));   // ya da Comparator.naturalOrder()
        System.out.println(names);
    }
}
