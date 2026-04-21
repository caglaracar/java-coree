package p17_reflection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * java.lang.reflect.Proxy ile interface'e runtime'da logging proxy üretimi.
 * Spring AOP, Mockito, transaction/sarmalama kütüphanelerinin temel taşı.
 */
public class DynamicProxyDemo {

    public interface Greeter {
        String hello(String name);
        int length(String s);
    }

    public static class RealGreeter implements Greeter {
        public String hello(String name) { return "Merhaba, " + name; }
        public int length(String s)      { return s.length(); }
    }

    /** Herhangi bir interface'i log ile sarmalayan yardimci. */
    @SuppressWarnings("unchecked")
    public static <T> T wrapWithLogging(T target, Class<T> iface) {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                long t0 = System.nanoTime();
                System.out.println("-> " + method.getName() + "(" + (args == null ? "" : join(args)) + ")");
                try {
                    Object ret = method.invoke(target, args);
                    System.out.println("<- " + method.getName() + " = " + ret
                            + "  (" + (System.nanoTime() - t0) / 1000 + " us)");
                    return ret;
                } catch (java.lang.reflect.InvocationTargetException ite) {
                    throw ite.getCause(); // gercek hatayi olduğu gibi fırlat
                }
            }
        };
        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[]{ iface },
                handler);
    }

    private static String join(Object[] a) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(a[i]);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        Greeter real = new RealGreeter();
        Greeter proxy = wrapWithLogging(real, Greeter.class);

        System.out.println(proxy.hello("Dunya"));
        System.out.println("uzunluk = " + proxy.length("reflection"));
    }
}
