package ua.lpnu.kzp;

/**
 * Незмінний запис (record) для збереження підсумкових показників обробки абонементів.
 *
 * @param totalRecords         кількість оброблених коректних записів
 * @param averageVisits        середня кількість відвідувань на один абонемент
 * @param totalRevenue         загальний виторг у гривнях
 * @param longestSubscription  текстовий опис найдовшого абонемента
 * @param mostPopularPlan      назва найпопулярнішого тарифного плану
 * @param mostPopularPlanCount кількість оформлень найпопулярнішого плану
 */
public record GymSummary(
        int totalRecords,
        double averageVisits,
        double totalRevenue,
        String longestSubscription,
        String mostPopularPlan,
        int mostPopularPlanCount
) {
    /**
     * Компактний конструктор для перевірки коректності обчислених показників.
     */
    public GymSummary {
        if (totalRecords < 0 || mostPopularPlanCount < 0) {
            throw new IllegalArgumentException("Кількість записів не може бути від'ємною");
        }
        if (!Double.isFinite(averageVisits) || averageVisits < 0.0
                || !Double.isFinite(totalRevenue) || totalRevenue < 0.0) {
            throw new IllegalArgumentException("Числові показники статистики мають бути невід'ємними та скінченними");
        }
    }
}