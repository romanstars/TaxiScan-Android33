package com.taxiscan.app;

/** Pure calculation logic for a single ride. Amounts are in UAH and distances in km. */
public final class TripCalculator {
    private TripCalculator() { }

    public static Result calculate(double fare, double rideKm, double pickupKm,
                                   double commissionPercent, double fuelLitresPer100Km,
                                   double fuelPricePerLitre, double wearPerKm) {
        requireNonNegative(fare, "Fare");
        requireNonNegative(rideKm, "Ride distance");
        requireNonNegative(pickupKm, "Pickup distance");
        requireRange(commissionPercent, 0, 100, "Commission");
        requireNonNegative(fuelLitresPer100Km, "Fuel consumption");
        requireNonNegative(fuelPricePerLitre, "Fuel price");
        requireNonNegative(wearPerKm, "Vehicle wear");

        double totalKm = rideKm + pickupKm;
        double commission = fare * commissionPercent / 100.0;
        double fuelCost = totalKm * fuelLitresPer100Km / 100.0 * fuelPricePerLitre;
        double wearCost = totalKm * wearPerKm;
        double net = fare - commission - fuelCost - wearCost;
        double netPerKm = totalKm == 0 ? 0 : net / totalKm;
        return new Result(fare, commission, fuelCost, wearCost, totalKm, net, netPerKm);
    }

    private static void requireNonNegative(double value, String name) {
        if (!Double.isFinite(value) || value < 0) throw new IllegalArgumentException(name + " must be non-negative");
    }

    private static void requireRange(double value, double min, double max, String name) {
        if (!Double.isFinite(value) || value < min || value > max) throw new IllegalArgumentException(name + " must be between " + min + " and " + max);
    }

    public static final class Result {
        public final double fare, commission, fuelCost, wearCost, totalKm, net, netPerKm;
        Result(double fare, double commission, double fuelCost, double wearCost, double totalKm, double net, double netPerKm) {
            this.fare = fare; this.commission = commission; this.fuelCost = fuelCost;
            this.wearCost = wearCost; this.totalKm = totalKm; this.net = net; this.netPerKm = netPerKm;
        }
    }
}
