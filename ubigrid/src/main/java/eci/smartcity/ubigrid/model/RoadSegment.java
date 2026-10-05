package eci.smartcity.ubigrid.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;

import eci.smartcity.ubigrid.model.enums.RoadType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Represents a segment of road in the city traffic network
 */
@Document(collection = "road_segments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadSegment {
    
    @Id
    private String roadSegmentId;
    
    private String name;
    
    private RoadType roadType;
    
    private GeoLocation startLocation;
    
    private GeoLocation endLocation;
    
    private List<GeoLocation> geometry; // Line string of the road segment path
    
    private Double lengthMeters;
    
    private Integer laneCount;
    
    private Double speedLimitKmh;
    
    private Integer maxCapacity;  // Maximum number of vehicles
    
    private Double currentCongestionLevel;  // 0.0 to 1.0
    
    private Integer currentVehicleCount;
    
    private Double currentAverageSpeed;
    
    @Builder.Default
    private List<String> outgoingConnections = new ArrayList<>(); // IDs of connected road segments
    
    @Builder.Default
    private List<String> incomingConnections = new ArrayList<>(); // IDs of connected road segments
    
    @Builder.Default
    private Map<String, String> restrictions = new HashMap<>();
    
    @Builder.Default
    private Map<String, String> trafficSignals = new HashMap<>();
}
