package p17_reflection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Mini "JUnit-vari" runner:
 *  - @MyTest ile isaretli no-arg metotlari bulur
 *  - reflection ile instantiate + invoke eder
 *  - InvocationTargetException uzerinden gercek hatayi raporlar
 *
 *  Koşturmak için:  main()
 */
public class MiniTestRunner {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface MyTest {}

    // --- Test edilen sınıf ---
    public static class Calculator {
        public int add(int a, int b) { return a + b; }
        public int div(int a, int b) { return a / b; }
    }

    // --- Test sınıfı (no-arg ctor sart) ---
    public static class CalculatorTests {
        private final Calculator c = new Calculator();

        @MyTest
        public void addsTwoPositives() {
            assertEq(5, c.add(2, 3));
        }

        @MyTest
        public void addsNegatives() {
            assertEq(-7, c.add(-3, -4));
        }

        @MyTest
        public void divByZeroShouldFail() {
            c.div(1, 0); // kasten patlar -> hata olarak raporlanmali
        }

        private static void assertEq(int expected, int actual) {
            if (expected != actual) {
                throw new AssertionError("expected " + expected + " but was " + actual);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> testClass = CalculatorTests.class;
        Object instance = testClass.getDeclaredConstructor().newInstance();

        int pass = 0, fail = 0;
        for (Method m : testClass.getDeclaredMethods()) {
            if (!m.isAnnotationPresent(MyTest.class)) continue;
            m.setAccessible(true);
            try {
                m.invoke(instance);
                System.out.println("[PASS] " + m.getName());
                pass++;
            } catch (InvocationTargetException ite) {
                // Gerçek hata burada sarmalanir
                Throwable cause = ite.getCause();
                System.out.println("[FAIL] " + m.getName() + " -> "
                        + cause.getClass().getSimpleName() + ": " + cause.getMessage());
                fail++;
            }
        }
        System.out.println("\nSonuc: " + pass + " pass, " + fail + " fail");
    }
}
