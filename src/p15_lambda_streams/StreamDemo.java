package p15_lambda_streams;

import java.util.*;
import java.util.stream.*;

public class StreamDemo {

    record Person(String name, String city, int age) {}

    public static void main(String[] args) {

        List<Person> people = List.of(
            new Person("Ali",   "Ankara",   30),
            new Person("Veli",  "İstanbul", 25),
            new Person("Ayşe",  "Ankara",   35),
            new Person("Fatma", "İzmir",    28),
            new Person("Can",   "İstanbul", 40),
            new Person("Deniz", "Ankara",   22)
        );

        // --- filter + map + collect ---
        List<String> ankaraNames = people.stream()
            .filter(p -> p.city().equals("Ankara"))
            .map(Person::name)
            .sorted()
            .toList();   // Java 16+; aksi halde .collect(Collectors.toList())
        System.out.println("Ankara: " + ankaraNames);

        // --- count ---
        long over30 = people.stream().filter(p -> p.age() > 30).count();
        System.out.println("30 üstü sayısı = " + over30);

        // --- reduce ---
        int totalAge = people.stream().mapToInt(Person::age).sum();  // primitive stream
        int reduced  = people.stream().map(Person::age).reduce(0, Integer::sum);
        System.out.println("yaş toplamı = " + totalAge + " (reduce=" + reduced + ")");

        // --- min / max ---
        Person oldest = people.stream()
            .max(Comparator.comparingInt(Person::age))
            .orElseThrow();
        System.out.println("en yaşlı: " + oldest);

        // --- groupingBy ---
        Map<String, List<Person>> byCity = people.stream()
            .collect(Collectors.groupingBy(Person::city));
        System.out.println("şehire göre: " + byCity);

        Map<String, Long> countByCity = people.stream()
            .collect(Collectors.groupingBy(Person::city, Collectors.counting()));
        System.out.println("şehir sayıları: " + countByCity);

        Map<String, Double> avgAgeByCity = people.stream()
            .collect(Collectors.groupingBy(Person::city, Collectors.averagingInt(Person::age)));
        System.out.println("ortalama yaş: " + avgAgeByCity);

        // --- partitioningBy (boolean) ---
        Map<Boolean, List<Person>> yetiskin = people.stream()
            .collect(Collectors.partitioningBy(p -> p.age() >= 30));
        System.out.println("yetişkinler: " + yetiskin.get(true));

        // --- joining ---
        String csv = people.stream()
            .map(Person::name)
            .collect(Collectors.joining(", ", "[", "]"));
        System.out.println("isimler: " + csv);

        // --- flatMap: nested streams'i düzleştir ---
        List<List<Integer>> nested = List.of(
            List.of(1, 2, 3), List.of(4, 5), List.of(6)
        );
        List<Integer> flat = nested.stream()
            .flatMap(List::stream)
            .toList();
        System.out.println("flat: " + flat);

        // --- Primitive stream ---
        int sum = IntStream.rangeClosed(1, 100).sum();
        double avg = IntStream.rangeClosed(1, 100).average().orElse(0);
        System.out.println("1..100 toplam=" + sum + " ort=" + avg);

        // --- anyMatch / allMatch / noneMatch / findFirst ---
        System.out.println("anyOver35:  " + people.stream().anyMatch(p -> p.age() > 35));
        System.out.println("allOver20:  " + people.stream().allMatch(p -> p.age() > 20));
        System.out.println("noneOver50: " + people.stream().noneMatch(p -> p.age() > 50));

        Optional<Person> first = people.stream().filter(p -> p.age() > 30).findFirst();
        first.ifPresent(p -> System.out.println("ilk 30+: " + p));

        // --- OPTIONAL örneği ---
        Optional<Person> maybe = people.stream()
            .filter(p -> p.name().equals("Yok"))
            .findFirst();
        String displayName = maybe.map(Person::name).orElse("bulunamadı");
        System.out.println("isim: " + displayName);

        // --- PARALLEL STREAM (örnek amaçlı) ---
        long parallelSum = LongStream.rangeClosed(1, 10_000_000).parallel().sum();
        System.out.println("parallel sum = " + parallelSum);

        // --- YAYGIN HATA: stream iki kere kullanılamaz ---
        Stream<Integer> s = Stream.of(1, 2, 3);
        s.count();
        try {
            s.count();   // IllegalStateException
        } catch (IllegalStateException e) {
            System.out.println("stream tek kere tüketilir!");
        }
    }
}
