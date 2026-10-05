package eci.smartcity.ubigrid.repository.traffic;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import eci.smartcity.ubigrid.model.traffic.TrafficIncident;
import eci.smartcity.ubigrid.model.traffic.enums.IncidentStatus;

@Repository
public interface TrafficIncidentRepository extends MongoRepository<TrafficIncident, String> {
    
    @Query("{'status': {$in: ['DETECTED', 'VERIFIED', 'IN_PROGRESS', 'RESOLVING']}, 'detectionTime': {$gt: ?0}}")
    List<TrafficIncident> findActiveIncidents(LocalDateTime cutoffTime);
    
    @Query("{'status': {$in: ['DETECTED', 'VERIFIED', 'IN_PROGRESS', 'RESOLVING']}, " +
           "'detectionTime': {$gt: ?0}, 'severityScore': {$gte: ?1}}")
    List<TrafficIncident> findActiveIncidentsBySeverity(LocalDateTime cutoffTime, Double minSeverity);
    
    @Query("{'status': {$in: ['DETECTED', 'VERIFIED', 'IN_PROGRESS', 'RESOLVING']}, " +
           "'detectionTime': {$gt: ?0}, 'incidentType': ?1}")
    List<TrafficIncident> findActiveIncidentsByType(LocalDateTime cutoffTime, String incidentType);
    
    @Query("{'status': {$in: ['DETECTED', 'VERIFIED', 'IN_PROGRESS', 'RESOLVING']}, " +
           "'detectionTime': {$gt: ?0}, 'severityScore': {$gte: ?1}, 'incidentType': ?2}")
    List<TrafficIncident> findActiveIncidentsBySeverityAndType(
            LocalDateTime cutoffTime, Double minSeverity, String incidentType);
    
    @Query("{'location': {$geoWithin: {$centerSphere: [[?0, ?1], ?2]}}}")
    List<TrafficIncident> findIncidentsNearPoint(double longitude, double latitude, double radiusInRadians);
    
    @Query("{'affectedRoadSegments': ?0, 'status': {$ne: 'RESOLVED'}}")
    List<TrafficIncident> findActiveIncidentsByRoadSegment(String roadSegmentId);
    
    @Query("{'locationId': ?0, 'status': {$ne: 'RESOLVED'}}")
    List<TrafficIncident> findActiveIncidentsByLocationId(String locationId);
    
    @Query(value = "{'detectionTime': {$gt: ?0}}", sort = "{'severityScore': -1}")
    List<TrafficIncident> findRecentIncidentsSortedBySeverity(LocalDateTime cutoffTime, Pageable pageable);
    
    @Query("{'status': ?0}")
    List<TrafficIncident> findByStatus(IncidentStatus status);
    
    @Query("{'estimatedResolutionTime': {$lt: ?0}, 'status': {$ne: 'RESOLVED'}}")
    List<TrafficIncident> findOverdueIncidents(LocalDateTime currentTime);
}