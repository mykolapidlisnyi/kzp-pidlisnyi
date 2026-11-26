package ua.lpnu.kzp;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Головний клас застосунку для лабораторної роботи № 4 (Варіант 20 — Тренажерний зал).
 * Використовує декларативні потокові запити {@link GymQueries} (Stream API) для обчислення звіту.
 *
 * @author Підлісний Микола Олександрович
 * @version 4.0.0
 */
public class Main {

    public static final String VERSION = "4.0.0";
    public static final String STUDENT_NAME = "Підлісний Микола Олександрович";
    public static final int VARIANT = 20;

    public static void main(String[] args) {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));

        String inputPath = "data/input.csv";
        String outputPath = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--help" -> {
                    printHelp();
                    return;
                }
                case "--version" -> {
                    System.out.println("Версія програми: " + VERSION + " | Автор: " + STUDENT_NAME + " (Варіант " + VARIANT + " - Тренажерний зал)");
                    return;
                }
                case "--input" -> {
                    if (i + 1 < args.length) {
                        inputPath = args[++i];
                    } else {
                        System.err.println("Помилка: Не вказано шлях до вхідного файла після --input");
                        System.exit(2);
                    }
                }
                case "--output" -> {
                    if (i + 1 < args.length) {
                        outputPath = args[++i];
                    } else {
                        System.err.println("Помилка: Не вказано шлях до вихідного файла після --output");
                        System.exit(2);
                    }
                }
                default -> {
                    System.err.println("Помилка: Неочікуваний аргумент " + args[i]);
                    printHelp();
                    System.exit(2);
                }
            }
        }

        Path input = Path.of(inputPath);
        if (!Files.exists(input)) {
            System.err.println("Помилка: Файл не знайдено за шляхом: " + input.toAbsolutePath());
            System.exit(1);
        }

        if (outputPath != null) {
            Path output = Path.of(outputPath);
            if (input.toAbsolutePath().normalize().equals(output.toAbsolutePath().normalize())) {
                System.err.println("Помилка: Вихідний файл не може збігатися з вхідним CSV-файлом!");
                System.exit(1);
            }
        }

        List<GymSubscription> validRecords = readSubscriptions(input);
        GymSummary summary = calculateSummary(validRecords);
        String report = formatReport(summary);

        System.out.print(report);

        if (outputPath != null) {
            try {
                Path output = Path.of(outputPath);
                Path parent = output.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Files.writeString(output, report, StandardCharsets.UTF_8);
                System.out.println("Звіт успішно збережено у файл: " + output.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Помилка запису вихідного файла: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    private static List<GymSubscription> readSubscriptions(Path input) {
        List<GymSubscription> records = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                try {
                    records.add(GymSubscription.fromCsv(line));
                } catch (IllegalArgumentException e) {
                    System.err.printf("Рядок %d пропущено: %s%n", lineNumber, e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Помилка читання файла: " + e.getMessage());
            System.exit(1);
        }

        return records;
    }

    /**
     * Обчислює підсумкові показники виключно декларативними запитами Stream API без ручних циклів.
     */
    public static GymSummary calculateSummary(List<GymSubscription> records) {
        if (records == null || records.isEmpty()) {
            return new GymSummary(0, 0.0, 0.0, "немає", "немає", 0, 0.0);
        }

        IntSummaryStatistics visitsStats = GymQueries.visitsStatistics(records);
        DoubleSummaryStatistics revenueStats = GymQueries.revenueStatistics(records);
        DoubleSummaryStatistics effectiveCostStats = GymQueries.effectiveVisitCostStatistics(records);

        String longestSubscriptionInfo = GymQueries.topLongestSubscriptions(records, 1).stream()
                .findFirst()
                .map(sub -> String.format(Locale.ROOT, "%s (%s, %d міс.)",
                        sub.getClient(), sub.getPlan(), sub.getMonths()))
                .orElse("немає");

        var popularPlanEntry = GymQueries.countByPlan(records).entrySet().stream()
                .max(Map.Entry.comparingByValue());

        String mostPopularPlan = popularPlanEntry.map(Map.Entry::getKey).orElse("немає");
        int maxPlanCount = popularPlanEntry.map(entry -> Math.toIntExact(entry.getValue())).orElse(0);

        return new GymSummary(
                records.size(),
                visitsStats.getAverage(),
                revenueStats.getSum(),
                longestSubscriptionInfo,
                mostPopularPlan,
                maxPlanCount,
                effectiveCostStats.getAverage()
        );
    }

    /**
     * Формує текстовий звіт (повністю зберігає формат попередніх робіт).
     */
    public static String formatReport(GymSummary summary) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "=== ЗВІТ ОБРОБКИ ДАНИХ (Варіант 20: Тренажерний зал) ===%n"));
        sb.append(String.format(Locale.ROOT, "Студент: %s%n", STUDENT_NAME));
        sb.append("---------------------------------------\n");

        if (summary.totalRecords() == 0) {
            sb.append("Не знайдено жодного коректного запису для обробки.\n");
            return sb.toString();
        }

        sb.append(String.format(Locale.ROOT, "1. Середня кількість відвідувань: %.2f%n", summary.averageVisits()));
        sb.append(String.format(Locale.ROOT, "2. Загальний виторг: %.2f грн%n", summary.totalRevenue()));
        sb.append(String.format(Locale.ROOT, "3. Найдовший абонемент: %s%n", summary.longestSubscription()));
        sb.append(String.format(Locale.ROOT, "4. Найпопулярніший абонемент: %s (куплено: %d)%n",
                summary.mostPopularPlan(), summary.mostPopularPlanCount()));
        sb.append(String.format(Locale.ROOT, "5. Середня ефективна вартість заняття: %.2f грн%n",
                summary.averageEffectiveVisitCost()));
        sb.append("---------------------------------------\n");

        return sb.toString();
    }

    private static void printHelp() {
        System.out.println("Використання: java -jar target/lab01-pidlisnyi-1.0.0-all.jar [опції]");
        System.out.println("Опції:");
        System.out.println("  --help            Показати цю довідку");
        System.out.println("  --version         Вивести версію програми та інформацію про автора");
        System.out.println("  --input <path>    Вказати шлях до вхідного CSV-файла (за замовчуванням: data/input.csv)");
        System.out.println("  --output <path>   Вказати шлях для збереження вихідного звіту");
    }
}