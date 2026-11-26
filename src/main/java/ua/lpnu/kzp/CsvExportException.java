package ua.lpnu.kzp;

/**
 * Виняток, що сигналізує про помилку під час рефлексивного експорту або запису CSV.
 */
public class CsvExportException extends Exception {

    public CsvExportException(String message) {
        super(message);
    }

    public CsvExportException(String message, Throwable cause) {
        super(message, cause);
    }
}