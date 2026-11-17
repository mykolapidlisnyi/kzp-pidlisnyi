package ua.lpnu.kzp;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Головний клас програми для лабораторної роботи № 1 (Варіант 20 - Тренажерний зал).
 * Здійснює зчитування CSV-файлу абонементів, валідацію полів та формування звіту.
 *
 * @author Підлісний Микола Олександрович
 * @version 1.0.0
 */
public class Main {

    public static final String VERSION = "1.0.0";
    public static final String STUDENT_NAME = "Підлісний Микола Олександрович";
    public static final int VARIANT = 20;

    /**
     * Запис для збереження валідованих даних про абонемент у тренажерному залі.
     */
    public record GymSubscription(String client, String plan, int months, int visits, double price) {}

    public static void main(String[] args) {
        String inputPath = "data/input.csv";
        String outputPath = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--help":
                    printHelp();
                    return;
                case "--version":
                    System.out.println("Версія програми: " + VERSION + " | Автор: " + STUDENT_NAME + " (Варіант " + VARIANT + " - Тренажерний зал)");
                    return;
                case "--input":
                    if (i + 1 < args.length) {
                        inputPath = args[++i];
                    } else {
                        System.err.println("Помилка: Не вказано шлях до вхідного файла після --input");
                        System.exit(2);
                    }
                    break;
                case "--output":
                    if (i + 1 < args.length) {
                        outputPath = args[++i];
                    } else {
                        System.err.println("Помилка: Не вказано шлях до вихідного файла після --output");
                        System.exit(2);
                    }
                    break;
                default:
                    System.err.println("Помилка: Неочікуваний аргумент " + args[i]);
                    printHelp();
                    System.exit(2);
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

        List<GymSubscription> validRecords = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split(";", -1);
                if (parts.length < 5) {
                    System.err.printf("Рядок %d пропущено: Недостатньо полів (очікується 5, отримано %d)%n", lineNumber, parts.length);
                    continue;
                }

                String client = parts[0].trim();
                String plan = parts[1].trim();
                String monthsStr = parts[2].trim();
                String visitsStr = parts[3].trim();
                String priceStr = parts[4].trim();

                if (client.isEmpty() || plan.isEmpty()) {
                    System.err.printf("Рядок %d пропущено: Ім'я клієнта та назва плану не можуть бути порожніми%n", lineNumber);
                    continue;
                }

                int months;
                try {
                    months = Integer.parseInt(monthsStr);
                    if (months <= 0) {
                        System.err.printf("Рядок %d пропущено: Тривалість місяців повинна бути більше 0 (отримано %d)%n", lineNumber, months);
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.err.printf("Рядок %d пропущено: Помилка розбору кількості місяців '%s'%n", lineNumber, monthsStr);
                    continue;
                }

                int visits;
                try {
                    visits = Integer.parseInt(visitsStr);
                    if (visits < 0) {
                        System.err.printf("Рядок %d пропущено: Кількість відвідувань не може бути від'ємною (отримано %d)%n", lineNumber, visits);
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.err.printf("Рядок %d пропущено: Помилка розбору кількості відвідувань '%s'%n", lineNumber, visitsStr);
                    continue;
                }

                double price;
                try {
                    price = Double.parseDouble(priceStr);
                    if (!Double.isFinite(price) || price <= 0.0) {
                        System.err.printf("Рядок %d пропущено: Ціна повинна бути скінченним числом > 0.0 (отримано %s)%n", lineNumber, priceStr);
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.err.printf("Рядок %d пропущено: Помилка розбору ціни '%s'%n", lineNumber, priceStr);
                    continue;
                }

                validRecords.add(new GymSubscription(client, plan, months, visits, price));
            }
        } catch (IOException e) {
            System.err.println("Помилка читання файла: " + e.getMessage());
            System.exit(1);
        }

        String report = generateReport(validRecords);
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

    /**
     * Формує підсумковий звіт згідно з 4 показниками Варіанта 20.
     *
     * @param records список коректних абонементів
     * @return сформований текст звіту
     */
    public static String generateReport(List<GymSubscription> records) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "=== ЗВІТ ОБРОБКИ ДАНИХ (Варіант 20: Тренажерний зал) ===%n"));
        sb.append(String.format(Locale.ROOT, "Студент: %s%n", STUDENT_NAME));
        sb.append("---------------------------------------\n");

        if (records.isEmpty()) {
            sb.append("Не знайдено жодного коректного запису для обробки.\n");
            return sb.toString();
        }

        int totalValidRecords = records.size();
        int totalVisits = 0;
        double totalRevenue = 0.0;
        
        int maxMonths = -1;
        String longestSubscriptionInfo = "";

        Map<String, Integer> planCounts = new LinkedHashMap<>();

        for (GymSubscription sub : records) {
            totalVisits += sub.visits();
            totalRevenue += sub.price();

            if (sub.months() > maxMonths) {
                maxMonths = sub.months();
                longestSubscriptionInfo = String.format("%s (%s, %d міс.)", sub.client(), sub.plan(), sub.months());
            }

            planCounts.put(sub.plan(), planCounts.getOrDefault(sub.plan(), 0) + 1);
        }

        double averageVisits = (double) totalVisits / totalValidRecords;

        String mostPopularPlan = "";
        int maxPlanCount = -1;
        for (Map.Entry<String, Integer> entry : planCounts.entrySet()) {
            if (entry.getValue() > maxPlanCount) {
                maxPlanCount = entry.getValue();
                mostPopularPlan = entry.getKey();
            }
        }

        sb.append(String.format(Locale.ROOT, "1. Середня кількість відвідувань: %.2f%n", averageVisits));
        sb.append(String.format(Locale.ROOT, "2. Загальний виторг: %.2f грн%n", totalRevenue));
        sb.append(String.format(Locale.ROOT, "3. Найдовший абонемент: %s%n", longestSubscriptionInfo));
        sb.append(String.format(Locale.ROOT, "4. Найпопулярніший абонемент: %s (куплено: %d)%n", mostPopularPlan, maxPlanCount));
        sb.append("---------------------------------------\n");

        return sb.toString();
    }
        /**
        * Виводить довідку щодо доступних параметрів командного рядка.
        */
        private static void printHelp() {
            System.out.println("Використання: java -jar target/lab01-pidlisnyi-1.0.0-all.jar [опції]");
            System.out.println("Опції:");
            System.out.println("  --help            Показати цю довідку");
            System.out.println("  --version         Вивести версію програми та інформацію про автора");
            System.out.println("  --input <path>    Вказати шлях до вхідного CSV-файла (за замовчуванням: data/input.csv)");
            System.out.println("  --output <path>   Вказати шлях для збереження вихідного звіту");
        }
}