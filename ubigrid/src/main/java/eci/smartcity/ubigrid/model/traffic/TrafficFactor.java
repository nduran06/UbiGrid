package eci.smartcity.ubigrid.model.traffic;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents factors influencing traffic conditions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrafficFactor {
    
    private String factorType;  // WEATHER, EVENT, INCIDENT, TIME_OF_DAY, etc.
    private String description;
    private Double impactScore;  // 0-1 scale indicating impact on traffic
    private String sourceId;     // ID of the source data this factor is derived from
    private Map<String, Object> additionalData; // Any additional factor-specific data
}
