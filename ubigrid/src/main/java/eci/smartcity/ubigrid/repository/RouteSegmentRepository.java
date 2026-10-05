package eci.smartcity.ubigrid.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import eci.smartcity.ubigrid.model.RouteSegment;

/**
 * Repository interface for accessing RouteSegment entities in MongoDB.
 */
@Repository
public interface RouteSegmentRepository extends MongoRepository<RouteSegment, String> {

	// Find segments by route ID
	List<RouteSegment> findByRouteIdOrderBySegmentIndexAsc(String routeId);

	// Find segments by route ID and segment index range
	List<RouteSegment> findByRouteIdAndSegmentIndexBetweenOrderBySegmentIndexAsc(String routeId, int startIndex,
			int endIndex);

	// Find segments with toll requirements
	List<RouteSegment> findByTollRequiredIsTrue();

	// Find segments by road type
	List<RouteSegment> findByRoadType(String roadType);

	// Find segments by traffic level
	List<RouteSegment> findByTrafficLevel(String trafficLevel);

	// Find segments near a specific point
	@Query("{'startLocation': {$near: {$geometry: {type: 'Point', coordinates: [?0, ?1]}, $maxDistance: ?2}}}")
	List<RouteSegment> findSegmentsNearStartLocation(double longitude, double latitude, double maxDistanceMeters);

	// Delete all segments for a specific route
	void deleteByRouteId(String routeId);

	// Count segments for a specific route
	long countByRouteId(String routeId);
}