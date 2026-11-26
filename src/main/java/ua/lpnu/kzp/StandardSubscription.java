package ua.lpnu.kzp;

/**
 * Стандартний абонемент тренажерного залу без додаткових бонусних сесій.
 */
public final class StandardSubscription extends GymSubscription {

    public StandardSubscription(String client, String plan, int months, int visits, double price) {
        super(client, plan, months, visits, price);
    }

    @Override
    public PlanCategory getCategory() {
        return PlanCategory.STANDARD;
    }

    @Override
    public double effectiveVisitCost() {
        int totalSessions = Math.max(1, getVisits());
        return getPrice() / totalSessions;
    }
}