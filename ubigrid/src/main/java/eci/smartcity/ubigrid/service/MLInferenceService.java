package eci.smartcity.ubigrid.service;

import java.util.List;
import java.util.Map;

import eci.smartcity.ubigrid.model.traffic.TrafficData;

/**
 * Interfaces with ML models for traffic predictions
 */
public interface MLInferenceService {

	/**
	 * Predict traffic conditions using ML models
	 */
	Map<String, Object> predictTrafficConditions(TrafficData currentData, List<TrafficData> recentData);

	/**
	 * Get fallback predictions when ML services are unavailable
	 */
	Map<String, Object> getFallbackPrediction(TrafficData currentData, List<TrafficData> recentData);
}
