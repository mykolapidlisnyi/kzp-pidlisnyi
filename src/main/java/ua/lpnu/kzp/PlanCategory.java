package ua.lpnu.kzp;

/**
 * Категорії абонементів у тренажерному залі.
 */
public enum PlanCategory {
    STANDARD("Стандартний"),
    PREMIUM("Преміум / VIP");

    private final String displayName;

    PlanCategory(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Повертає читабельний опис категорії для звіту.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Визначає категорію за назвою тарифного плану з CSV.
     */
    public static PlanCategory fromPlanName(String plan) {
        if (plan == null) {
            return STANDARD;
        }
        String lower = plan.toLowerCase(java.util.Locale.ROOT);
        if (lower.contains("unlimited") || lower.contains("vip") || lower.contains("premium")) {
            return PREMIUM;
        }
        return STANDARD;
    }
}