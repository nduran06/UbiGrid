package eci.smartcity.ubigrid.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import eci.smartcity.ubigrid.service.TrafficService;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Implementation of the TrafficService interface.
 * In a real system, this would connect to traffic data providers or IoT sensors.
 * This implementation provides simulated traffic data for demonstration purposes.
 */
@Service
public class TrafficServiceImpl implements TrafficService {

    private static final Logger logger = LoggerFactory.getLogger(TrafficServiceImpl.class);
    private final Random random = new Random();
    
    // Cache to provide consistent values for the same locations
    private final Map<String, String> trafficConditionCache = new HashMap<>();
    private final Map<String, Integer> travelTimeCache = new HashMap<>();
    private final Map<String, Boolean> tollCache = new HashMap<>();
    private final Map<String, String> roadTypeCache = new HashMap<>();
    private final Map<String, Double> congestionCache = new HashMap<>();
    
    // Simulated road closures
    private final Map<String, String> roadClosures = new HashMap<>();
    
    public TrafficServiceImpl() {
        // Initialize some simulated road closures
        roadClosures.put("37.7749,-122.4194", "Construction on Market Street");
        roadClosures.put("40.7128,-74.0060", "Road maintenance on Broadway");
        roadClosures.put("34.0522,-118.2437", "Event closure on Figueroa Street");
        roadClosures.put("41.8781,-87.6298", "Accident on Michigan Avenue");
        roadClosures.put("51.5074,-0.1278", "Parade on Oxford Street");
    }
    
    @Override
    public String getTrafficCondition(double startLat, double startLon, double endLat, double endLon) {
        String key = String.format("%.4f,%.4f-%.4f,%.4f", startLat, startLon, endLat, endLon);
        
        if (!trafficConditionCache.containsKey(key)) {
            // In a real system, this would query a traffic data service
            // For simulation, we'll generate random traffic conditions
            String[] conditions = {"LIGHT", "MODERATE", "HEAVY"};
            int index = random.nextInt(conditions.length);
            
            // Bias toward moderate traffic
            if (random.nextDouble() < 0.4) {
                index = 1; // MODERATE
            }
            
            trafficConditionCache.put(key, conditions[index]);
        }
        
        return trafficConditionCache.get(key);
    }
    
    @Override
    public int getEstimatedTravelTime(double startLat, double startLon, double endLat, double endLon) {
        String key = String.format("%.4f,%.4f-%.4f,%.4f", startLat, startLon, endLat, endLon);
        
        if (!travelTimeCache.containsKey(key)) {
            // Calculate the direct distance between points (in meters)
            double distance = calculateDistance(startLat, startLon, endLat, endLon);
            
            // Get the traffic condition to adjust travel time
            String trafficCondition = getTrafficCondition(startLat, startLon, endLat, endLon);
            
            // Base travel time: assume 50 km/h (13.9 m/s) average speed
            int baseTime = (int) (distance / 13.9);
            
            // Apply traffic multiplier
            double multiplier;
            switch (trafficCondition) {
                case "HEAVY":
                    multiplier = 2.5 + random.nextDouble(); // 2.5-3.5x slowdown
                    break;
                case "MODERATE":
                    multiplier = 1.5 + random.nextDouble(); // 1.5-2.5x slowdown
                    break;
                default: // LIGHT
                    multiplier = 1.0 + random.nextDouble() * 0.5; // 1.0-1.5x slowdown
            }
            
            int travelTime = (int) (baseTime * multiplier);
            travelTimeCache.put(key, travelTime);
        }
        
        return travelTimeCache.get(key);
    }
    
    @Override
    public Map<String, String> getRoadClosures(double centerLat, double centerLon, double radiusKm) {
        Map<String, String> closuresInArea = new HashMap<>();
        
        // Check each closure to see if it's within the radius
        for (Map.Entry<String, String> entry : roadClosures.entrySet()) {
            String[] coords = entry.getKey().split(",");
            double closureLat = Double.parseDouble(coords[0]);
            double closureLon = Double.parseDouble(coords[1]);
            
            // Calculate distance between center and closure
            double distance = calculateDistance(centerLat, centerLon, closureLat, closureLon) / 1000.0; // Convert to km
            
            if (distance <= radiusKm) {
                closuresInArea.put(entry.getKey(), entry.getValue());
            }
        }
        
        // Add a random closure occasionally for simulation
        if (random.nextDouble() < 0.1) {
            // Generate a point within the radius
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = random.nextDouble() * radiusKm;
            
            // Convert to lat/lon (approximate)
            double latOffset = distance * Math.cos(angle) / 111.0; // 1 degree lat = ~111 km
            double lonOffset = distance * Math.sin(angle) / (111.0 * Math.cos(Math.toRadians(centerLat)));
            
            double newLat = centerLat + latOffset;
            double newLon = centerLon + lonOffset;
            
            String key = String.format("%.4f,%.4f", newLat, newLon);
            String[] reasons = {
                "Traffic accident",
                "Construction work",
                "Special event",
                "Temporary closure",
                "Road maintenance"
            };
            
            closuresInArea.put(key, reasons[random.nextInt(reasons.length)]);
        }
        
        return closuresInArea;
    }
    
