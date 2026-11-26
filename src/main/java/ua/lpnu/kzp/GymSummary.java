package ua.lpnu.kzp;

/**
 * Незмінний запис для збереження підсумкових показників обробки абонементів.
 */
public record GymSummary(
        int totalRecords,
        double averageVisits,
        double totalRevenue,
        String longestSubscription,
        String mostPopularPlan,
        int mostPopularPlanCount,
        double averageEffectiveVisitCost
) {
    public GymSummary {
        if (totalRecords < 0 || mostPopularPlanCount < 0) {
            throw new IllegalArgumentException("Кількість записів не може бути від'ємною");
        }
        if (!Double.isFinite(averageVisits) || averageVisits < 0.0
                || !Double.isFinite(totalRevenue) || totalRevenue < 0.0
                || !Double.isFinite(averageEffectiveVisitCost) || averageEffectiveVisitCost < 0.0) {
            throw new IllegalArgumentException("Числові показники статистики мають бути невід'ємними та скінченними");
        }
    }
}