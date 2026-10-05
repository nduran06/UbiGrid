package eci.smartcity.ubigrid.model;

/**
 * One candidate path the router priced against the live traffic readings
 * before picking the best one.
 */
public record RouteEvaluation(
        String label,
        double distanceMeters,
        int durationSeconds,
        String trafficLevel,
        boolean chosen) {
}
