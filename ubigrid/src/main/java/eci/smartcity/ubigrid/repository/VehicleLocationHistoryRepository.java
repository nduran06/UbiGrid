package eci.smartcity.ubigrid.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import eci.smartcity.ubigrid.model.VehicleLocationHistory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for accessing VehicleLocationHistory entities in
 * MongoDB.
 */
@Repository
public interface VehicleLocationHistoryRepository extends MongoRepository<VehicleLocationHistory, String> {

	// Find location history by vehicle ID
	List<VehicleLocationHistory> findByVehicleId(String vehicleId);

	// Find location history by vehicle ID and time range
	List<VehicleLocationHistory> findByVehicleIdAndTimestampBetweenOrderByTimestampAsc(String vehicleId,
			LocalDateTime startTime, LocalDateTime endTime);

	// Find latest location history for a vehicle
	VehicleLocationHistory findTopByVehicleIdOrderByTimestampDesc(String vehicleId);

	// Find locations within a specific area
	@Query("{'location': {$near: {$geometry: {type: 'Point', coordinates: [?0, ?1]}, $maxDistance: ?2}}}")
	List<VehicleLocationHistory> findLocationsNear(double longitude, double latitude, double maxDistanceMeters);

	// Count location updates for a vehicle in a time period
	long countByVehicleIdAndTimestampBetween(String vehicleId, LocalDateTime startTime, LocalDateTime endTime);

	// Find vehicles that were active (had location updates) in a time period
	@Query("{'timestamp': {$gte: ?0, $lte: ?1}}")
	List<String> findDistinctVehicleIdsByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);

	// Delete location history older than a certain date
	void deleteByTimestampBefore(LocalDateTime cutoffDate);

	// Find entries where speed is above threshold
	List<VehicleLocationHistory> findByVehicleIdAndSpeedGreaterThan(String vehicleId, Double speedThreshold);
}