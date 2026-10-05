package eci.smartcity.ubigrid.repository;

import java.util.List;

import eci.smartcity.ubigrid.model.RoadSegment;

public interface RoadSegmentCustomRepository {
    
    List<RoadSegment> findRoadSegmentsNearPoint(double longitude, double latitude, double radiusMeters);
    
    void updateCongestionLevel(String roadSegmentId, double congestionLevel);
    
    void updateVehicleCount(String roadSegmentId, int vehicleCount);
    
    void updateAverageSpeed(String roadSegmentId, double averageSpeed);
}
