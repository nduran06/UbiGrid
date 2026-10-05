package eci.smartcity.ubigrid.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import eci.smartcity.ubigrid.model.enums.RoutePreference;
import eci.smartcity.ubigrid.model.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a vehicle node in the smart city traffic system.
 * Each connected vehicle is treated as an individual node that
 * participates in the traffic network.
 */
@Document(collection = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {
    
    @Id
    private String vehicleId;
    
    private VehicleType vehicleType;  // CAR, BUS, TRUCK, EMERGENCY, etc.
    
    private String status;
    
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoLocation currentLocation;
    
    private Double currentSpeed;
    
    private Double heading;  // Direction in degrees
    
    @Indexed
    private LocalDateTime lastUpdateTime;
    
    private String currentRoadSegmentId;
    
    private String destinationLocationId;
    
    private GeoLocation destinationLocation;
    
    private List<RouteSegment> plannedRoute;
    
    private RoutePreference routePreference;
    
    private LocalDateTime estimatedArrivalTime;
    
    @Indexed
    private Boolean isConnected;
    
    @Indexed
    private Boolean isEmergencyVehicle;
    
    @Builder.Default
    private Map<String, String> attributes = new HashMap<>();

    public void setCurrentLocation(double longitude, double latitude) {
    	this.currentLocation.setCoordinates(new double[]{longitude, latitude});
    }
}
