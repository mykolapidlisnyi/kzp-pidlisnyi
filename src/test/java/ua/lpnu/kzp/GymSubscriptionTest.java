package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class GymSubscriptionTest {

    @Test
    void fromCsvCreatesAppropriateSubtypes() {
        GymSubscription premium = GymSubscription.fromCsv("Олексій Ковальчук;Full Unlimited;12;120;12000.00");
        GymSubscription standard = GymSubscription.fromCsv("Марія Бондаренко;Standard Gym;3;24;3200.00");

        assertInstanceOf(PremiumSubscription.class, premium);
        assertEquals(PlanCategory.PREMIUM, premium.getCategory());
        assertEquals(24, ((PremiumSubscription) premium).getBonusSessions());

        assertInstanceOf(StandardSubscription.class, standard);
        assertEquals(PlanCategory.STANDARD, standard.getCategory());
    }

    @Test
    void polymorphicEffectiveVisitCostDiffersBetweenSubtypes() {
        GymSubscription standard = new StandardSubscription("Іван", "Standard Gym", 1, 10, 1000.0);
        GymSubscription premium = new PremiumSubscription("Олена", "VIP Club", 1, 10, 1000.0, 10);

        // Standard: 1000 / 10 = 100.0
        assertEquals(100.0, standard.effectiveVisitCost(), 0.0001);
        // Premium: 1000 / (10 + 10) = 50.0
        assertEquals(50.0, premium.effectiveVisitCost(), 0.0001);

        List<GymSubscription> items = List.of(standard, premium);
        double totalEffective = items.stream().mapToDouble(GymSubscription::effectiveVisitCost).sum();
        assertEquals(150.0, totalEffective, 0.0001);
    }

    @Test
    void constructorRejectsInvalidFields() {
        assertThrows(IllegalArgumentException.class,
                () -> new StandardSubscription("", "Basic", 1, 10, 500.0));
        assertThrows(IllegalArgumentException.class,
                () -> new StandardSubscription("Іван", null, 1, 10, 500.0));
        assertThrows(IllegalArgumentException.class,
                () -> new StandardSubscription("Іван", "Basic", 0, 10, 500.0));
        assertThrows(IllegalArgumentException.class,
                () -> new StandardSubscription("Іван", "Basic", 1, -1, 500.0));
        assertThrows(IllegalArgumentException.class,
                () -> new StandardSubscription("Іван", "Basic", 1, 10, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> new PremiumSubscription("Іван", "VIP", 1, 10, 500.0, -2));
    }

    @Test
    void fromCsvRejectsMalformedLines() {
        assertThrows(IllegalArgumentException.class, () -> GymSubscription.fromCsv(null));
        assertThrows(IllegalArgumentException.class, () -> GymSubscription.fromCsv("   "));
        assertThrows(IllegalArgumentException.class, () -> GymSubscription.fromCsv("Клієнт;План;12"));
        assertThrows(IllegalArgumentException.class, () -> GymSubscription.fromCsv("Клієнт;План;abc;10;1000"));
        assertThrows(IllegalArgumentException.class, () -> GymSubscription.fromCsv("Клієнт;План;12;xyz;1000"));
        assertThrows(IllegalArgumentException.class, () -> GymSubscription.fromCsv("Клієнт;План;12;10;NaN"));
    }

    @Test
    void equalsAndHashCodeWorkInHashSet() {
        GymSubscription s1 = new StandardSubscription("Марія", "Standard", 3, 24, 3200.0);
        GymSubscription s2 = new StandardSubscription("Марія", "Standard", 3, 24, 3200.0);
        GymSubscription p1 = new PremiumSubscription("Марія", "Standard", 3, 24, 3200.0, 0);

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
        // Об'єкти різних класів не рівні навіть за однакових базових полів
        assertNotEquals(s1, p1);

        Set<GymSubscription> set = new HashSet<>();
        set.add(s1);
        set.add(s2);
        set.add(p1);
        assertEquals(2, set.size());
    }

    @Test
    void enumDisplaysUkrainianNames() {
        assertEquals("Стандартний", PlanCategory.STANDARD.getDisplayName());
        assertEquals("Преміум / VIP", PlanCategory.PREMIUM.getDisplayName());
    }
}