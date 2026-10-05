package eci.smartcity.ubigrid.repository;

import java.time.LocalDateTime;
import java.util.List;

import eci.smartcity.ubigrid.model.Vehicle;


public interface VehicleNodeCustomRepository {
    
    double getAverageSpeedOnRoadSegment(String roadSegmentId, LocalDateTime cutoffTime);
    
    List<Vehicle> findVehiclesWithPlannedRouteContaining(String roadSegmentId);
    
    void updateVehicleCurrentRoadSegment(String vehicleId, String roadSegmentId);
    
    List<Vehicle> findVehiclesNearPoint(double longitude, double latitude, double radiusMeters);
}