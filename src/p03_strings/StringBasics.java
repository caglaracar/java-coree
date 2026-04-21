package p03_strings;

public class StringBasics {
    public static void main(String[] args) {

        // --- LITERAL vs new ---
        String a = "java";
        String b = "java";
        String c = new String("java");

        System.out.println("a == b : " + (a == b));        // true (pool)
        System.out.println("a == c : " + (a == c));        // false (farklı nesne)
        System.out.println("a.equals(c) : " + a.equals(c));// true  (içerik)
        System.out.println("a == c.intern() : " + (a == c.intern())); // true

        // --- IMMUTABILITY ---
        String s = "hello";
        s.toUpperCase();          // sonucu ATMIYORUZ, s değişmez!
        System.out.println("s = " + s);  // "hello"

        s = s.toUpperCase();      // sonucu yakalamalıyız
        System.out.println("s = " + s);  // "HELLO"

        // --- YAYGIN METOTLAR ---
        String str = "  Merhaba Dünya  ";
        System.out.println("length:    " + str.length());
        System.out.println("trim:      [" + str.trim() + "]");
        System.out.println("strip:     [" + str.strip() + "]");
        System.out.println("upper:     " + str.toUpperCase());
        System.out.println("substring: " + str.trim().substring(0, 7));
        System.out.println("indexOf:   " + str.indexOf("Dünya"));
        System.out.println("replace:   " + str.replace("Dünya", "Java"));
        System.out.println("contains:  " + str.contains("Merhaba"));
        System.out.println("split:     ");
        for (String part : "a,b,c,d".split(",")) {
            System.out.println("  -> " + part);
        }
        System.out.println("join:      " + String.join("-", "2024", "01", "15"));
        System.out.println("format:    " + String.format("Ad:%-6s Yaş:%03d", "Ali", 7));
        System.out.println("repeat:    " + "-=".repeat(5));
        System.out.println("isBlank:   " + "   ".isBlank());
        System.out.println("compareTo: " + "apple".compareTo("banana")); // negatif

        // --- ESCAPE & TEXT BLOCKS ---
        String path = "C:\\Users\\Java";     // \\ ile ters bölü
        String quote = "O dedi ki: \"Java!\"";
        String textBlock = """
                {
                    "ad": "Ali",
                    "yas": 30
                }
                """;
        System.out.println(path);
        System.out.println(quote);
        System.out.println(textBlock);
    }
}
