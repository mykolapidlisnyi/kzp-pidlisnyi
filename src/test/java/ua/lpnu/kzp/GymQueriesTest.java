package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class GymQueriesTest {

    private final List<GymSubscription> sampleData = List.of(
            new PremiumSubscription("Олексій Ковальчук", "Full Unlimited", 12, 120, 12000.0, 24),
            new StandardSubscription("Марія Бондаренко", "Standard Gym", 3, 24, 3200.0),
            new StandardSubscription("Олена Кравченко", "Standard Gym", 6, 50, 5800.0),
            new PremiumSubscription("Тарас Шевченко", "Premium Annual", 24, 210, 22000.0, 48),
            new StandardSubscription("Ігор Савченко", "Standard Gym", 6, 40, 6500.0)
    );

    @Test
    void filterByMinVisitsReturnsMatchingSubscriptions() {
        List<GymSubscription> active = GymQueries.filterByMinVisits(sampleData, 50);
        assertEquals(3, active.size());
        assertTrue(active.stream().allMatch(s -> s.getVisits() >= 50));

        assertThrows(IllegalArgumentException.class, () -> GymQueries.filterByMinVisits(sampleData, -1));
    }

    @Test
    void extractDistinctClientsReturnsSortedUniqueNames() {
        List<GymSubscription> withDuplicates = List.of(
                new StandardSubscription("Тарас", "Standard", 3, 20, 3000.0),
                new StandardSubscription("Андрій", "Standard", 1, 10, 1200.0),
                new PremiumSubscription("Тарас", "VIP", 12, 100, 10000.0, 24)
        );
        assertEquals(List.of("Андрій", "Тарас"), GymQueries.extractDistinctClients(withDuplicates));
    }

    @Test
    void groupByCategoryAndCountByPlanWorkCorrectly() {
        Map<PlanCategory, List<GymSubscription>> byCategory = GymQueries.groupByCategory(sampleData);
        assertEquals(2, byCategory.get(PlanCategory.PREMIUM).size());
        assertEquals(3, byCategory.get(PlanCategory.STANDARD).size());

        Map<String, Long> byPlan = GymQueries.countByPlan(sampleData);
        assertEquals(3L, byPlan.get("Standard Gym"));
        assertEquals(1L, byPlan.get("Full Unlimited"));
        assertEquals(1L, byPlan.get("Premium Annual"));
    }

    @Test
    void statisticsMatchManualCalculation() {
        IntSummaryStatistics visits = GymQueries.visitsStatistics(sampleData);
        assertEquals(5, visits.getCount());
        assertEquals(444, visits.getSum());
        assertEquals(24, visits.getMin());
        assertEquals(210, visits.getMax());

        DoubleSummaryStatistics revenue = GymQueries.revenueStatistics(sampleData);
        assertEquals(49500.0, revenue.getSum(), 0.001);

        DoubleSummaryStatistics effective = GymQueries.effectiveVisitCostStatistics(sampleData);
        assertTrue(effective.getAverage() > 0.0);
    }

    @Test
    void topLongestSubscriptionsUsesCompoundComparator() {
        // Очікуємо: 1) 24 міс., 2) 12 міс., 3) 6 міс. за ціною 6500 (Ігор), далі 6 міс. за ціною 5800 (Олена)
        List<GymSubscription> top4 = GymQueries.topLongestSubscriptions(sampleData, 4);
        assertEquals(4, top4.size());
        assertEquals("Тарас Шевченко", top4.get(0).getClient());
        assertEquals("Олексій Ковальчук", top4.get(1).getClient());
        assertEquals("Ігор Савченко", top4.get(2).getClient());
        assertEquals("Олена Кравченко", top4.get(3).getClient());

        assertEquals(0, GymQueries.topLongestSubscriptions(sampleData, 0).size());
        assertThrows(IllegalArgumentException.class, () -> GymQueries.topLongestSubscriptions(sampleData, -2));
    }

    @Test
    void findByClientReturnsOptionalAndHandlesMissing() {
        Optional<GymSubscription> found = GymQueries.findByClient(sampleData, "  тарас шевченко ");
        assertTrue(found.isPresent());
        assertEquals("Premium Annual", found.get().getPlan());

        assertTrue(GymQueries.findByClient(sampleData, "Невідомий Клієнт").isEmpty());
        assertTrue(GymQueries.findByClient(sampleData, "   ").isEmpty());
    }

    @Test
    void queriesDoNotMutateInputAndHandleEmptyList() {
        List<GymSubscription> snapshot = List.copyOf(sampleData);

        GymQueries.filterByMinVisits(sampleData, 10);
        GymQueries.extractDistinctClients(sampleData);
        GymQueries.groupByCategory(sampleData);
        GymQueries.countByPlan(sampleData);
        GymQueries.topLongestSubscriptions(sampleData, 3);
        GymQueries.findByClient(sampleData, "Тарас Шевченко");

        assertEquals(snapshot, sampleData);

        // Перевірка порожнього входу
        List<GymSubscription> empty = List.of();
        assertTrue(GymQueries.filterByMinVisits(empty, 10).isEmpty());
        assertTrue(GymQueries.extractDistinctClients(empty).isEmpty());
        assertTrue(GymQueries.groupByCategory(empty).isEmpty());
        assertEquals(0, GymQueries.visitsStatistics(empty).getCount());
        assertTrue(GymQueries.topLongestSubscriptions(empty, 5).isEmpty());
        assertTrue(GymQueries.findByClient(empty, "Будь-хто").isEmpty());
    }
}