package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Lab05ExportTest {

    @Test
    void repositoryStoresSnapshotAndFiltersWithPredicate() {
        Repository<GymSubscription> repo = new Repository<>();
        GymSubscription s1 = new StandardSubscription("Марія", "Standard Gym", 3, 24, 3200.0);
        GymSubscription p1 = new PremiumSubscription("Олексій", "Full Unlimited", 12, 120, 12000.0, 24);

        repo.add(s1);
        repo.add(p1);

        assertEquals(2, repo.size());
        assertEquals(1, repo.findAll(sub -> sub.getCategory() == PlanCategory.PREMIUM).size());
        assertTrue(repo.findFirst(sub -> sub.getClient().equals("Марія")).isPresent());

        // Знімок захищений від зовнішньої модифікації
        List<GymSubscription> snap = repo.snapshot();
        assertThrows(UnsupportedOperationException.class, () -> snap.add(s1));
    }

    @Test
    void reflectionExporterRoundTripPreservesDomainData(@TempDir Path tempDir) throws Exception {
        Repository<GymSubscription> repo = new Repository<>();
        repo.add(new PremiumSubscription("Олексій Ковальчук", "Full Unlimited", 12, 120, 12000.0, 24));
        repo.add(new StandardSubscription("Марія Бондаренко", "Standard Gym", 3, 24, 3200.0));

        Path exportedCsv = tempDir.resolve("exported_gym.csv");
        ReflectionCsvExporter.exportToFile(exportedCsv, repo.snapshot(), GymSubscription.class, false);

        List<String> rawLines = Files.readAllLines(exportedCsv, StandardCharsets.UTF_8);
        assertEquals(2, rawLines.size());

        // Round-trip: повторне зчитування збережених CSV-рядків назад у доменні об'єкти
        List<GymSubscription> reloaded = rawLines.stream()
                .map(GymSubscription::fromCsv)
                .toList();

        assertEquals(repo.snapshot(), reloaded);
    }

    @Test
    void exporterThrowsWhenClassHasNoAnnotations() {
        assertThrows(CsvExportException.class,
                () -> ReflectionCsvExporter.toCsvLines(List.of("простий рядок"), String.class, true));
    }
}