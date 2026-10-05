package eci.smartcity.ubigrid.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import eci.smartcity.ubigrid.model.traffic.TrafficData;
import eci.smartcity.ubigrid.service.TrafficService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Implementation of the TrafficService interface.
 * Traffic conditions come from the latest sensor readings published by
 * TrafficFeedSimulator (fake data standing in for the city's sensor network),
 * interpolated to any point by inverse-distance weighting. Tolls, road types
 * and closures are still simulated locally.
 */
@Service
public class TrafficServiceImpl implements TrafficService {

    private static final Logger logger = LoggerFactory.getLogger(TrafficServiceImpl.class);
    private final Random random = new Random();
    
    private static final double LIGHT_BELOW = 0.40;
    private static final double MODERATE_BELOW = 0.70;
    private static final double MIN_SPEED_KMH = 5.0;

    private final TrafficFeedSimulator trafficFeed;

    // Cache to provide consistent values for the same locations
    private final Map<String, Boolean> tollCache = new HashMap<>();
    private final Map<String, String> roadTypeCache = new HashMap<>();
    
    // Simulated road closures
    private final Map<String, String> roadClosures = new HashMap<>();
    
    public TrafficServiceImpl(TrafficFeedSimulator trafficFeed) {
        this.trafficFeed = trafficFeed;
        // Initialize some simulated road closures
        roadClosures.put("37.7749,-122.4194", "Construction on Market Street");
        roadClosures.put("40.7128,-74.0060", "Road maintenance on Broadway");
        roadClosures.put("34.0522,-118.2437", "Event closure on Figueroa Street");
        roadClosures.put("41.8781,-87.6298", "Accident on Michigan Avenue");
        roadClosures.put("51.5074,-0.1278", "Parade on Oxford Street");
    }
    
    @Override
    public String getTrafficCondition(double startLat, double startLon, double endLat, double endLon) {
        double congestion = interpolate((startLat + endLat) / 2, (startLon + endLon) / 2)[0];
        if (congestion < LIGHT_BELOW) {
            return "LIGHT";
        }
        return congestion < MODERATE_BELOW ? "MODERATE" : "HEAVY";
    }

    @Override
    public double getAverageSpeedKmh(double lat, double lon) {
        return Math.max(MIN_SPEED_KMH, interpolate(lat, lon)[1]);
    }

    @Override
    public int getEstimatedTravelTime(double startLat, double startLon, double endLat, double endLon) {
        double distance = calculateDistance(startLat, startLon, endLat, endLon);
        double speedMetersPerSecond = getAverageSpeedKmh((startLat + endLat) / 2, (startLon + endLon) / 2) / 3.6;
        return (int) (distance / speedMetersPerSecond);
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
        return interpolate(lat, lon)[0];
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
    
    /**
     * Inverse-distance-weighted blend of every sensor's latest reading at a
     * point. Returns {congestion (0-1), average speed (km/h)}.
     */
    private double[] interpolate(double lat, double lon) {
        List<TrafficData> readings = trafficFeed.getLatestReadings();
        double weightSum = 0;
        double congestion = 0;
        double speed = 0;
        for (TrafficData reading : readings) {
            double km = calculateDistance(lat, lon, reading.getLocation().getLatitude(),
                    reading.getLocation().getLongitude()) / 1000.0;
            double weight = 1.0 / Math.pow(km + 0.2, 3);
            weightSum += weight;
            congestion += weight * reading.getCongestionLevel();
            speed += weight * reading.getAverageSpeed();
        }
        return new double[] { congestion / weightSum, speed / weightSum };
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