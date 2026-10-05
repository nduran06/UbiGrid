package eci.smartcity.ubigrid.model.traffic;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import eci.smartcity.ubigrid.model.GeoLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents traffic data collected from sensors, cameras, and other sources
 */
@Document(collection = "traffic_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrafficData {
    
    @Id
    private String id;
    
    @Indexed
    private String sensorId;
    
    @Indexed
    private String locationId;
    
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoLocation location;
    
    @Indexed
    private LocalDateTime timestamp;
    
    private Integer vehicleCount;
    
    private Double averageSpeed;
    
    private Double congestionLevel;
    
    private Map<String, Integer> vehicleTypes = new HashMap<String, Integer>();
    
    private Map<String, String> metadata = new HashMap<String, String>();
    
    // Weather-related fields
    private Double temperature;
    private Double precipitation;
    private String weatherCondition;
    
    // Event-related field
    private String nearbyEvent;
    
    // Road segment ID this data relates to, if applicable
    private String roadSegmentId;
    
    // Data source type
    private String sourceType; // SENSOR, CAMERA, PROBE, CALCULATION
}
