package generators;

import com.github.curiousoddman.rgxgen.RgxGen;

import java.lang.reflect.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Генератор случайных моделей на основе структуры Java-класса.
 */
public class RandomModelGenerator {

    private static final Random RANDOM = new Random();

    /**
     * Главный статический метод — генерирует объект любого класса.
     */
    public static <T> T generate(Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();

            for (Field field : getAllFields(clazz)) {
                field.setAccessible(true);

                Object value;
                GeneratingRule rule = field.getAnnotation(GeneratingRule.class);

                if (rule != null) {
                    value = generateFromRegex(rule.regex(), field.getType());
                } else {
                    value = generateRandomValue(field);
                }

                field.set(instance, value);
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate entity for class: " + clazz.getName(), e);
        }
    }

    /**
     * Генерация по regex через RgxGen.
     */
    private static Object generateFromRegex(String regex, Class<?> type) {
        RgxGen rgxGen = new RgxGen(regex);
        String generated = rgxGen.generate();

        if (type.equals(String.class)) return generated;
        if (type.equals(Integer.class) || type.equals(int.class)) return Integer.parseInt(generated);
        if (type.equals(Long.class) || type.equals(long.class)) return Long.parseLong(generated);
        if (type.equals(Double.class) || type.equals(double.class)) return Double.parseDouble(generated);
        if (type.equals(Boolean.class) || type.equals(boolean.class)) return Boolean.parseBoolean(generated);

        return generated;
    }

    /**
     * Генерация случайного значения по типу поля.
     */
    private static Object generateRandomValue(Field field) {
        Class<?> type = field.getType();

        // Примитивы и простые типы
        if (type.equals(String.class)) return "str_" + UUID.randomUUID().toString().substring(0, 8);
        if (type.equals(Integer.class) || type.equals(int.class)) return RANDOM.nextInt(1000);
        if (type.equals(Long.class) || type.equals(long.class)) return Math.abs(RANDOM.nextLong() % 10000);
        if (type.equals(Double.class) || type.equals(double.class)) return RANDOM.nextDouble() * 100;
        if (type.equals(Boolean.class) || type.equals(boolean.class)) return RANDOM.nextBoolean();
        if (type.equals(LocalDate.class)) return LocalDate.now().minusDays(RANDOM.nextInt(1000));
        if (type.equals(UUID.class)) return UUID.randomUUID();

        // Enum
        if (type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            return constants[RANDOM.nextInt(constants.length)];
        }

        // Коллекции
        if (Collection.class.isAssignableFrom(type)) {
            return generateCollection(field);
        }

        // Map
        if (Map.class.isAssignableFrom(type)) {
            return generateMap(field);
        }

        // Вложенные классы
        if (!type.isPrimitive() && !type.getName().startsWith("java.")) {
            return generate(type);
        }

        return null;
    }

    /**
     * Генерация списка или множества.
     */
    private static Collection<?> generateCollection(Field field) {
        Type genericType = field.getGenericType();
        int size = RANDOM.nextInt(5) + 1;

        Class<?> rawType = (Class<?>) ((ParameterizedType) genericType).getRawType();
        Class<?> elementType = extractGenericParameterType(genericType, 0);

        Collection<Object> collection;
        if (Set.class.isAssignableFrom(rawType)) {
            collection = new HashSet<>();
        } else {
            collection = new ArrayList<>();
        }

        for (int i = 0; i < size; i++) {
            collection.add(generateValueForType(elementType));
        }

        return collection;
    }

    /**
     * Генерация Map<K,V>.
     */
    private static Map<?, ?> generateMap(Field field) {
        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType)) return Collections.emptyMap();

        Class<?> keyType = extractGenericParameterType(genericType, 0);
        Class<?> valueType = extractGenericParameterType(genericType, 1);

        int size = RANDOM.nextInt(3) + 1;
        Map<Object, Object> map = new HashMap<>();

        for (int i = 0; i < size; i++) {
            Object key = generateValueForType(keyType);
            Object value = generateValueForType(valueType);
            map.put(key, value);
        }

        return map;
    }

    /**
     * Генерация значения по типу (для коллекций и map).
     */
    private static Object generateValueForType(Class<?> type) {
        try {
            if (type.equals(String.class)) return "val_" + UUID.randomUUID().toString().substring(0, 6);
            if (type.equals(Integer.class) || type.equals(int.class)) return RANDOM.nextInt(100);
            if (type.equals(Long.class) || type.equals(long.class)) return Math.abs(RANDOM.nextLong() % 10000);
            if (type.equals(Double.class) || type.equals(double.class)) return RANDOM.nextDouble() * 50;
            if (type.equals(Boolean.class) || type.equals(boolean.class)) return RANDOM.nextBoolean();
            if (type.equals(LocalDate.class)) return LocalDate.now().minusDays(RANDOM.nextInt(300));
            if (type.isEnum()) {
                Object[] constants = type.getEnumConstants();
                return constants[RANDOM.nextInt(constants.length)];
            }
            if (!type.isPrimitive() && !type.getName().startsWith("java.")) {
                return generate(type);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate value for type: " + type, e);
        }
        return null;
    }

    /**
     * Извлечение generic-параметра типа.
     */
    private static Class<?> extractGenericParameterType(Type genericType, int index) {
        if (genericType instanceof ParameterizedType) {
            Type[] actualTypeArgs = ((ParameterizedType) genericType).getActualTypeArguments();
            if (actualTypeArgs.length > index && actualTypeArgs[index] instanceof Class<?>) {
                return (Class<?>) actualTypeArgs[index];
            }
        }
        return Object.class;
    }

    /**
     * Получение всех полей класса, включая наследуемые.
     */
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields.stream()
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .collect(Collectors.toList());
    }
}
