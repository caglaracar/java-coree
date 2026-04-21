/**
 * Java Core Rehberi — giriş noktası.
 *
 * Rehberin tamamı 16 pakete ayrılmıştır. Her paket içinde:
 *   - NOTES.md : konunun teorik anlatımı (+ mülakat soruları)
 *   - .java    : çalıştırabileceğin örnekler
 *
 * Başlamak için: README.md (proje kökünde) dosyasını aç.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("== Java Core Rehberi ==");
        System.out.println("README.md dosyasını açarak başla.");
        System.out.println();
        System.out.println("Paketler:");
        String[] pkgs = {
            "p01_basics                - JVM/JDK/JRE, primitive types, casting",
            "p02_operators_controlflow - operatörler, if/else, switch, loops",
            "p03_strings               - String, pool, StringBuilder",
            "p04_memory                - stack/heap, static, pass-by-value",
            "p05_oop_classes           - class, object, constructor, this",
            "p06_inheritance           - extends, super, Object",
            "p07_polymorphism          - overload/override, dynamic dispatch",
            "p08_abstract_interface    - abstract vs interface",
            "p09_encapsulation_access  - access modifiers, final, immutability",
            "p10_exceptions            - checked/unchecked, try-with-resources",
            "p11_collections           - List, Set, Map, equals/hashCode",
            "p12_generics              - generics, PECS, type erasure",
            "p13_multithreading        - Thread, sync, Executor, CompletableFuture",
            "p14_gc                    - Garbage Collector, reference tipleri",
            "p15_lambda_streams        - lambda, Stream API, Optional",
            "p16_io_nio                - Files, Path, I/O",
            "p17_reflection           - Class<?>, annotation, dynamic proxy",
        };
        System.out.println();
        System.out.println("Uygulama projeleri: project_1 (rent a car), project_2 (inheritance), project_3 (doctors)");
        for (String p : pkgs) System.out.println("  - " + p);
    }
}