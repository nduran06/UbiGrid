package eci.smartcity.ubigrid.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import eci.smartcity.ubigrid.model.traffic.TrafficAnalysisResult;
import eci.smartcity.ubigrid.model.traffic.TrafficPredictionRequest;

/**
 * Predicts future traffic conditions using ML models
 */
public interface TrafficPredictionService {

	/**
	 * Predict traffic conditions based on current data and history
	 */
	TrafficAnalysisResult predictTraffic(TrafficPredictionRequest request);

	/**
	 * Predict traffic for multiple locations simultaneously
	 */
	Map<String, TrafficAnalysisResult> batchPredictTraffic(List<String> locationIds, LocalDateTime predictionTime,
			Integer predictionHorizonMinutes) throws Exception;

	/**
	 * Get a cached traffic prediction if available
	 */
	TrafficAnalysisResult getCachedTrafficPrediction(TrafficPredictionRequest request);
}