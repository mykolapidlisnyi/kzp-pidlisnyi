package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void testCalculateSummaryAndFormatReportWithValidGymData() {
        List<GymSubscription> records = List.of(
                new GymSubscription("Іван Франко", "Unlimited", 12, 100, 10000.0),
                new GymSubscription("Леся Українка", "Standard", 3, 20, 3000.0)
        );

        GymSummary summary = Main.calculateSummary(records);
        String report = Main.formatReport(summary);

        assertEquals(2, summary.totalRecords());
        assertEquals(60.0, summary.averageVisits(), 0.001);
        assertEquals(13000.0, summary.totalRevenue(), 0.001);

        assertTrue(report.contains("Середня кількість відвідувань: 60.00"));
        assertTrue(report.contains("Загальний виторг: 13000.00 грн"));
        assertTrue(report.contains("Іван Франко (Unlimited, 12 міс.)"));
    }

    @Test
    void testCalculateSummaryEmptyList() {
        GymSummary summary = Main.calculateSummary(List.of());
        String report = Main.formatReport(summary);

        assertEquals(0, summary.totalRecords());
        assertTrue(report.contains("Не знайдено жодного коректного запису"));
    }
}