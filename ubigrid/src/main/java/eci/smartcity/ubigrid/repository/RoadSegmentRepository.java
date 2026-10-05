package eci.smartcity.ubigrid.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import eci.smartcity.ubigrid.model.RoadSegment;

@Repository
public interface RoadSegmentRepository extends MongoRepository<RoadSegment, String>, RoadSegmentCustomRepository {
    
    @Query("{'currentCongestionLevel': {$gte: ?0}}")
    List<RoadSegment> findCongestedRoadSegments(double congestionThreshold);
    
    @Query("{'startLocation.coordinates': {$geoWithin: {$box: [[?0, ?1], [?2, ?3]]}}}") 
    List<RoadSegment> findRoadSegmentsInArea(
            double minLongitude, double minLatitude,
            double maxLongitude, double maxLatitude);
    
    List<RoadSegment> findByRoadType(String roadType);
    
    @Query("{'trafficSignals.?0': {$exists: true}}")
    List<RoadSegment> findRoadSegmentsByTrafficSignal(String trafficSignalId);
    
    List<RoadSegment> findByOutgoingConnectionsContaining(String connectionId);
    
    List<RoadSegment> findByIncomingConnectionsContaining(String connectionId);
    
    List<RoadSegment> findByNameLike(String namePattern);
}