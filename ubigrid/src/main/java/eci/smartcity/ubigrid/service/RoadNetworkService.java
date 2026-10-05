package eci.smartcity.ubigrid.service;

import java.util.List;

import eci.smartcity.ubigrid.model.RoadSegment;

/**
 * Manages road network topology and status
 */
public interface RoadNetworkService {

	/**
	 * Update road segment congestion level
	 */
	void updateRoadSegmentCongestion(String roadSegmentId, double congestionLevel);

	/**
	 * Update road segment vehicle count
	 */
	void updateRoadSegmentVehicleCount(String roadSegmentId, int vehicleCount);

	/**
	 * Update road segment average speed
	 */
	void updateRoadSegmentAverageSpeed(String roadSegmentId, double averageSpeed);

	/**
	 * Find all road segments within a bounding box
	 */
	List<RoadSegment> findRoadSegmentsInArea(double minLon, double minLat, double maxLon, double maxLat);

	/**
	 * Find road segments near a point
	 */
	List<RoadSegment> findRoadSegmentsNearPoint(double longitude, double latitude, double radiusMeters);

	/**
	 * Update traffic signal state
	 */
	void updateTrafficSignalState(String roadSegmentId, String signalId, String state);
}