    @Override
    public double getCongestionLevel(double lat, double lon, double radiusKm) {
        String key = String.format("%.4f,%.4f-%.1f", lat, lon, radiusKm);
        
        if (!congestionCache.containsKey(key)) {
            // Time-based congestion - higher during rush hours
            int hour = java.time.LocalTime.now().getHour();
            double baseCongestion;
            
            // Rush hour logic
            if ((hour >= 7 && hour <= 9) || (hour >= 16 && hour <= 18)) {
                baseCongestion = 0.6 + random.nextDouble() * 0.4; // 0.6-1.0 during rush hour
            } else if ((hour >= 10 && hour <= 15) || (hour >= 19 && hour <= 21)) {
                baseCongestion = 0.3 + random.nextDouble() * 0.3; // 0.3-0.6 during business hours
            } else {
                baseCongestion = random.nextDouble() * 0.3; // 0.0-0.3 during off hours
            }
            
            // Add some randomization based on location
            double locationFactor = (Math.sin(lat * 10) + Math.cos(lon * 10)) / 2.0;
            locationFactor = (locationFactor + 1) / 2.0; // Normalize to 0-1
            
            double congestion = baseCongestion * 0.8 + locationFactor * 0.2;
            congestion = Math.min(1.0, Math.max(0.0, congestion)); // Ensure it's between 0 and 1
            
            congestionCache.put(key, congestion);
        }
        
        return congestionCache.get(key);
    }
    
    @Override
    public boolean hasToll(double startLat, double startLon, double endLat, double endLon) {
        String key = String.format("%.4f,%.4f-%.4f,%.4f", startLat, startLon, endLat, endLon);
        
        if (!tollCache.containsKey(key)) {
            // Simulate some toll roads based on location patterns
            // In a real system, this would check against actual toll road data
            
            // Check if either point is near a "toll area"
            boolean startNearToll = isNearTollArea(startLat, startLon);
            boolean endNearToll = isNearTollArea(endLat, endLon);
            
            // If both points are near toll areas, higher chance of toll
            if (startNearToll && endNearToll) {
                tollCache.put(key, random.nextDouble() < 0.8);
            }
            // If one point is near toll area, moderate chance of toll
            else if (startNearToll || endNearToll) {
                tollCache.put(key, random.nextDouble() < 0.4);
            }
            // If neither point is near toll area, low chance of toll
            else {
                tollCache.put(key, random.nextDouble() < 0.1);
            }
        }
        
        return tollCache.get(key);
    }
    
    @Override
    public String getRoadType(double lat, double lon) {
        String key = String.format("%.4f,%.4f", lat, lon);
        
        if (!roadTypeCache.containsKey(key)) {
            // Simulate road types based on location patterns
            // In a real system, this would query map data
            
            // Generate value from coordinates for consistent results
            double value = (Math.sin(lat * 100) + Math.cos(lon * 100)) / 2.0;
            value = (value + 1) / 2.0; // Normalize to 0-1
            
            String roadType;
            if (value < 0.1) {
                roadType = "HIGHWAY";
            } else if (value < 0.3) {
                roadType = "ARTERIAL";
            } else if (value < 0.6) {
                roadType = "COLLECTOR";
            } else if (value < 0.9) {
                roadType = "RESIDENTIAL";
            } else {
                roadType = "SERVICE";
            }
            
            roadTypeCache.put(key, roadType);
        }
        
        return roadTypeCache.get(key);
    }
    
    // Helper method to check if a point is near a simulated toll area
    private boolean isNearTollArea(double lat, double lon) {
        // Define several "toll centers" (these would be real toll road coordinates in a production system)
        double[][] tollCenters = {
            {37.789, -122.389}, // San Francisco Bay Bridge area
            {40.712, -74.006},  // NYC area
            {34.052, -118.243}, // LA area
            {41.878, -87.629},  // Chicago area
            {51.507, -0.127}    // London area
        };
        
        // Check if the point is near any toll center
        for (double[] center : tollCenters) {
            double distance = calculateDistance(lat, lon, center[0], center[1]) / 1000.0; // Convert to km
            if (distance < 10.0) { // Within 10km of a toll center
                return true;
            }
        }
        
        return false;
    }
    
    // Calculate distance between two points using Haversine formula
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth radius in meters
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // Distance in meters
    }
}