package eci.smartcity.ubigrid.service;

import java.util.Map;

/**
 * Service interface for traffic-related operations.
 */
public interface TrafficService {
    
    /**
     * Get the current traffic conditions for a route segment
     * 
     * @param startLat Starting latitude
     * @param startLon Starting longitude
     * @param endLat Ending latitude
     * @param endLon Ending longitude
     * @return Traffic level (LIGHT, MODERATE, HEAVY)
     */
    String getTrafficCondition(double startLat, double startLon, double endLat, double endLon);
    
    /**
     * Get the estimated travel time between two points considering current traffic
     * 
     * @param startLat Starting latitude
     * @param startLon Starting longitude
     * @param endLat Ending latitude
     * @param endLon Ending longitude
     * @return Estimated travel time in seconds
     */
    int getEstimatedTravelTime(double startLat, double startLon, double endLat, double endLon);
    
    /**
     * Get a map of road closures in the area
     * 
     * @param centerLat Center latitude of the area
     * @param centerLon Center longitude of the area
     * @param radiusKm Radius in kilometers
     * @return Map of road closure locations to their descriptions
     */
    Map<String, String> getRoadClosures(double centerLat, double centerLon, double radiusKm);
    
    /**
     * Get the congestion level (0.0-1.0) for a specific area
     * 
     * @param lat Latitude
     * @param lon Longitude
     * @param radiusKm Radius in kilometers
     * @return Congestion level between 0.0 (no congestion) and 1.0 (maximum congestion)
     */
    double getCongestionLevel(double lat, double lon, double radiusKm);
    
    /**
     * Check if a specific road segment has toll requirements
     * 
     * @param startLat Starting latitude
     * @param startLon Starting longitude
     * @param endLat Ending latitude
     * @param endLon Ending longitude
     * @return True if toll is required, false otherwise
     */
    boolean hasToll(double startLat, double startLon, double endLat, double endLon);
    
    /**
     * Get the road type for a specific segment
     * 
     * @param lat Latitude
     * @param lon Longitude
     * @return Road type (HIGHWAY, ARTERIAL, RESIDENTIAL, etc.)
     */
    String getRoadType(double lat, double lon);
}

