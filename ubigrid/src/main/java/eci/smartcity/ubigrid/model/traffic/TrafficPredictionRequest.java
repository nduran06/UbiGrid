package eci.smartcity.ubigrid.model.traffic;

import java.time.LocalDateTime;

import eci.smartcity.ubigrid.model.traffic.enums.PredictionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a request for traffic prediction
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrafficPredictionRequest {
    
    private String locationId;
    private LocalDateTime predictionTimestamp;
    private Integer predictionHorizonMinutes;
    private Boolean includeWeatherFactors;
    private Boolean includeEventFactors;
    private Boolean includeHistoricalComparison;
    private String roadSegmentId;
    
    // Optional constraints
    private Double minimumCongestionLevel;
    private Double maximumCongestionLevel;
    
    // Prediction type
    private PredictionType predictionType; // SHORT_TERM, MEDIUM_TERM, LONG_TERM
}