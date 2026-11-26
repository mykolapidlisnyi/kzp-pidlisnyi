package ua.lpnu.kzp;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Експортує об'єкти у формат CSV (роздільник ';') за допомогою анотації {@link CsvColumn} та Reflection API.
 */
public final class ReflectionCsvExporter {

    private ReflectionCsvExporter() {
    }

    /**
     * Формує список CSV-рядків для переданої колекції об'єктів.
     */
    public static <T> List<String> toCsvLines(List<T> items, Class<T> itemType, boolean includeHeader)
            throws CsvExportException {
        Objects.requireNonNull(items, "Список об'єктів не може бути null");
        Objects.requireNonNull(itemType, "Клас об'єктів не може бути null");

        List<Field> annotatedFields = collectAnnotatedFields(itemType);
        if (annotatedFields.isEmpty()) {
            throw new CsvExportException("Клас " + itemType.getSimpleName() + " не містить полів із @CsvColumn");
        }

        List<String> lines = new ArrayList<>();
        if (includeHeader) {
            String headerLine = annotatedFields.stream()
                    .map(f -> f.getAnnotation(CsvColumn.class).header())
                    .collect(Collectors.joining(";"));
            lines.add(headerLine);
        }

        for (T item : items) {
            if (item == null) {
                throw new CsvExportException("Колекція містить null-елемент");
            }
            List<String> values = new ArrayList<>(annotatedFields.size());
            for (Field field : annotatedFields) {
                try {
                    Object rawValue = field.get(item);
                    values.add(formatValue(rawValue));
                } catch (IllegalAccessException e) {
                    throw new CsvExportException("Не вдалося прочитати поле " + field.getName(), e);
                }
            }
            lines.add(String.join(";", values));
        }

        return lines;
    }

    /**
     * Записує об'єкти у CSV-файл із явним кодуванням UTF-8.
     */
    public static <T> void exportToFile(Path target, List<T> items, Class<T> itemType, boolean includeHeader)
            throws CsvExportException {
        Objects.requireNonNull(target, "Шлях до файла не може бути null");
        List<String> lines = toCsvLines(items, itemType, includeHeader);
        try {
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(target, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CsvExportException("Помилка запису CSV-файла: " + target, e);
        }
    }

    private static List<Field> collectAnnotatedFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(CsvColumn.class)) {
                    field.setAccessible(true);
                    fields.add(field);
                }
            }
        }
        fields.sort(Comparator.comparingInt(f -> f.getAnnotation(CsvColumn.class).order()));
        return fields;
    }

    private static String formatValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Double d) {
            return String.format(Locale.ROOT, "%.2f", d);
        }
        return value.toString();
    }
}