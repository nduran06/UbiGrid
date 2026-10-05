package eci.smartcity.ubigrid.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

/**
 * Entity representing a historical vehicle location record in the MongoDB
 * database. This class maps to documents in the "vehicleLocationHistory"
 * collection.
 */
@Document(collection = "vehicle_location_history")
public class VehicleLocationHistory {

	@Id
	private String id;

	@Indexed
	@Field("vehicleId")
	private String vehicleId;

	@Indexed
	@Field("timestamp")
	private LocalDateTime timestamp;

	@Field("location")
	private double[] location; // [longitude, latitude]

	@Field("speed")
	private Double speed; // in km/h

	@Field("heading")
	private Double heading; // in degrees (0-360)

	@Field("elevation")
	private Double elevation; // in meters

	@Field("accuracy")
	private Double accuracy; // in meters

	@Field("additionalData")
	private Map<String, Object> additionalData;

	// Constructors
	public VehicleLocationHistory() {
		// Default constructor required by MongoDB
		this.timestamp = LocalDateTime.now();
		this.additionalData = new HashMap<>();
	}

	public VehicleLocationHistory(String vehicleId, double latitude, double longitude) {
		this();
		this.vehicleId = vehicleId;
		this.setCoordinates(latitude, longitude);
	}

	public VehicleLocationHistory(String vehicleId, double latitude, double longitude, LocalDateTime timestamp) {
		this(vehicleId, latitude, longitude);
		this.timestamp = timestamp;
	}

	// Helper method for setting coordinates with the correct order
	public void setCoordinates(double latitude, double longitude) {
		this.location = new double[] { longitude, latitude }; // MongoDB uses [longitude, latitude] order
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

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public double[] getLocation() {
		return location;
	}

	public void setLocation(double[] location) {
		this.location = location;
	}

	public double getLatitude() {
		return location != null && location.length > 1 ? location[1] : 0;
	}

	public double getLongitude() {
		return location != null && location.length > 0 ? location[0] : 0;
	}

	public Double getSpeed() {
		return speed;
	}

	public void setSpeed(Double speed) {
		this.speed = speed;
	}

	public Double getHeading() {
		return heading;
	}

	public void setHeading(Double heading) {
		this.heading = heading;
	}

	public Double getElevation() {
		return elevation;
	}

	public void setElevation(Double elevation) {
		this.elevation = elevation;
	}

	public Double getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(Double accuracy) {
		this.accuracy = accuracy;
	}

	public Map<String, Object> getAdditionalData() {
		return additionalData;
	}

	public void setAdditionalData(Map<String, Object> additionalData) {
		this.additionalData = additionalData;
	}

	public void addAdditionalData(String key, Object value) {
		if (this.additionalData == null) {
			this.additionalData = new HashMap<>();
		}
		this.additionalData.put(key, value);
	}

	@Override
	public String toString() {
		return "VehicleLocationHistory{" + "id='" + id + '\'' + ", vehicleId='" + vehicleId + '\'' + ", timestamp="
				+ timestamp + ", location=[" + getLongitude() + ", " + getLatitude() + "]" + ", speed=" + speed
				+ ", heading=" + heading + '}';
	}
}

