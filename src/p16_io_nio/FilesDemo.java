package p16_io_nio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;

public class FilesDemo {
    public static void main(String[] args) throws IOException {

        // Geçici bir dosya üret — demo için
        Path tmp = Files.createTempFile("java-core-", ".txt");
        System.out.println("dosya: " + tmp);

        // --- Tek satırda yazma ---
        Files.writeString(tmp, "Birinci satır\nİkinci satır\n", StandardCharsets.UTF_8);

        // --- Tek satırda okuma ---
        String all = Files.readString(tmp, StandardCharsets.UTF_8);
        System.out.println("içerik:\n" + all);

        // --- Satır satır (bellekte) ---
        List<String> lines = Files.readAllLines(tmp, StandardCharsets.UTF_8);
        lines.forEach(l -> System.out.println("  -> " + l));

        // --- Stream ile (BÜYÜK dosyalar için lazy) ---
        try (Stream<String> s = Files.lines(tmp, StandardCharsets.UTF_8)) {
            long nonEmpty = s.filter(l -> !l.isBlank()).count();
            System.out.println("non-empty satır sayısı: " + nonEmpty);
        }

        // --- BufferedWriter ile append ---
        try (BufferedWriter bw = Files.newBufferedWriter(
                tmp, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            bw.write("Üçüncü (append) satır");
            bw.newLine();
        }

        // --- BufferedReader ile satır satır oku ---
        try (BufferedReader br = Files.newBufferedReader(tmp, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("read: " + line);
            }
        }

        // --- Dosya bilgisi ---
        System.out.println("boyut: " + Files.size(tmp) + " byte");
        System.out.println("var mı? " + Files.exists(tmp));

        // --- Temizle ---
        Files.deleteIfExists(tmp);
        System.out.println("silindi mi? " + !Files.exists(tmp));

        // --- Dizin gezme (çalıştığın dizinin .java dosyaları) ---
        try (Stream<Path> walk = Files.walk(Path.of("."), 3)) {
            walk.filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".java"))
                .limit(5)
                .forEach(System.out::println);
        }
    }
}
