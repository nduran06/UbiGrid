package eci.smartcity.ubigrid.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eci.smartcity.ubigrid.model.GeoLocation;
import eci.smartcity.ubigrid.model.RouteSegment;
import eci.smartcity.ubigrid.model.Vehicle;
import eci.smartcity.ubigrid.repository.VehicleRepository;
import eci.smartcity.ubigrid.service.VehicleRouteService;
import eci.smartcity.ubigrid.service.impl.TrafficFeedSimulator;

/**
 * Dispatches the nearest available vehicle to a pickup point and returns the
 * real route VehicleRouteService computed for it, for the front-end map to
 * draw and animate.
 */
@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final VehicleRouteService vehicleRouteService;
    private final VehicleRepository vehicleRepository;
    private final TrafficFeedSimulator trafficFeed;

    @Autowired
    public RouteController(VehicleRouteService vehicleRouteService, VehicleRepository vehicleRepository,
            TrafficFeedSimulator trafficFeed) {
        this.vehicleRouteService = vehicleRouteService;
        this.vehicleRepository = vehicleRepository;
        this.trafficFeed = trafficFeed;
    }

    @PostMapping
    public RouteResponseDto requestRoute(@RequestBody RouteRequestDto request) {
        Vehicle vehicle = findNearestVehicle(request.destinationLongitude(), request.destinationLatitude());

        List<RouteSegment> segments = vehicleRouteService.computeOptimalRoute(
                vehicle.getVehicleId(),
                request.destinationLongitude(),
                request.destinationLatitude(),
                null,
                request.routePreference());

        return toResponse(vehicle, segments);
    }

    private Vehicle findNearestVehicle(double destinationLongitude, double destinationLatitude) {
        List<Vehicle> vehicles = vehicleRepository.findAll();

        Vehicle nearest = null;
        double bestDistanceMeters = Double.MAX_VALUE;

        for (Vehicle vehicle : vehicles) {
            GeoLocation location = vehicle.getCurrentLocation();
            if (location == null) {
                continue;
            }
            double distance = distanceMeters(
                    location.getLatitude(), location.getLongitude(),
                    destinationLatitude, destinationLongitude);
            if (distance < bestDistanceMeters) {
                bestDistanceMeters = distance;
                nearest = vehicle;
            }
        }

        if (nearest == null) {
            throw new IllegalStateException("No hay vehículos disponibles en este momento.");
        }
        return nearest;
    }

    private RouteResponseDto toResponse(Vehicle vehicle, List<RouteSegment> segments) {
        List<RoutePointDto> path = new ArrayList<>();
        if (!segments.isEmpty()) {
            double[] start = segments.get(0).getStartLocation();
            path.add(new RoutePointDto(start[1], start[0]));
            for (RouteSegment segment : segments) {
                double[] end = segment.getEndLocation();
                path.add(new RoutePointDto(end[1], end[0]));
            }
        }

        double totalDistance = segments.stream().mapToDouble(RouteSegment::getDistanceMeters).sum();
        int totalDuration = segments.stream().mapToInt(RouteSegment::getDurationSeconds).sum();

        return new RouteResponseDto(
                vehicle.getVehicleId(),
                vehicle.getVehicleType() != null ? vehicle.getVehicleType().name() : "CAR",
                totalDistance,
                totalDuration,
                worstTrafficLevel(segments),
                path,
                segments.stream().map(RouteSegment::getTrafficLevel).toList(),
                vehicleRouteService.getLastEvaluations(vehicle.getVehicleId()),
                trafficFeed.getLatestReadings().size());
    }

    private String worstTrafficLevel(List<RouteSegment> segments) {
        if (segments.stream().anyMatch(s -> "HEAVY".equals(s.getTrafficLevel()))) {
            return "HEAVY";
        }
        if (segments.stream().anyMatch(s -> "MODERATE".equals(s.getTrafficLevel()))) {
            return "MODERATE";
        }
        return "LIGHT";
    }

    // Haversine distance, kept local since every other service in this codebase
    // (VehicleRouteServiceImpl, TrafficServiceImpl) inlines the same formula
    // rather than sharing a utility.
    private double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        final int earthRadiusMeters = 6371000;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusMeters * c;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleNoVehicleAvailable(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", ex.getMessage()));
    }
}
