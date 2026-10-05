package eci.smartcity.ubigrid.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import eci.smartcity.ubigrid.model.enums.ConnectionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a connection between two road segments in the network
 */
@Document(collection = "road_connections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadConnection {
    
    @Id
    private String connectionId;
    
    @Indexed
    private String sourceSegmentId;
    
    @Indexed
    private String targetSegmentId;
    
    private ConnectionType connectionType;
    
    private Double turnAngle;  // In degrees, 0 = straight, negative = left, positive = right
    
    private Double lengthMeters;
    
    private Double averageTraversalTimeSeconds;
    
    private Double currentTraversalTimeSeconds;
    
    private Boolean isRestricted;
    
    @Builder.Default
    private Map<String, Boolean> vehicleRestrictions = new HashMap<>();
}
