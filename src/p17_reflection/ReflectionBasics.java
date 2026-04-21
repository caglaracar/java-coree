package p17_reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;

/**
 * Reflection 101:
 *  - Class<?> elde etmenin 3 yolu
 *  - getFields vs getDeclaredFields
 *  - Constructor ile nesne üretme
 *  - private method çağırma
 *  - field okuma/yazma
 *  - annotation okuma
 *  - generic tip bilgisini okuma
 */
public class ReflectionBasics {

    public static void main(String[] args) throws Exception {
        // 1) Class<?> elde etme
        Class<Person> c1 = Person.class;
        Class<?> c2 = new Person("Ali", 30).getClass();
        Class<?> c3 = Class.forName("p17_reflection.Person");
        System.out.println("1) Ayni Class nesnesi mi? " + (c1 == c2 && c2 == c3));

        // 2) Metadata
        System.out.println("\n2) Metadata:");
        System.out.println("   name        = " + c1.getName());
        System.out.println("   simpleName  = " + c1.getSimpleName());
        System.out.println("   super       = " + c1.getSuperclass().getSimpleName());
        System.out.println("   isPublic?   = " + Modifier.isPublic(c1.getModifiers()));

        // 3) public vs declared
        System.out.println("\n3) getMethods vs getDeclaredMethods:");
        System.out.println("   getMethods         -> " + c1.getMethods().length + " metot (Object'ten miras dahil)");
        System.out.println("   getDeclaredMethods -> " + c1.getDeclaredMethods().length + " metot (yalniz Person)");

        // 4) Constructor ile nesne uretme (newInstance deprecated yerine)
        Constructor<Person> ctor = c1.getDeclaredConstructor(String.class, int.class);
        Person p = ctor.newInstance("Veli", 25);
        System.out.println("\n4) Constructor ile uretilen: " + p);

        // 5) Private metod cagirma
        Method secret = c1.getDeclaredMethod("secret", String.class);
        secret.setAccessible(true);
        String result = (String) secret.invoke(p, "DBG");
        System.out.println("\n5) Private secret() -> " + result);

        // 6) Field okuma/yazma
        Field ageField = c1.getDeclaredField("age");
        ageField.setAccessible(true);
        System.out.println("\n6) age once = " + ageField.get(p));
        ageField.setInt(p, 99);
        System.out.println("   age sonra = " + p.getAge());

        // 7) Annotation okuma
        System.out.println("\n7) @Info ile isaretli field'lar:");
        for (Field f : c1.getDeclaredFields()) {
            Info info = f.getAnnotation(Info.class);
            if (info != null) {
                System.out.println("   - " + f.getName()
                        + " label='" + info.label() + "'"
                        + " required=" + info.required());
            }
        }

        // 8) Generic tipi okuma (List<String> hobbies)
        Field hobbies = c1.getDeclaredField("hobbies");
        ParameterizedType pt = (ParameterizedType) hobbies.getGenericType();
        System.out.println("\n8) hobbies generic arg = " + pt.getActualTypeArguments()[0].getTypeName());
    }
}
