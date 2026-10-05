package eci.smartcity.ubigrid.repository.traffic;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import eci.smartcity.ubigrid.model.traffic.TrafficData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrafficDataRepository extends MongoRepository<TrafficData, String> {
    
    @Query("{'locationId': ?0, 'timestamp': {$gt: ?1}}")
    List<TrafficData> findByLocationIdAndTimestampGreaterThan(String locationId, LocalDateTime timestamp);
    
    @Query("{'locationId': ?0, 'timestamp': {$gte: ?1, $lte: ?2}}")
    List<TrafficData> findByLocationIdAndTimestampBetween(String locationId, LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("{'sensorId': ?0}")
    List<TrafficData> findBySensorId(String sensorId);
    
    @Query("{'roadSegmentId': ?0, 'timestamp': {$gt: ?1}}")
    List<TrafficData> findByRoadSegmentIdAndRecentTimestamp(String roadSegmentId, LocalDateTime cutoffTime);
    
    @Query("{'location': {$geoWithin: {$box: [[?0, ?1], [?2, ?3]]}}}")
    List<TrafficData> findByLocationInArea(
            double minLongitude, double minLatitude,
            double maxLongitude, double maxLatitude);
    
    @Query(value = "{'locationId': ?0}", sort = "{'timestamp': -1}")
    List<TrafficData> findRecentByLocationId(String locationId, Pageable pageable);
    
    @Query(value = "{'congestionLevel': {$gte: ?0}}", sort = "{'timestamp': -1}")
    List<TrafficData> findByHighCongestionLevel(double threshold, Pageable pageable);
    
    @Query("{'weatherCondition': ?0, 'timestamp': {$gt: ?1}}")
    List<TrafficData> findByWeatherConditionAndRecentTimestamp(String weatherCondition, LocalDateTime cutoffTime);
    
    @Query("{'nearbyEvent': {$exists: true, $ne: null}}")
    List<TrafficData> findWithNearbyEvents();
    
    @Query(value = "{'roadSegmentId': ?0}", sort = "{'timestamp': -1}")
    Optional<TrafficData> findLatestByRoadSegmentId(String roadSegmentId);
    
    @Query(value = "{'locationId': ?0}", sort = "{'timestamp': -1}")
    Optional<TrafficData> findLatestByLocationId(String locationId);
    
    // Aggregation query
    @Query(value = "{'locationId': ?0, 'timestamp': {$gte: ?1}}", count = true)
    Long countRecentDataPoints(String locationId, LocalDateTime cutoffTime);
}