package eci.smartcity.ubigrid.service.impl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eci.smartcity.ubigrid.model.GeoLocation;
import eci.smartcity.ubigrid.model.RoadSegment;
import eci.smartcity.ubigrid.model.Route;
import eci.smartcity.ubigrid.model.RouteSegment;
import eci.smartcity.ubigrid.model.Vehicle;
import eci.smartcity.ubigrid.model.VehicleLocationHistory;
import eci.smartcity.ubigrid.repository.RoadSegmentRepository;
import eci.smartcity.ubigrid.repository.RouteRepository;
import eci.smartcity.ubigrid.repository.VehicleLocationHistoryRepository;
import eci.smartcity.ubigrid.repository.VehicleRepository;
import eci.smartcity.ubigrid.service.RoadNetworkService;
import eci.smartcity.ubigrid.service.VehicleTrackingService;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;

/**
 * Implementation of the VehicleTrackingService interface.
 */
@Service
@Slf4j
public class VehicleTrackingServiceImpl implements VehicleTrackingService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleTrackingServiceImpl.class);
    
    // Speed threshold in km/h below which a vehicle is considered not moving
    @Value("${vehicle.tracking.movement.threshold:5.0}")
    private double movementThresholdKmh;
    
    // Time window in minutes to check for movement
    @Value("${vehicle.tracking.movement.timeWindow:5}")
    private int movementTimeWindowMinutes;
    
    private final VehicleRepository vehicleRepository;
    private final VehicleLocationHistoryRepository locationHistoryRepository;
    private final RouteRepository routeRepository;
    
    @Autowired
    public VehicleTrackingServiceImpl(
            VehicleRepository vehicleRepository,
            VehicleLocationHistoryRepository locationHistoryRepository,
            RouteRepository routeRepository) {
        this.vehicleRepository = vehicleRepository;
        this.locationHistoryRepository = locationHistoryRepository;
        this.routeRepository = routeRepository;
    }
    
    @Override
    @Transactional
    public Vehicle updateVehicleLocation(
            String vehicleId, 
            double latitude, 
            double longitude, 
            LocalDateTime timestamp,
            Map<String, Object> additionalData) {
        
        logger.debug("Updating location for vehicle {}: [{}, {}] at {}", 
                vehicleId, longitude, latitude, timestamp);
        
        // Find or create the vehicle
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + vehicleId));
        
        // Update vehicle's location
        vehicle.setCurrentLocation(latitude, longitude);
        vehicle.setLastUpdateTime(timestamp != null ? timestamp : LocalDateTime.now());
        
        // Save the updated vehicle
        vehicle = vehicleRepository.save(vehicle);
        
        // Create and save location history entry
        VehicleLocationHistory locationHistory = new VehicleLocationHistory(vehicleId, latitude, longitude);
        if (timestamp != null) {
            locationHistory.setTimestamp(timestamp);
        }
        
        // Extract speed and heading from additional data if available
        if (additionalData != null) {
            if (additionalData.containsKey("speed")) {
                locationHistory.setSpeed((Double) additionalData.get("speed"));
            }
            
            if (additionalData.containsKey("heading")) {
                locationHistory.setHeading((Double) additionalData.get("heading"));
            }
            
            if (additionalData.containsKey("elevation")) {
                locationHistory.setElevation((Double) additionalData.get("elevation"));
            }
            
            if (additionalData.containsKey("accuracy")) {
                locationHistory.setAccuracy((Double) additionalData.get("accuracy"));
            }
            
            // Add all additional data
            locationHistory.setAdditionalData(additionalData);
        }
        
        locationHistoryRepository.save(locationHistory);
        
        // Update route progress if vehicle has an active route
        updateRouteProgress(vehicleId, latitude, longitude);
        
        return vehicle;
    }
    
    @Override
    public GeoLocation getVehicleLocation(String vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
        return vehicle != null ? vehicle.getCurrentLocation() : null;
    }
    
    @Override
    public List<VehicleLocationHistory> getVehicleLocationHistory(
            String vehicleId, 
            LocalDateTime startTime, 
            LocalDateTime endTime) {
        
        return locationHistoryRepository.findByVehicleIdAndTimestampBetweenOrderByTimestampAsc(
                vehicleId, startTime, endTime);
    }
    
    @Override
    public List<Vehicle> findVehiclesInArea(
            double centerLatitude, 
            double centerLongitude, 
            double radiusKm) {
        
        // Convert radius from km to meters
        double radiusMeters = radiusKm * 1000;
        
        return vehicleRepository.findVehiclesNearPoint(
                centerLongitude, centerLatitude, radiusMeters);
    }
    
    @Override
    public List<Vehicle> findVehiclesByType(String vehicleType) {
        return vehicleRepository.findByVehicleType(vehicleType);
    }
    
    @Override
    public double calculateDistanceTraveled(
            String vehicleId, 
            LocalDateTime startTime, 
            LocalDateTime endTime) {
        
        List<VehicleLocationHistory> locationHistory = 
                locationHistoryRepository.findByVehicleIdAndTimestampBetweenOrderByTimestampAsc(
                        vehicleId, startTime, endTime);
        
        if (locationHistory.size() < 2) {
            return 0.0;
        }
        
        double totalDistance = 0.0;
        VehicleLocationHistory previous = locationHistory.get(0);
        
        for (int i = 1; i < locationHistory.size(); i++) {
            VehicleLocationHistory current = locationHistory.get(i);
            
            totalDistance += calculateDistance(
                    previous.getLatitude(), previous.getLongitude(),
                    current.getLatitude(), current.getLongitude());
            
            previous = current;
        }
        
        return totalDistance;
    }
    
    @Override
    public LocalDateTime getEstimatedTimeOfArrival(String vehicleId) {
        Optional<Route> routeOpt = routeRepository.findByVehicleIdAndActiveIsTrue(vehicleId);
        
        if (routeOpt.isPresent() && routeOpt.get().isActive()) {
            return routeOpt.get().getEstimatedArrivalTime();
        }
        
        return null;
    }
    
    @Override
    public boolean isVehicleMoving(String vehicleId) {
        // Get recent location history for the vehicle
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timeWindow = now.minusMinutes(movementTimeWindowMinutes);
        
        List<VehicleLocationHistory> recentLocations = 
                locationHistoryRepository.findByVehicleIdAndTimestampBetweenOrderByTimestampAsc(
                        vehicleId, timeWindow, now);
        
        if (recentLocations.isEmpty()) {
            return false; // No recent locations, assume not moving
        }
        
        // Check if we have speed data
        for (VehicleLocationHistory location : recentLocations) {
            if (location.getSpeed() != null && location.getSpeed() > movementThresholdKmh) {
                return true; // Speed above threshold, definitely moving
            }
        }
        
        // If we don't have speed data or speed is below threshold, check position changes
        if (recentLocations.size() >= 2) {
            VehicleLocationHistory oldest = recentLocations.get(0);
            VehicleLocationHistory newest = recentLocations.get(recentLocations.size() - 1);
            
            double distance = calculateDistance(
                    oldest.getLatitude(), oldest.getLongitude(),
                    newest.getLatitude(), newest.getLongitude());
            
            // Calculate time difference in seconds
            long secondsDifference = Duration.between(oldest.getTimestamp(), newest.getTimestamp()).getSeconds();
            
            if (secondsDifference > 0) {
                // Calculate speed in km/h
                double speedKmh = (distance / 1000.0) / (secondsDifference / 3600.0);
                return speedKmh > movementThresholdKmh;
            }
        }
        
        return false;
    }
    
    // Helper method to update the progress of an active route
    private void updateRouteProgress(String vehicleId, double latitude, double longitude) {
        Optional<Route> routeOpt = routeRepository.findByVehicleIdAndActiveIsTrue(vehicleId);
        
        if (!routeOpt.isPresent() || !routeOpt.get().isActive()) {
            return; // No active route
        }
        
        Route route = routeOpt.get();
        double[] destinationLocation = route.getDestinationLocation();
        
        if (destinationLocation == null || destinationLocation.length < 2) {
            return; // No destination set
        }
        
        // Calculate total route distance
        double totalDistance = route.getTotalDistanceMeters();
        
        // Calculate remaining distance
        double remainingDistance = calculateDistance(
                latitude, longitude,
                destinationLocation[1], destinationLocation[0]); // [1]=lat, [0]=lon
        
        // Update progress percentage
        if (totalDistance > 0) {
            double progress = 100 * (1 - (remainingDistance / totalDistance));
            // Cap progress between 0-100
            progress = Math.min(100, Math.max(0, progress));
            route.setProgressPercentage(progress);
            
            // Update the estimated arrival time based on progress
            if (progress < 100) {
                int totalDuration = route.getTotalDurationSeconds();
                int remainingDuration = (int) (totalDuration * (1 - (progress / 100)));
                LocalDateTime now = LocalDateTime.now();
                route.setEstimatedArrivalTime(now.plusSeconds(remainingDuration));
            }
            
            routeRepository.save(route);
        }
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