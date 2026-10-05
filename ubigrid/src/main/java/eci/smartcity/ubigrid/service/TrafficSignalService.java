package eci.smartcity.ubigrid.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * Manages traffic signals across the network
 */
public interface TrafficSignalService {

	/**
	 * Optimize traffic signals based on current flows
	 */
	void optimizeSignals(Set<String> signalIds, Map<String, Integer> inboundFlows, Map<String, Integer> outboundFlows);

	/**
	 * Apply critical signal strategy for severe congestion
	 */
	void applyCriticalSignalStrategy(Set<String> signalIds);

	/**
	 * Schedule a future signal optimization
	 */
	void scheduleFutureOptimization(Set<String> signalIds, LocalDateTime scheduledTime);
}
