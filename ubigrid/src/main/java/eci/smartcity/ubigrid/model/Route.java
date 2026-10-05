package eci.smartcity.ubigrid.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import eci.smartcity.ubigrid.model.enums.RoutePreference;

import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing an active route in the MongoDB database.
 * This class maps to documents in the "activeRoutes" collection.
 */
@Document(collection = "routes")
public class Route {

    @Id
    private String id;
    
    @Indexed
    @Field("vehicleId")
    private String vehicleId;
    
    @Field("routeIds")
    private List<String> routeIds; // Can store multiple route options
    
    @Field("currentRouteIndex")
    private int currentRouteIndex; // Index of the currently selected route
    
    @Field("active")
	private Boolean active;
    
    @Field("status")
	private String status; // ACTIVE, COMPLETED, CANCELLED
    
    @Field("startLocation")
    private double[] startLocation; // [longitude, latitude]
    
    @Field("destinationLocation")
    private double[] destinationLocation; // [longitude, latitude]
    
    @Field("destinationLocationId")
    private String destinationLocationId;
    
    @Field("routePreference")
    private String routePreference; // Maps to RoutePreference enum name
    
    @Field("totalDistanceMeters")
    private double totalDistanceMeters;
    
    @Field("totalDurationSeconds")
    private int totalDurationSeconds;
    
    @Field("createdAt")
    private LocalDateTime createdAt;
    
    @Field("updatedAt")
    private LocalDateTime updatedAt;
    
    @Field("estimatedArrivalTime")
    private LocalDateTime estimatedArrivalTime;
    
    @Field("currentSegmentIndex")
    private int currentSegmentIndex;
    
    @Field("progressPercentage")
    private double progressPercentage;
    
    // Constructors
    public Route() {
        // Default constructor required by MongoDB
    	this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.currentRouteIndex = 0;
        this.currentSegmentIndex = 0;
        this.progressPercentage = 0.0;
    }
    
    // Helper methods for setting coordinates with the correct order
    public void setStartPoint(double latitude, double longitude) {
        this.startLocation = new double[] { longitude, latitude }; // MongoDB uses [longitude, latitude] order
    }
    
    public void setDestinationPoint(double latitude, double longitude) {
        this.destinationLocation = new double[] { longitude, latitude }; // MongoDB uses [longitude, latitude] order
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getVehicleId() {
        return vehicleId;
    }
    
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }
    
    public List<String> getRouteIds() {
        return routeIds;
    }
    
    public void setRouteIds(List<String> routeIds) {
        this.routeIds = routeIds;
    }
    
    public int getCurrentRouteIndex() {
        return currentRouteIndex;
    }
    
    public void setCurrentRouteIndex(int currentRouteIndex) {
        this.currentRouteIndex = currentRouteIndex;
    }
    
    public Boolean isActive() {
		return this.active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}
    
    public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
    
    public double[] getStartLocation() {
        return startLocation;
    }
    
    public void setStartLocation(double[] startLocation) {
        this.startLocation = startLocation;
    }
    
    public double[] getDestinationLocation() {
        return destinationLocation;
    }
    
    public void setDestinationLocation(double[] destinationLocation) {
        this.destinationLocation = destinationLocation;
    }
    
    public String getDestinationLocationId() {
        return destinationLocationId;
    }
    
    public void setDestinationLocationId(String destinationLocationId) {
        this.destinationLocationId = destinationLocationId;
    }
    
    public String getRoutePreference() {
        return routePreference;
    }
    
    public void setRoutePreference(String routePreference) {
        this.routePreference = routePreference;
    }
    
    public void setRoutePreference(RoutePreference routePreference) {
        this.routePreference = routePreference.name();
    }
    
    public double getTotalDistanceMeters() {
        return totalDistanceMeters;
    }
    
    public void setTotalDistanceMeters(double totalDistanceMeters) {
        this.totalDistanceMeters = totalDistanceMeters;
    }
    
    public int getTotalDurationSeconds() {
        return totalDurationSeconds;
    }
    
    public void setTotalDurationSeconds(int totalDurationSeconds) {
        this.totalDurationSeconds = totalDurationSeconds;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getEstimatedArrivalTime() {
        return estimatedArrivalTime;
    }
    
    public void setEstimatedArrivalTime(LocalDateTime estimatedArrivalTime) {
        this.estimatedArrivalTime = estimatedArrivalTime;
    }
    
    public int getCurrentSegmentIndex() {
        return currentSegmentIndex;
    }
    
    public void setCurrentSegmentIndex(int currentSegmentIndex) {
        this.currentSegmentIndex = currentSegmentIndex;
    }
    
    public double getProgressPercentage() {
        return progressPercentage;
    }
    
    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
    
    public String getCurrentRouteId() {
        if (routeIds != null && !routeIds.isEmpty() && currentRouteIndex < routeIds.size()) {
            return routeIds.get(currentRouteIndex);
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "ActiveRoute{" +
                "id='" + id + '\'' +
                ", vehicleId='" + vehicleId + '\'' +
                ", status=" + status +
                ", destinationLocationId='" + destinationLocationId + '\'' +
                ", routePreference='" + routePreference + '\'' +
                ", totalDistanceMeters=" + totalDistanceMeters +
                ", totalDurationSeconds=" + totalDurationSeconds +
                ", estimatedArrivalTime=" + estimatedArrivalTime +
                ", progress=" + progressPercentage + "%" +
                '}';
    }
}