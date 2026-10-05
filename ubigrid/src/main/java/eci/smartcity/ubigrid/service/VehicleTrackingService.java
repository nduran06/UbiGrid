package eci.smartcity.ubigrid.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import eci.smartcity.ubigrid.model.GeoLocation;
import eci.smartcity.ubigrid.model.Vehicle;
import eci.smartcity.ubigrid.model.VehicleLocationHistory;

/**
 * Service interface for vehicle tracking operations.
 */
public interface VehicleTrackingService {

	/**
	 * Update a vehicle's current location
	 * 
	 * @param vehicleId      The ID of the vehicle
	 * @param latitude       The current latitude
	 * @param longitude      The current longitude
	 * @param timestamp      The timestamp of the location update
	 * @param additionalData Additional data like speed, heading, etc.
	 * @return The updated vehicle node
	 */
	Vehicle updateVehicleLocation(String vehicleId, double latitude, double longitude, LocalDateTime timestamp,
			Map<String, Object> additionalData);

	/**
	 * Get the current location of a vehicle
	 * 
	 * @param vehicleId The ID of the vehicle
	 * @return Array of [longitude, latitude] or null if not available
	 */
	GeoLocation getVehicleLocation(String vehicleId);

	/**
	 * Get the location history of a vehicle for a specific time period
	 * 
	 * @param vehicleId The ID of the vehicle
	 * @param startTime The start time of the period
	 * @param endTime   The end time of the period
	 * @return List of location history entries
	 */
	List<VehicleLocationHistory> getVehicleLocationHistory(String vehicleId, LocalDateTime startTime,
			LocalDateTime endTime);

	/**
	 * Find all vehicles in a specific area
	 * 
	 * @param centerLatitude  The latitude of the center point
	 * @param centerLongitude The longitude of the center point
	 * @param radiusKm        The radius in kilometers
	 * @return List of vehicles in the area
	 */
	List<Vehicle> findVehiclesInArea(double centerLatitude, double centerLongitude, double radiusKm);

	/**
	 * Find all vehicles of a specific type
	 * 
	 * @param vehicleType The type of vehicle to find
	 * @return List of vehicles of the specified type
	 */
	List<Vehicle> findVehiclesByType(String vehicleType);

	/**
	 * Calculate the distance traveled by a vehicle in a specific time period
	 * 
	 * @param vehicleId The ID of the vehicle
	 * @param startTime The start time of the period
	 * @param endTime   The end time of the period
	 * @return The distance traveled in meters
	 */
	double calculateDistanceTraveled(String vehicleId, LocalDateTime startTime, LocalDateTime endTime);

	/**
	 * Get the estimated time of arrival to destination
	 * 
	 * @param vehicleId The ID of the vehicle
	 * @return The estimated time of arrival or null if not available
	 */
	LocalDateTime getEstimatedTimeOfArrival(String vehicleId);

	/**
	 * Check if a vehicle is currently moving
	 * 
	 * @param vehicleId The ID of the vehicle
	 * @return True if the vehicle is moving, false otherwise
	 */
	boolean isVehicleMoving(String vehicleId);
}
