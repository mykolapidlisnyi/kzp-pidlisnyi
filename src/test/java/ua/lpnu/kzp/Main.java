package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void testGenerateReportWithValidGymData() {
        List<Main.GymSubscription> records = List.of(
            new Main.GymSubscription("Іван Франко", "Unlimited", 12, 100, 10000.0),
            new Main.GymSubscription("Леся Українка", "Standard", 3, 20, 3000.0)
        );

        String report = Main.generateReport(records);

        assertTrue(report.contains("Середня кількість відвідувань: 60.00"));
        assertTrue(report.contains("Загальний виторг: 13000.00 грн"));
        assertTrue(report.contains("Іван Франко (Unlimited, 12 міс.)"));
    }

    @Test
    void testGenerateReportEmptyList() {
        String report = Main.generateReport(List.of());
        assertTrue(report.contains("Не знайдено жодного коректного запису"));
    }
}