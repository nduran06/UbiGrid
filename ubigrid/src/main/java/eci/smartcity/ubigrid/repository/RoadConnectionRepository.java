package eci.smartcity.ubigrid.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import eci.smartcity.ubigrid.model.RoadConnection;

@Repository
public interface RoadConnectionRepository extends MongoRepository<RoadConnection, String> {
    
    List<RoadConnection> findBySourceSegmentId(String sourceSegmentId);
    
    List<RoadConnection> findByTargetSegmentId(String targetSegmentId);
    
    @Query("{'sourceSegmentId': ?0, 'targetSegmentId': ?1}")
    RoadConnection findBySourceAndTarget(String sourceSegmentId, String targetSegmentId);
    
    @Query("{'isRestricted': true}")
    List<RoadConnection> findRestrictedConnections();
    
    @Query("{'vehicleRestrictions.?0': true}")
    List<RoadConnection> findConnectionsRestrictedForVehicleType(String vehicleType);
}
