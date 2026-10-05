package eci.smartcity.ubigrid.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import eci.smartcity.ubigrid.model.Vehicle;
import eci.smartcity.ubigrid.model.enums.RoutePreference;


@Repository
@Component
public interface VehicleRepository extends MongoRepository<Vehicle, String>, VehicleNodeCustomRepository {
    
    List<Vehicle> findByLastUpdateTimeGreaterThan(LocalDateTime cutoffTime);
    
    List<Vehicle> findByCurrentRoadSegmentId(String roadSegmentId);
    
    List<Vehicle> findByVehicleType(String vehicleType);
    
    List<Vehicle> findByStatus(String status);
    
    @Query("{'location': {$near: {$geometry: {type: 'Point', coordinates: [?0, ?1]}, $maxDistance: ?2}}}")
    List<Vehicle> findVehiclesNearLocation(double longitude, double latitude, double maxDistanceMeters);
    
    @Query("{'currentLocation': {$geoWithin: {$box: [[?0, ?1], [?2, ?3]]}}}")
    List<Vehicle> findVehiclesInArea(
            double minLongitude, double minLatitude,
            double maxLongitude, double maxLatitude);
    
    List<Vehicle> findByDestinationLocationIdAndLastUpdateTimeGreaterThan(
            String destinationLocationId, LocalDateTime cutoffTime);
    
    List<Vehicle> findByIsEmergencyVehicleAndLastUpdateTimeGreaterThan(
            boolean isEmergency, LocalDateTime cutoffTime);
    
    List<Vehicle> findByVehicleTypeAndLastUpdateTimeGreaterThan(
            String vehicleType, LocalDateTime cutoffTime);
    
    List<Vehicle> findByRoutePreferenceAndLastUpdateTimeGreaterThan(
            RoutePreference preference, LocalDateTime cutoffTime);
    
    @Query(value = "{'currentRoadSegmentId': ?0}", count = true)
    Integer countVehiclesOnRoadSegment(String roadSegmentId);
    
    @Query("{'currentRoadSegmentId': ?0, 'lastUpdateTime': {$gt: ?1}}")
    List<Vehicle> findActiveVehiclesOnRoadSegment(String roadSegmentId, LocalDateTime cutoffTime);
    
    @Query(value = "{'currentRoadSegmentId': ?0, 'lastUpdateTime': {$gt: ?1}}", fields = "{'currentSpeed': 1}")
    List<Vehicle> findVehicleSpeedsOnRoadSegment(String roadSegmentId, LocalDateTime cutoffTime);
}