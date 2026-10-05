package eci.smartcity.ubigrid.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import eci.smartcity.ubigrid.model.traffic.TrafficAnalysisResult;
import eci.smartcity.ubigrid.model.traffic.TrafficData;

/**
 * Analyzes traffic data to provide insights and analytics
 */
public interface TrafficAnalysisService {

	/**
	 * Process raw traffic data from sensors and cameras
	 */
	void processTrafficData(TrafficData trafficData);

	/**
	 * Analyze traffic data to generate insights
	 */
	void analyzeTrafficData(TrafficData trafficData);

	/**
	 * Get the latest analysis for a specific location
	 */
	Optional<TrafficAnalysisResult> getLatestAnalysisForLocation(String locationId);

	/**
	 * Get current congestion levels across all monitored road segments
	 */
	Map<String, Double> getCurrentCongestionMap();

	/**
	 * Get historical traffic analysis for a specific location and time range
	 */
	List<TrafficAnalysisResult> getHistoricalAnalysis(String locationId, LocalDateTime startTime,
			LocalDateTime endTime);
}