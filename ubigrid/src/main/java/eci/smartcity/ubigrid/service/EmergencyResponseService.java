package eci.smartcity.ubigrid.service;

import java.util.List;

import eci.smartcity.ubigrid.model.RouteSegment;
import eci.smartcity.ubigrid.model.Vehicle;
import eci.smartcity.ubigrid.model.enums.EmergencyStatus;
import eci.smartcity.ubigrid.model.enums.EmergencyVehicleType;

/**
 * Manages emergency response coordination
 */
public interface EmergencyResponseService {

	/**
	 * Register an emergency vehicle
	 */
	void registerEmergencyVehicle(String vehicleId, EmergencyVehicleType type, EmergencyStatus status);

	/**
	 * Update emergency status
	 */
	void updateEmergencyStatus(String vehicleId, EmergencyStatus status);

	/**
	 * Get active emergency vehicles
	 */
	List<Vehicle> getActiveEmergencyVehicles();

	/**
	 * Prioritize route for emergency vehicle
	 */
	List<RouteSegment> prioritizeEmergencyRoute(String vehicleId, double destLon, double destLat);

	/**
	 * Notify nearby vehicles of emergency
	 */
	void notifyNearbyVehicles(String emergencyVehicleId, double radiusMeters);
}
