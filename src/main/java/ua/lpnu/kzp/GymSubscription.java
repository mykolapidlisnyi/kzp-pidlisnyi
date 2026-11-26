package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;

/**
 * Спільний абстрактний базовий тип для всіх абонементів у тренажерному залі (Варіант 20).
 * Інкапсулює спільні поля, валідацію інваріантів та поліморфний контракт розрахунку вартості.
 */
public abstract class GymSubscription {

    @CsvColumn(header = "client", order = 1)
    private final String client;
    @CsvColumn(header = "plan", order = 2)
    private final String plan;
    @CsvColumn(header = "months", order = 3)
    private final int months;
    @CsvColumn(header = "visits", order = 4)
    private final int visits;
    @CsvColumn(header = "price", order = 5)
    private final double price;

    /**
     * Створює абонемент після перевірки всіх спільних інваріантів.
     * Валідація виконується до делегування у приватний конструктор, щоб запобігти CT_CONSTRUCTOR_THROW.
     */
    protected GymSubscription(String client, String plan, int months, int visits, double price) {
        this(
                validateText(client, "Ім'я клієнта не може бути порожнім"),
                validateText(plan, "Назва абонемента не може бути порожньою"),
                validatePositive(months, "Тривалість місяців повинна бути більше 0"),
                validateNonNegative(visits, "Кількість відвідувань не може бути від'ємною"),
                validatePrice(price),
                true
        );
    }

    private GymSubscription(String client, String plan, int months, int visits, double price, boolean validated) {
        this.client = client;
        this.plan = plan;
        this.months = months;
        this.visits = visits;
        this.price = price;
    }

    private static String validateText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private static int validatePositive(int value, String message) {
        if (value <= 0) {
            throw new IllegalArgumentException(message + " (отримано " + value + ")");
        }
        return value;
    }

    private static int validateNonNegative(int value, String message) {
        if (value < 0) {
            throw new IllegalArgumentException(message + " (отримано " + value + ")");
        }
        return value;
    }

    private static double validatePrice(double price) {
        if (!Double.isFinite(price) || price <= 0.0) {
            throw new IllegalArgumentException("Ціна повинна бути скінченним числом > 0.0 (отримано " + price + ")");
        }
        return price;
    }

    /**
     * Статичний фабричний метод для створення конкретного підтипу абонемента з рядка CSV:
     * {@code client;plan;months;visits;price}
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

        PlanCategory category = PlanCategory.fromPlanName(plan);
        return switch (category) {
            case PREMIUM -> new PremiumSubscription(client, plan, months, visits, price, months * 2);
            case STANDARD -> new StandardSubscription(client, plan, months, visits, price);
        };
    }

    /**
     * Повертає категорію абонемента (поліморфний контракт).
     */
    public abstract PlanCategory getCategory();

    /**
     * Поліморфний метод обчислення ефективної вартості одного відвідування у грн.
     * Кожен підтип реалізує власну формулу врахування бонусних занять.
     */
    public abstract double effectiveVisitCost();

    public final String getClient() {
        return client;
    }

    public final String getPlan() {
        return plan;
    }

    public final int getMonths() {
        return months;
    }

    public final int getVisits() {
        return visits;
    }

    public final double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s [%s] (%s, %d міс., %d відв., %.2f грн)",
                client, getCategory().getDisplayName(), plan, months, visits, price);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GymSubscription that = (GymSubscription) o;
        return months == that.months
                && visits == that.visits
                && Double.compare(price, that.price) == 0
                && Objects.equals(client, that.client)
                && Objects.equals(plan, that.plan);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), client, plan, months, visits, price);
    }
}