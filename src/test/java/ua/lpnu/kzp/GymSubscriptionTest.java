package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GymSubscriptionTest {

    @Test
    void fromCsvCreatesValidEntity() {
        GymSubscription sub = GymSubscription.fromCsv("Олексій Ковальчук;Full Unlimited;12;120;12000.00");

        assertEquals("Олексій Ковальчук", sub.getClient());
        assertEquals("Full Unlimited", sub.getPlan());
        assertEquals(12, sub.getMonths());
        assertEquals(120, sub.getVisits());
        assertEquals(12000.00, sub.getPrice(), 0.0001);
    }

    @Test
    void constructorRejectsInvalidFields() {
        assertThrows(IllegalArgumentException.class,
                () -> new GymSubscription("", "Basic", 1, 10, 500.0));
        assertThrows(IllegalArgumentException.class,
                () -> new GymSubscription("Іван", null, 1, 10, 500.0));
        assertThrows(IllegalArgumentException.class,
                () -> new GymSubscription("Іван", "Basic", 0, 10, 500.0));
        assertThrows(IllegalArgumentException.class,
                () -> new GymSubscription("Іван", "Basic", 1, -1, 500.0));
        assertThrows(IllegalArgumentException.class,
                () -> new GymSubscription("Іван", "Basic", 1, 10, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> new GymSubscription("Іван", "Basic", 1, 10, Double.NaN));
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
    void toStringFormatsWithLocaleRoot() {
        GymSubscription sub = new GymSubscription("Марія", "Standard", 3, 24, 3200.50);
        assertEquals("Марія (Standard, 3 міс., 24 відв., 3200.50 грн)", sub.toString());
    }

    @Test
    void summaryRecordValidatesAndSupportsValueEquality() {
        GymSummary s1 = new GymSummary(4, 101.0, 43000.0, "Тарас (Annual, 24 міс.)", "Standard", 2);
        GymSummary s2 = new GymSummary(4, 101.0, 43000.0, "Тарас (Annual, 24 міс.)", "Standard", 2);

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());

        assertThrows(IllegalArgumentException.class,
                () -> new GymSummary(-1, 10.0, 100.0, "a", "b", 1));
    }
}