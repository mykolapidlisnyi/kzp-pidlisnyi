package ua.lpnu.kzp;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Набір декларативних потокових запитів (Stream API) для предметної області «Тренажерний зал» (Варіант 20).
 * Усі методи не змінюють вхідну колекцію і працюють із поліморфною ієрархією {@link GymSubscription}.
 */
public final class GymQueries {

    private GymQueries() {
    }

    /**
     * 1. Відбір (filter): повертає абонементи з кількістю відвідувань не менше заданого порогу.
     */
    public static List<GymSubscription> filterByMinVisits(List<GymSubscription> records, int minVisits) {
        if (minVisits < 0) {
            throw new IllegalArgumentException("Мінімальна кількість відвідувань не може бути від'ємною");
        }
        return streamOf(records)
                .filter(sub -> sub.getVisits() >= minVisits)
                .toList();
    }

    /**
     * 2. Перетворення (map): повертає впорядкований список унікальних імен клієнтів.
     */
    public static List<String> extractDistinctClients(List<GymSubscription> records) {
        return streamOf(records)
                .map(GymSubscription::getClient)
                .distinct()
                .sorted()
                .toList();
    }

    /**
     * 3а. Групування (groupingBy): групує абонементи за категорією {@link PlanCategory}.
     */
    public static Map<PlanCategory, List<GymSubscription>> groupByCategory(List<GymSubscription> records) {
        return streamOf(records)
                .collect(Collectors.groupingBy(GymSubscription::getCategory));
    }

    /**
     * 3б. Групування з підрахунком (groupingBy + counting): визначає кількість продажів кожного тарифного плану.
     */
    public static Map<String, Long> countByPlan(List<GymSubscription> records) {
        return streamOf(records)
                .collect(Collectors.groupingBy(
                        GymSubscription::getPlan,
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
    }

    /**
     * 4а. Зведена статистика відвідувань (summarizingInt).
     */
    public static IntSummaryStatistics visitsStatistics(List<GymSubscription> records) {
        return streamOf(records)
                .collect(Collectors.summarizingInt(GymSubscription::getVisits));
    }

    /**
     * 4б. Зведена статистика виторгу (summarizingDouble).
     */
    public static DoubleSummaryStatistics revenueStatistics(List<GymSubscription> records) {
        return streamOf(records)
                .collect(Collectors.summarizingDouble(GymSubscription::getPrice));
    }

    /**
     * 4в. Зведена поліморфна статистика ефективної вартості заняття (summarizingDouble).
     */
    public static DoubleSummaryStatistics effectiveVisitCostStatistics(List<GymSubscription> records) {
        return streamOf(records)
                .collect(Collectors.summarizingDouble(GymSubscription::effectiveVisitCost));
    }

    /**
     * 5. Топ-N (sorted + складений Comparator + limit):
     * сортує за тривалістю (months) за спаданням, далі за ціною (price) за спаданням, далі за клієнтом.
     */
    public static List<GymSubscription> topLongestSubscriptions(List<GymSubscription> records, int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Ліміт не може бути від'ємним (отримано " + limit + ")");
        }
        Comparator<GymSubscription> comparator = Comparator
                .comparingInt(GymSubscription::getMonths).reversed()
                .thenComparing(Comparator.comparingDouble(GymSubscription::getPrice).reversed())
                .thenComparing(GymSubscription::getClient);

        return streamOf(records)
                .sorted(comparator)
                .limit(limit)
                .toList();
    }

    /**
     * 6. Безпечний пошук (Optional): знаходить перший абонемент вказаного клієнта без урахування регістру.
     */
    public static Optional<GymSubscription> findByClient(List<GymSubscription> records, String clientName) {
        if (clientName == null || clientName.isBlank()) {
            return Optional.empty();
        }
        String target = clientName.trim();
        return streamOf(records)
                .filter(sub -> sub.getClient().equalsIgnoreCase(target))
                .findFirst();
    }

    private static Stream<GymSubscription> streamOf(List<GymSubscription> records) {
        return Objects.requireNonNull(records, "Список записів не може бути null").stream();
    }
}