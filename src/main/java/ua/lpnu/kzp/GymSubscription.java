package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;

/**
 * Описує незмінний запис про абонемент клієнта у тренажерному залі (Варіант 20).
 * Усі інваріанти перевіряються під час створення об'єкта.
 */
public final class GymSubscription {

    private final String client;
    private final String plan;
    private final int months;
    private final int visits;
    private final double price;

    /**
     * Створює коректний об'єкт абонемента після перевірки всіх полів.
     *
     * @param client ім'я та прізвище клієнта (непорожній рядок)
     * @param plan   назва тарифного плану (непорожній рядок)
     * @param months тривалість абонемента в місяцях (має бути > 0)
     * @param visits кількість відвідувань (має бути >= 0)
     * @param price  вартість абонемента у грн (скінченне число > 0.0)
     * @throws IllegalArgumentException якщо хоча б одне поле порушує правила валідації
     */
    public GymSubscription(String client, String plan, int months, int visits, double price) {
        if (client == null || client.isBlank()) {
            throw new IllegalArgumentException("Ім'я клієнта не може бути порожнім");
        }
        if (plan == null || plan.isBlank()) {
            throw new IllegalArgumentException("Назва абонемента не може бути порожньою");
        }
        if (months <= 0) {
            throw new IllegalArgumentException("Тривалість місяців повинна бути більше 0 (отримано " + months + ")");
        }
        if (visits < 0) {
            throw new IllegalArgumentException("Кількість відвідувань не може бути від'ємною (отримано " + visits + ")");
        }
        if (!Double.isFinite(price) || price <= 0.0) {
            throw new IllegalArgumentException("Ціна повинна бути скінченним числом > 0.0 (отримано " + price + ")");
        }

        this.client = client.trim();
        this.plan = plan.trim();
        this.months = months;
        this.visits = visits;
        this.price = price;
    }

    /**
     * Статичний фабричний метод для розбору рядка CSV формату:
     * {@code client;plan;months;visits;price}
     *
     * @param line рядок із вхідного CSV-файлу
     * @return валідований екземпляр {@link GymSubscription}
     * @throws IllegalArgumentException якщо рядок пошкоджений або має невірний формат полів
     */
    public static GymSubscription fromCsv(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("Рядок CSV не може бути порожнім");
        }

        String[] parts = line.split(";", -1);
        if (parts.length != 5) {
            throw new IllegalArgumentException(
                    "Некоректна кількість полів (очікується 5, отримано " + parts.length + ")");
        }

        String client = parts[0].trim();
        String plan = parts[1].trim();
        String monthsStr = parts[2].trim();
        String visitsStr = parts[3].trim();
        String priceStr = parts[4].trim();

        int months;
        try {
            months = Integer.parseInt(monthsStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Помилка розбору кількості місяців '" + monthsStr + "'", e);
        }

        int visits;
        try {
            visits = Integer.parseInt(visitsStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Помилка розбору кількості відвідувань '" + visitsStr + "'", e);
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Помилка розбору ціни '" + priceStr + "'", e);
        }

        return new GymSubscription(client, plan, months, visits, price);
    }

    public String getClient() {
        return client;
    }

    public String getPlan() {
        return plan;
    }

    public int getMonths() {
        return months;
    }

    public int getVisits() {
        return visits;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s (%s, %d міс., %d відв., %.2f грн)",
                client, plan, months, visits, price);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GymSubscription that)) return false;
        return months == that.months
                && visits == that.visits
                && Double.compare(price, that.price) == 0
                && Objects.equals(client, that.client)
                && Objects.equals(plan, that.plan);
    }

    @Override
    public int hashCode() {
        return Objects.hash(client, plan, months, visits, price);
    }
}