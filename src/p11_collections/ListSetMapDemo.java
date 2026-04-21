package p11_collections;

import java.util.*;

public class ListSetMapDemo {
    public static void main(String[] args) {

        // --- LIST ---
        List<String> al = new ArrayList<>();
        al.add("Java"); al.add("Python"); al.add("Java");  // tekrar OK
        al.add(1, "Go");                                     // index'le ekleme
        System.out.println("ArrayList: " + al);              // [Java, Go, Python, Java]
        System.out.println("get(2): " + al.get(2));
        al.remove("Java");                                    // ilk bulduğunu siler
        System.out.println("after remove: " + al);

        LinkedList<String> ll = new LinkedList<>();
        ll.add("b"); ll.addFirst("a"); ll.addLast("c");      // deque davranışı
        System.out.println("LinkedList: " + ll);

        // --- SET ---
        Set<String> hs = new HashSet<>();
        hs.add("a"); hs.add("b"); hs.add("a");               // tekrar yutulur
        System.out.println("HashSet: " + hs);                // sırasız

        Set<String> lhs = new LinkedHashSet<>(List.of("z","a","m","a"));
        System.out.println("LinkedHashSet (insert order): " + lhs); // [z, a, m]

        Set<Integer> ts = new TreeSet<>(List.of(5, 1, 3, 2, 4));
        System.out.println("TreeSet (sorted): " + ts);       // [1,2,3,4,5]

        // --- MAP ---
        Map<String, Integer> scores = new HashMap<>();
        scores.put("Ali", 90);
        scores.put("Veli", 85);
        scores.put("Ali", 95);                               // aynı key => override
        System.out.println("scores: " + scores);
        System.out.println("Ali: " + scores.get("Ali"));
        System.out.println("contains 'Ayşe': " + scores.containsKey("Ayşe"));
        System.out.println("default: " + scores.getOrDefault("Ayşe", 0));

        // İterasyon
        for (Map.Entry<String, Integer> e : scores.entrySet()) {
            System.out.println(" " + e.getKey() + " -> " + e.getValue());
        }

        // Değer güncelleme (fonksiyonel)
        scores.merge("Ali", 5, Integer::sum);                // Ali'nin skorunu +5
        scores.computeIfAbsent("Ayşe", k -> 70);             // yoksa ekle
        System.out.println("after merge/compute: " + scores);

        // TreeMap – sıralı key
        TreeMap<String, Integer> tm = new TreeMap<>(scores);
        System.out.println("TreeMap: " + tm);
        System.out.println("firstKey: " + tm.firstKey());

        // --- QUEUE / DEQUE ---
        Deque<Integer> stack = new ArrayDeque<>();  // Stack sınıfı LEGACY, bunu kullan
        stack.push(1); stack.push(2); stack.push(3);
        System.out.println("pop: " + stack.pop() + " peek: " + stack.peek());

        Queue<Integer> q = new ArrayDeque<>();
        q.offer(1); q.offer(2); q.offer(3);
        System.out.println("poll: " + q.poll() + " queue: " + q);

        PriorityQueue<Integer> pq = new PriorityQueue<>();
        pq.offer(5); pq.offer(1); pq.offer(3);
        while (!pq.isEmpty()) System.out.print(pq.poll() + " "); // 1 3 5
        System.out.println();

        // --- IMMUTABLE (Java 9+) ---
        List<Integer> immutable = List.of(1, 2, 3);
        try { immutable.add(4); } catch (UnsupportedOperationException e) {
            System.out.println("List.of() immutable!");
        }

        // --- ConcurrentModificationException ---
        List<Integer> nums = new ArrayList<>(List.of(1,2,3,4,5));
        try {
            for (int n : nums) if (n == 3) nums.remove(Integer.valueOf(3));
        } catch (ConcurrentModificationException e) {
            System.out.println("CME yakalandı — Iterator.remove veya removeIf kullan!");
        }
        nums.removeIf(n -> n == 3);
        System.out.println("after removeIf: " + nums);
    }
}
