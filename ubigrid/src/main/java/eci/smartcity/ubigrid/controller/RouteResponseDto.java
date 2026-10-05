package eci.smartcity.ubigrid.controller;

import java.util.List;

import eci.smartcity.ubigrid.model.RouteEvaluation;

/**
 * The route assigned to a pickup request: which vehicle was dispatched and
 * the actual path it computed (via VehicleRouteService), with distance,
 * duration and a traffic summary for the whole route. segmentTraffic has one
 * traffic level per path segment (path.size() - 1 entries); evaluations lists
 * every candidate path priced against the live traffic readings, and
 * activeSensors how many sensors fed that pricing.
 */
public record RouteResponseDto(
        String vehicleId,
        String vehicleType,
        double totalDistanceMeters,
        int totalDurationSeconds,
        String trafficSummary,
        List<RoutePointDto> path,
        List<String> segmentTraffic,
        List<RouteEvaluation> evaluations,
        int activeSensors) {
}
