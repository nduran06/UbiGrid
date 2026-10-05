package eci.smartcity.ubigrid.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import eci.smartcity.ubigrid.model.Route;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing RouteData entities in MongoDB.
 */
/*
 * @Repository public interface RouteRepository extends
 * MongoRepository<RouteData, String> {
 * 
 * // Find routes by vehicle ID List<RouteData> findByVehicleId(String
 * vehicleId);
 * 
 * // Find active routes List<RouteData> findByStatus(String status);
 * 
 * // Find routes by vehicle ID and status List<RouteData>
 * findByVehicleIdAndStatus(String vehicleId, String status);
 * 
 * // Find routes near a location (start point)
 * 
 * @Query("{'startLocation': {$near: {$geometry: {type: 'Point', coordinates: [?0, ?1]}, $maxDistance: ?2}}}"
 * ) List<RouteData> findRoutesNearStartLocation(double longitude, double
 * latitude, double maxDistanceMeters);
 * 
 * // Find routes near a location (end point)
 * 
 * @Query("{'endLocation': {$near: {$geometry: {type: 'Point', coordinates: [?0, ?1]}, $maxDistance: ?2}}}"
 * ) List<RouteData> findRoutesNearEndLocation(double longitude, double
 * latitude, double maxDistanceMeters); }
 */

/**
 * Repository interface for accessing ActiveRoute entities in MongoDB.
 */
@Repository
public interface RouteRepository extends MongoRepository<Route, String> {
    
    // Find active route by vehicle ID
    Optional<Route> findByVehicleIdAndActiveIsTrue(String vehicleId);
    
    // Find all active routes
    List<Route> findByActiveIsTrue();
    
    // Find routes by destination location ID
    List<Route> findByDestinationLocationId(String destinationLocationId);
    
    // Find routes by route preference
    List<Route> findByRoutePreference(String routePreference);
    
    // Check if a vehicle has an active route
    boolean existsByVehicleIdAndActiveIsTrue(String vehicleId);
    
    // Deactivate routes (set active=false) for a specific vehicle
    long countByVehicleIdAndActiveIsTrue(String vehicleId);
}

