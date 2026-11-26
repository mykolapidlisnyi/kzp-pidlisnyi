package ua.lpnu.kzp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Позначає поле або метод-геттер, який має бути експортований у CSV-файл.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface CsvColumn {
    /**
     * Заголовок стовпця у CSV.
     */
    String header();

    /**
     * Порядок слідування стовпців (за зростанням).
     */
    int order();
}