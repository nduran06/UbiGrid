package eci.smartcity.ubigrid.model.traffic;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the result of traffic analysis operations
 */
@Document(collection = "traffic_analysis_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrafficAnalysisResult {

	@Id
	private String analysisId;

	@Indexed
	private String locationId;

	@Indexed
	private LocalDateTime analysisTimestamp;

	private LocalDateTime predictionTimestamp;

	// Current traffic conditions
	private Double currentCongestionLevel;
	private Integer currentVehicleCount;
	private Double currentAverageSpeed;

	// Predicted traffic conditions
	private Double predictedCongestionLevel;
	private Integer predictedVehicleCount;
	private Double predictedAverageSpeed;

	// Anomaly detection
	private Boolean isAnomaly;
	private String anomalyType;
	private Double anomalyScore;
	private String anomalyDescription;

	// Factors affecting traffic
	private List<TrafficFactor> influencingFactors;

	// Historical comparison
	private Double historicalCongestionDelta; // % change from historical average
	private Double peakHourDelta; // % change from typical peak hour

	// Confidence scores
	private Map<String, Double> modelConfidenceScores;

	// Recommended actions
	private List<String> recommendedActions;

	// Road segment
	private String roadSegmentId;

	// Prediction metrics (for evaluation)
	private Map<String, Double> predictionMetrics;

	/**
	 * Represents a factor that influences traffic conditions
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class TrafficFactor {
		private String factorType; // WEATHER, EVENT, CONSTRUCTION, ACCIDENT, etc.
		private String description; // Detailed description of the factor
		private Double impactWeight; // Numerical weight of impact (0.0 to 1.0)
		private LocalDateTime startTime; // When this factor began affecting traffic
		private LocalDateTime endTime; // When this factor is expected to stop affecting traffic
		private Map<String, Object> additionalData; // Any additional factor-specific data
	}
}
