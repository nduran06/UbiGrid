package eci.smartcity.ubigrid.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Manages information about events affecting traffic
 */
public interface EventService {

	/**
	 * Get nearby events that might affect traffic
	 */
	String getNearbyEvents(Double latitude, Double longitude, LocalDateTime eventTime, Double radiusMeters);

	/**
	 * Get events affecting traffic for a specific location
	 */
	List<Map<String, Object>> getEventsAffectingTraffic(String locationId, LocalDateTime eventTime);
}