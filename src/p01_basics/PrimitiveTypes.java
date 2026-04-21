package p01_basics;

/**
 * Bu sınıf Java'daki 8 primitive tipi, varsayılan değerlerini,
 * literal gösterimlerini ve sınır (overflow) davranışlarını gösterir.
 */
public class PrimitiveTypes {

    // Instance field'larda primitive tiplerin VARSAYILAN değerleri vardır.
    // Local değişkenlerde yoktur — başlatılmadan kullanılamaz.
    static byte    defaultByte;     // 0
    static short   defaultShort;    // 0
    static int     defaultInt;      // 0
    static long    defaultLong;     // 0L
    static float   defaultFloat;    // 0.0f
    static double  defaultDouble;   // 0.0d
    static char    defaultChar;     // '\u0000' (null karakter)
    static boolean defaultBool;     // false

    public static void main(String[] args) {

        // --- TAM SAYI TİPLERİ ---
        byte  b = 127;                  // -128..127
        short s = 32_000;
        int   i = 2_147_483_647;        // int max
        long  l = 9_000_000_000L;       // L eki ŞART (int'e sığmaz)

        // --- KESİRLİ TİPLER ---
        // double varsayılandır; float'ta 'f' eki zorunlu.
        float  f = 3.14f;
        double d = 3.141592653589793;

        // --- char ---
        // char aslında 16-bit işaretsiz bir tamsayıdır (UTF-16 code unit).
        char  c1 = 'A';
        char  c2 = 65;          // 65 = 'A'
        char  c3 = '\u0041';    // unicode
        int   codeOfA = 'A';    // char -> int otomatik widening

        // --- boolean ---
        boolean active = true;

        // --- Literal gösterimler ---
        int dec = 100;
        int bin = 0b0110_0100;  // 100
        int oct = 0144;         // 100
        int hex = 0x64;         // 100

        System.out.println("byte max  = " + Byte.MAX_VALUE);
        System.out.println("short max = " + Short.MAX_VALUE);
        System.out.println("int max   = " + Integer.MAX_VALUE);
        System.out.println("long max  = " + Long.MAX_VALUE);
        System.out.println("char='A'  -> int = " + codeOfA);
        System.out.println("bin==dec? " + (bin == dec));

        // --- OVERFLOW (Taşma) ---
        // Java overflow'da HATA ATMAZ, sessizce sarar (wrap-around).
        int overflow = Integer.MAX_VALUE + 1;
        System.out.println("MAX_VALUE + 1 = " + overflow); // negatif (wrap)

        // --- FLOAT PRECISION TUZAĞI ---
        // 0.1 + 0.2 == 0.3 değildir! (IEEE-754)
        System.out.println(0.1 + 0.2);                // 0.30000000000000004
        System.out.println((0.1 + 0.2) == 0.3);       // false
        // Para/finans için BigDecimal kullan!

        // --- CASTING ---
        int big = 130;
        byte narrow = (byte) big;   // -126 (veri kaybı — 8 bit'e sığmaz)
        System.out.println("130 -> byte = " + narrow);

        double pi = 3.99;
        int truncated = (int) pi;   // 3 (yuvarlamaz, keser!)
        System.out.println("(int)3.99 = " + truncated);

        // --- var (Java 10+) ---
        var message = "var ile tip çıkarımı";  // String
        var number  = 42;                      // int
        System.out.println(message + " | " + number);

        // Varsayılan değerler (static field'lar)
        System.out.println("Default int = " + defaultInt);
        System.out.println("Default bool = " + defaultBool);
        System.out.println("Default char code = " + (int) defaultChar);
    }
}
