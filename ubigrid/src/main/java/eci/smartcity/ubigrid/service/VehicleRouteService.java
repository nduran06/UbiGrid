package eci.smartcity.ubigrid.service;

import java.util.List;

import eci.smartcity.ubigrid.model.RouteSegment;
import eci.smartcity.ubigrid.model.enums.RoutePreference;

/**
 * Service interface for vehicle routing operations.
 */
public interface VehicleRouteService {
    
    /**
     * Compute optimal route for a vehicle
     * 
     * @param vehicleId The ID of the vehicle to route
     * @param destinationLongitude Destination longitude coordinate
     * @param destinationLatitude Destination latitude coordinate
     * @param destinationLocationId Optional identifier for the destination location
     * @param routePreference Preferred routing strategy (fastest, shortest, etc.)
     * @return List of route segments constituting the optimal route
     */
    List<RouteSegment> computeOptimalRoute(
            String vehicleId, 
            double destinationLongitude, 
            double destinationLatitude,
            String destinationLocationId, 
            RoutePreference routePreference);
    
    /**
     * Find alternative routes to offer driver choices
     * 
     * @param vehicleId The ID of the vehicle to route
     * @param destLon Destination longitude coordinate
     * @param destLat Destination latitude coordinate
     * @param destId Optional identifier for the destination location
     * @param preference Preferred routing strategy (fastest, shortest, etc.) 
     * @return List of alternative routes, each represented as a list of route segments
     */
    List<List<RouteSegment>> findAlternativeRoutes(
            String vehicleId, 
            double destLon, 
            double destLat, 
            String destId,
            RoutePreference preference);
    
    /**
     * Recompute routes for all active vehicles to adapt to changing conditions
     */
    void recomputeAllRoutes();
}