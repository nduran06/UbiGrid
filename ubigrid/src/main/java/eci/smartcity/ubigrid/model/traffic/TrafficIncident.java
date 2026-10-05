package eci.smartcity.ubigrid.model.traffic;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import eci.smartcity.ubigrid.model.GeoLocation;
import eci.smartcity.ubigrid.model.traffic.enums.IncidentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a traffic incident
 */
@Document(collection = "traffic_incidents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrafficIncident {
    
    @Id
    private String incidentId;
    
    @Indexed
    private String locationId;
    
    @Indexed
    private String incidentType;  // ACCIDENT, ROAD_CLOSURE, CONGESTION, etc.
    
    private String description;
    
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoLocation location;
    
    @Indexed
    private LocalDateTime detectionTime;
    
    private LocalDateTime estimatedResolutionTime;
    
    private LocalDateTime actualResolutionTime;
    
    private Double severityScore;  // 0-1 scale
    
    private Integer impactRadiusMeters;
    
    private List<String> affectedRoadSegments;
    
    private Boolean isVerified;
    
    private String detectionSource;  // CAMERA, SENSOR, USER_REPORT, etc.
    
    private Map<String, Object> additionalData;
    
    @Indexed
    private IncidentStatus status;
    
    private List<IncidentUpdate> updates;
}