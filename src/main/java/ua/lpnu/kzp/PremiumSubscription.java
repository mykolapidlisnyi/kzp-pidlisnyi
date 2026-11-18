package ua.lpnu.kzp;

import java.util.Objects;

/**
 * Преміум/VIP абонемент тренажерного залу з нарахованими бонусними сесіями (тренер/СПА).
 */
public final class PremiumSubscription extends GymSubscription {

    private final int bonusSessions;

    public PremiumSubscription(String client, String plan, int months, int visits, double price, int bonusSessions) {
        super(client, plan, months, visits, price);
        if (bonusSessions < 0) {
            throw new IllegalArgumentException("Кількість бонусних сесій не може бути від'ємною (отримано " + bonusSessions + ")");
        }
        this.bonusSessions = bonusSessions;
    }

    public int getBonusSessions() {
        return bonusSessions;
    }

    @Override
    public PlanCategory getCategory() {
        return PlanCategory.PREMIUM;
    }

    @Override
    public double effectiveVisitCost() {
        int totalSessions = Math.max(1, getVisits() + bonusSessions);
        return getPrice() / totalSessions;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        PremiumSubscription that = (PremiumSubscription) o;
        return bonusSessions == that.bonusSessions;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), bonusSessions);
    }
}