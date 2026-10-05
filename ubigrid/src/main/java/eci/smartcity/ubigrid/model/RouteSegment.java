package eci.smartcity.ubigrid.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a segment of a route in the MongoDB database. This class
 * maps to documents in the "routeSegments" collection.
 */
@Document(collection = "route_segments")
public class RouteSegment {

	@Id
	private String id;

	@Field("routeId")
	private String routeId;

	@Field("segmentIndex")
	private int segmentIndex;

	@Field("startLocation")
	private double[] startLocation; // [longitude, latitude]

	@Field("endLocation")
	private double[] endLocation; // [longitude, latitude]

	@Field("startLocationId")
	private String startLocationId;

	@Field("endLocationId")
	private String endLocationId;

	@Field("distanceMeters")
	private double distanceMeters;

	@Field("durationSeconds")
	private int durationSeconds;

	@Field("trafficLevel")
	private String trafficLevel; // LIGHT, MODERATE, HEAVY

	@Field("roadType")
	private String roadType; // HIGHWAY, ARTERIAL, RESIDENTIAL, etc.

	@Field("instructions")
	private String instructions;

	@Field("maneuverType")
	private String maneuverType; // STRAIGHT, RIGHT_TURN, LEFT_TURN, UTURN, etc.

	@Field("tollRequired")
	private boolean tollRequired;

	// Constructors
	public RouteSegment() {
		// Default constructor required by MongoDB
	}

	public RouteSegment(String routeId, int segmentIndex, double[] startLocation, double[] endLocation) {
		this.routeId = routeId;
		this.segmentIndex = segmentIndex;
		this.startLocation = startLocation;
		this.endLocation = endLocation;
	}

	// Helper methods for setting coordinates with the correct order
	public void setStartPoint(double latitude, double longitude) {
		this.startLocation = new double[] { longitude, latitude }; // MongoDB uses [longitude, latitude] order
	}

	public void setEndPoint(double latitude, double longitude) {
		this.endLocation = new double[] { longitude, latitude }; // MongoDB uses [longitude, latitude] order
	}

	// Getters and setters
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

	public int getSegmentIndex() {
		return segmentIndex;
	}

	public void setSegmentIndex(int segmentIndex) {
		this.segmentIndex = segmentIndex;
	}

	public double[] getStartLocation() {
		return startLocation;
	}

	public void setStartLocation(double[] startLocation) {
		this.startLocation = startLocation;
	}

	public double[] getEndLocation() {
		return endLocation;
	}

	public void setEndLocation(double[] endLocation) {
		this.endLocation = endLocation;
	}

	public String getStartLocationId() {
		return startLocationId;
	}

	public void setStartLocationId(String startLocationId) {
		this.startLocationId = startLocationId;
	}

	public String getEndLocationId() {
		return endLocationId;
	}

	public void setEndLocationId(String endLocationId) {
		this.endLocationId = endLocationId;
	}

	public double getDistanceMeters() {
		return distanceMeters;
	}

	public void setDistanceMeters(double distanceMeters) {
		this.distanceMeters = distanceMeters;
	}

	public int getDurationSeconds() {
		return durationSeconds;
	}

	public void setDurationSeconds(int durationSeconds) {
		this.durationSeconds = durationSeconds;
	}

	public String getTrafficLevel() {
		return trafficLevel;
	}

	public void setTrafficLevel(String trafficLevel) {
		this.trafficLevel = trafficLevel;
	}

	public String getRoadType() {
		return roadType;
	}

	public void setRoadType(String roadType) {
		this.roadType = roadType;
	}

	public String getInstructions() {
		return instructions;
	}

	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	public String getManeuverType() {
		return maneuverType;
	}

	public void setManeuverType(String maneuverType) {
		this.maneuverType = maneuverType;
	}

	public boolean isTollRequired() {
		return tollRequired;
	}

	public void setTollRequired(boolean tollRequired) {
		this.tollRequired = tollRequired;
	}

	@Override
	public String toString() {
		return "RouteSegment{" + "id='" + id + '\'' + ", routeId='" + routeId + '\'' + ", segmentIndex=" + segmentIndex
				+ ", instructions='" + instructions + '\'' + ", distanceMeters=" + distanceMeters + ", durationSeconds="
				+ durationSeconds + '}';
	}
}
