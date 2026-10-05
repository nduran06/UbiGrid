package eci.smartcity.ubigrid.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eci.smartcity.ubigrid.model.GeoLocation;
import eci.smartcity.ubigrid.model.Route;
import eci.smartcity.ubigrid.model.RouteEvaluation;
import eci.smartcity.ubigrid.model.RouteSegment;
import eci.smartcity.ubigrid.model.Vehicle;
import eci.smartcity.ubigrid.model.enums.RoutePreference;
import eci.smartcity.ubigrid.repository.RouteRepository;
import eci.smartcity.ubigrid.repository.RouteSegmentRepository;
import eci.smartcity.ubigrid.repository.VehicleRepository;
import eci.smartcity.ubigrid.service.TrafficService;
import eci.smartcity.ubigrid.service.VehicleRouteService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of the VehicleRouteService interface.
 */
@Service
public class VehicleRouteServiceImpl implements VehicleRouteService {

	private static final Logger logger = LoggerFactory.getLogger(VehicleRouteServiceImpl.class);
	private static final int MAX_ALTERNATIVE_ROUTES = 3;
	private static final double ROUTE_VARIATION_FACTOR = 0.2; // 20% variation for alternative routes

	private final RouteSegmentRepository routeSegmentRepository;
	private final VehicleRepository vehicleRepository;
	private final RouteRepository routeRepository;
	private final TrafficService trafficService;
	private final Map<String, List<RouteEvaluation>> lastEvaluations = new ConcurrentHashMap<>();

	@Autowired
	public VehicleRouteServiceImpl(RouteSegmentRepository routeSegmentRepository,
			VehicleRepository vehicleRepository, RouteRepository routeRepository,
			TrafficService trafficService) {
		this.routeSegmentRepository = routeSegmentRepository;
		this.vehicleRepository = vehicleRepository;
		this.routeRepository = routeRepository;
		this.trafficService = trafficService;
	}

	@Override
	@Transactional
	public List<RouteSegment> computeOptimalRoute(String vehicleId, double destinationLongitude,
			double destinationLatitude, String destinationLocationId, RoutePreference routePreference) {

		logger.info("Computing optimal route for vehicle {} to destination [{}, {}]", vehicleId, destinationLongitude,
				destinationLatitude);

		// Find the vehicle
		Vehicle vehicle = vehicleRepository.findById(vehicleId)
				.orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + vehicleId));

		// Deactivate any existing active routes for this vehicle
		Optional<Route> existingRoute = routeRepository.findByVehicleIdAndActiveIsTrue(vehicleId);
		existingRoute.ifPresent(route -> {
			route.setActive(false);
			routeRepository.save(route);
		});

		// Get vehicle's current location
		GeoLocation vehicleLocation = vehicle.getCurrentLocation();
		if (vehicleLocation == null) {
			throw new IllegalStateException("Vehicle location is not available");
		}

		double vehicleLongitude = vehicleLocation.getLongitude();
		double vehicleLatitude = vehicleLocation.getLatitude();

		// Generate route segments
		List<RouteSegment> routeSegments = generateRouteSegments(vehicleId, vehicleLongitude, vehicleLatitude,
				destinationLongitude, destinationLatitude, routePreference);

		// Calculate total distance and duration
		double totalDistance = routeSegments.stream().mapToDouble(RouteSegment::getDistanceMeters).sum();
		int totalDuration = routeSegments.stream().mapToInt(RouteSegment::getDurationSeconds).sum();

		// Create a new active route
		Route route = new Route();
		route.setVehicleId(vehicleId);
		route.setActive(true);
		route.setStartLocation(new double[] { vehicleLongitude, vehicleLatitude });
		route.setDestinationLocation(new double[] { destinationLongitude, destinationLatitude });
		route.setDestinationLocationId(destinationLocationId);
		route.setRoutePreference(routePreference);
		route.setTotalDistanceMeters(totalDistance);
		route.setTotalDurationSeconds(totalDuration);
		route.setEstimatedArrivalTime(LocalDateTime.now().plusSeconds(totalDuration));

		// Create a unique ID for the route
		String routeId = UUID.randomUUID().toString();

		// Save all route segments with the route ID
		for (RouteSegment segment : routeSegments) {
			segment.setRouteId(routeId);
			routeSegmentRepository.save(segment);
		}

		// Set the route ID for the active route
		route.setRouteIds(Collections.singletonList(routeId));
		routeRepository.save(route);

		logger.info("Created new route with {} segments, total distance: {}m, duration: {}s", routeSegments.size(),
				totalDistance, totalDuration);

		return routeSegments;
	}

	@Override
	@Transactional
	public List<List<RouteSegment>> findAlternativeRoutes(String vehicleId, double destLon, double destLat,
			String destId, RoutePreference preference) {

		logger.info("Finding alternative routes for vehicle {} to destination [{}, {}]", vehicleId, destLon, destLat);

		// Find the vehicle
		Vehicle vehicle = vehicleRepository.findById(vehicleId)
				.orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + vehicleId));

		// Get vehicle's current location
		GeoLocation vehicleLocation = vehicle.getCurrentLocation();
		if (vehicleLocation == null) {
			throw new IllegalStateException("Vehicle location is not available");
		}

		double vehicleLon = vehicleLocation.getLongitude();
		double vehicleLat = vehicleLocation.getLatitude();

		// First, compute the optimal route
		List<RouteSegment> optimalRoute = computeOptimalRoute(vehicleId, destLon, destLat, destId, preference);

		// Generate alternative routes
		List<List<RouteSegment>> alternativeRoutes = new ArrayList<>();
		alternativeRoutes.add(optimalRoute); // Add the optimal route as the first option

		// Generate additional alternative routes with variations
		for (int i = 1; i < MAX_ALTERNATIVE_ROUTES; i++) {
			// Create a slightly different route preference for variety
			RoutePreference altPreference = getAlternativePreference(preference, i);

			// Generate an alternative route with the modified preference
			List<RouteSegment> alternativeRoute = generateRouteSegments(vehicleId, vehicleLon, vehicleLat, destLon,
					destLat, altPreference);

			// Create a unique ID for the route
			String routeId = UUID.randomUUID().toString();

			// Save all route segments with the route ID
			for (RouteSegment segment : alternativeRoute) {
				segment.setRouteId(routeId);
				routeSegmentRepository.save(segment);
			}

			// Add the alternative route
			alternativeRoutes.add(alternativeRoute);

			// Update the active route to include this alternative route ID
			Optional<Route> routeOpt = routeRepository.findByVehicleIdAndActiveIsTrue(vehicleId);
			if (routeOpt.isPresent()) {
				Route route = routeOpt.get();
				List<String> routeIds = route.getRouteIds();
				if (routeIds == null) {
					routeIds = new ArrayList<>();
				}
				routeIds.add(routeId);
				route.setRouteIds(routeIds);
				routeRepository.save(route);
			}
		}

		logger.info("Generated {} alternative routes for vehicle {}", alternativeRoutes.size(), vehicleId);
		return alternativeRoutes;
	}

	@Override
	@Scheduled(fixedRate = 300000) // Run every 5 minutes
	@Transactional
	public void recomputeAllRoutes() {
		logger.info("Recomputing all active routes to adapt to current traffic conditions");

		// Get all active routes
		List<Route> routes = routeRepository.findByActiveIsTrue();

		for (Route route : routes) {
			try {
				String vehicleId = route.getVehicleId();
				double[] destLocation = route.getDestinationLocation();
				String destLocationId = route.getDestinationLocationId();
				RoutePreference preference = RoutePreference.valueOf(route.getRoutePreference());

				// Recompute the route using the current traffic conditions
				computeOptimalRoute(vehicleId, destLocation[0], destLocation[1], destLocationId, preference);

				logger.debug("Successfully recomputed route for vehicle {}", vehicleId);
			} catch (Exception e) {
				logger.error("Error recomputing route for vehicle {}: {}", route.getVehicleId(), e.getMessage(), e);
			}
		}

		logger.info("Completed recomputing {} active routes", routes.size());
	}

	@Override
	public List<RouteEvaluation> getLastEvaluations(String vehicleId) {
		return lastEvaluations.getOrDefault(vehicleId, List.of());
	}

	// Lateral bow of each candidate path, as a fraction of the direct distance.
	private static final double[] BOW_FRACTIONS = { 0.0, 0.2, -0.2, 0.35, -0.35 };
	private static final int TOLL_PENALTY_SECONDS = 600;
	private static final int HIGHWAY_PENALTY_SECONDS = 300;

	private record Candidate(String label, List<RouteSegment> segments, double distance, int duration, double cost) {
	}

	// Prices several candidate paths against the live traffic readings and
	// returns the cheapest one, recording every evaluation for the caller.
	private List<RouteSegment> generateRouteSegments(String vehicleId, double startLon, double startLat, double endLon,
			double endLat, RoutePreference preference) {

		double directDistance = calculateDistance(startLat, startLon, endLat, endLon);

		// Determine number of segments based on distance (1 segment per km, minimum 3)
		int numSegments = Math.max(3, (int) (directDistance / 1000));

		List<Candidate> candidates = new ArrayList<>();
		for (double bow : BOW_FRACTIONS) {
			List<double[]> waypoints = generateWaypoints(startLon, startLat, endLon, endLat, numSegments, bow);
			List<RouteSegment> segments = buildSegments(waypoints, numSegments);
			candidates.add(scoreCandidate(describeBow(bow, startLon, startLat, endLon, endLat), segments, preference));
		}

		Candidate best = candidates.stream().min(Comparator.comparingDouble(Candidate::cost)).get();

		List<RouteEvaluation> evaluations = new ArrayList<>();
		for (Candidate candidate : candidates) {
			evaluations.add(new RouteEvaluation(candidate.label(), candidate.distance(), candidate.duration(),
					worstTrafficLevel(candidate.segments()), candidate == best));
		}
		lastEvaluations.put(vehicleId, evaluations);

		logger.info("Evaluated {} candidate routes against live traffic; chose '{}' ({}s)", candidates.size(),
				best.label(), best.duration());
		return best.segments();
	}

	private Candidate scoreCandidate(String label, List<RouteSegment> segments, RoutePreference preference) {
		double distance = segments.stream().mapToDouble(RouteSegment::getDistanceMeters).sum();
		int duration = segments.stream().mapToInt(RouteSegment::getDurationSeconds).sum();

		double cost = preference == RoutePreference.SHORTEST ? distance : duration;
		if (preference == RoutePreference.AVOID_TOLLS) {
			cost += TOLL_PENALTY_SECONDS * segments.stream().filter(RouteSegment::isTollRequired).count();
		}
		if (preference == RoutePreference.AVOID_HIGHWAYS || preference == RoutePreference.SCENIC) {
			cost += HIGHWAY_PENALTY_SECONDS * segments.stream().filter(sg -> "HIGHWAY".equals(sg.getRoadType())).count();
		}
		return new Candidate(label, segments, distance, duration, cost);
	}

	private String worstTrafficLevel(List<RouteSegment> segments) {
		if (segments.stream().anyMatch(sg -> "HEAVY".equals(sg.getTrafficLevel()))) {
			return "HEAVY";
		}
		if (segments.stream().anyMatch(sg -> "MODERATE".equals(sg.getTrafficLevel()))) {
			return "MODERATE";
		}
		return "LIGHT";
	}

	// Creates route segments for each pair of waypoints, with duration derived
	// from the average speed the traffic readings report around each segment.
	private List<RouteSegment> buildSegments(List<double[]> waypoints, int numSegments) {
		List<RouteSegment> segments = new ArrayList<>();

		for (int i = 0; i < waypoints.size() - 1; i++) {
			double[] startPoint = waypoints.get(i);
			double[] endPoint = waypoints.get(i + 1);

			RouteSegment segment = new RouteSegment();
			segment.setSegmentIndex(i);
			segment.setStartLocation(startPoint);
			segment.setEndLocation(endPoint);

			double segmentDistance = calculateDistance(startPoint[1], startPoint[0], // lat, lon for start
					endPoint[1], endPoint[0]); // lat, lon for end
			segment.setDistanceMeters(segmentDistance);

			double midLat = (startPoint[1] + endPoint[1]) / 2;
			double midLon = (startPoint[0] + endPoint[0]) / 2;

			segment.setTrafficLevel(trafficService.getTrafficCondition(startPoint[1], startPoint[0], endPoint[1],
					endPoint[0]));

			double speedMetersPerSecond = trafficService.getAverageSpeedKmh(midLat, midLon) / 3.6;
			segment.setDurationSeconds((int) (segmentDistance / speedMetersPerSecond));

			segment.setTollRequired(trafficService.hasToll(startPoint[1], startPoint[0], endPoint[1], endPoint[0]));
			segment.setRoadType(trafficService.getRoadType(midLat, midLon));
			segment.setInstructions(generateInstructions(i, numSegments, segment.getRoadType()));
			segment.setManeuverType(determineManeuverType(i > 0 ? waypoints.get(i - 1) : null, startPoint, endPoint));

			segments.add(segment);
		}

		return segments;
	}

	// Waypoints along the straight line from start to end, bowed sideways by
	// bowFraction * direct distance at the midpoint (0 = straight line).
	private List<double[]> generateWaypoints(double startLon, double startLat, double endLon, double endLat,
			int numSegments, double bowFraction) {

		List<double[]> waypoints = new ArrayList<>();
		waypoints.add(new double[] { startLon, startLat });

		double dLat = endLat - startLat;
		double dLon = endLon - startLon;
		// Perpendicular (left of travel direction) in degree space, scaled to the segment length
		double perpLat = dLon;
		double perpLon = -dLat;

		for (int i = 1; i < numSegments; i++) {
			double ratio = (double) i / numSegments;
			double bow = bowFraction * Math.sin(Math.PI * ratio);
			waypoints.add(new double[] { startLon + dLon * ratio + perpLon * bow, startLat + dLat * ratio + perpLat * bow });
		}

		waypoints.add(new double[] { endLon, endLat });
		return waypoints;
	}

	// Human-readable name for a candidate, by where its midpoint bows to.
	private String describeBow(double bowFraction, double startLon, double startLat, double endLon, double endLat) {
		if (bowFraction == 0.0) {
			return "Ruta directa";
		}
		double perpLat = endLon - startLon;
		double perpLon = -(endLat - startLat);
		double sign = Math.signum(bowFraction);
		double offsetLat = perpLat * sign;
		double offsetLon = perpLon * sign;
		String side;
		if (Math.abs(offsetLat) >= Math.abs(offsetLon)) {
			side = offsetLat >= 0 ? "norte" : "sur";
		} else {
			side = offsetLon >= 0 ? "este" : "oeste";
		}
		return (Math.abs(bowFraction) > 0.3 ? "Desvío amplio por el " : "Desvío por el ") + side;
	}

	// Get an alternative preference for generating route variations
	private RoutePreference getAlternativePreference(RoutePreference primary, int variationIndex) {
		// For first variation, use a different but related preference
		if (variationIndex == 1) {
			switch (primary) {
			case FASTEST:
				return RoutePreference.EFFICIENT;
			case SHORTEST:
				return RoutePreference.FASTEST;
			case EFFICIENT:
				return RoutePreference.ECO_FRIENDLY;
			case ECO_FRIENDLY:
				return RoutePreference.EFFICIENT;
			case AVOID_TOLLS:
				return RoutePreference.SHORTEST;
			case AVOID_HIGHWAYS:
				return RoutePreference.ECO_FRIENDLY;
			case SCENIC:
				return RoutePreference.AVOID_HIGHWAYS;
			default:
				return RoutePreference.SHORTEST;
			}
		}
		// For second variation, use another different preference
		else {
			switch (primary) {
			case FASTEST:
				return RoutePreference.SHORTEST;
			case SHORTEST:
				return RoutePreference.ECO_FRIENDLY;
			case EFFICIENT:
				return RoutePreference.FASTEST;
			case ECO_FRIENDLY:
				return RoutePreference.AVOID_HIGHWAYS;
			case AVOID_TOLLS:
				return RoutePreference.EFFICIENT;
			case AVOID_HIGHWAYS:
				return RoutePreference.SHORTEST;
			case SCENIC:
				return RoutePreference.ECO_FRIENDLY;
			default:
				return RoutePreference.EFFICIENT;
			}
		}
	}

	// Generate navigation instructions
	private String generateInstructions(int segmentIndex, int totalSegments, String roadType) {
		if (segmentIndex == 0) {
			return "Start your journey and proceed on " + roadType.toLowerCase();
		} else if (segmentIndex == totalSegments - 1) {
			return "Continue toward your destination";
		} else {
			String[] instructions = { "Continue straight ahead", "Keep going on " + roadType.toLowerCase(),
					"Follow the road", "Proceed on current path", "Stay on " + roadType.toLowerCase() };

			return instructions[segmentIndex % instructions.length];
		}
	}

	// Determine the type of maneuver
	private String determineManeuverType(double[] prevPoint, double[] currPoint, double[] nextPoint) {
		if (prevPoint == null) {
			return "DEPART";
		}

		if (nextPoint == null) {
			return "ARRIVE";
		}

		// Calculate the bearing change to determine turn direction
		double bearing1 = calculateBearing(prevPoint[1], prevPoint[0], // lat1, lon1
				currPoint[1], currPoint[0]); // lat2, lon2

		double bearing2 = calculateBearing(currPoint[1], currPoint[0], // lat1, lon1
				nextPoint[1], nextPoint[0]); // lat2, lon2

		double bearingChange = bearing2 - bearing1;

		// Normalize to -180 to 180
		if (bearingChange > 180) {
			bearingChange -= 360;
		} else if (bearingChange < -180) {
			bearingChange += 360;
		}

		// Determine turn type based on bearing change
		if (Math.abs(bearingChange) < 10) {
			return "STRAIGHT";
		} else if (bearingChange >= 10 && bearingChange < 45) {
			return "SLIGHT_RIGHT";
		} else if (bearingChange >= 45 && bearingChange < 135) {
			return "RIGHT_TURN";
		} else if (bearingChange >= 135) {
			return "UTURN";
		} else if (bearingChange <= -10 && bearingChange > -45) {
			return "SLIGHT_LEFT";
		} else if (bearingChange <= -45 && bearingChange > -135) {
			return "LEFT_TURN";
		} else if (bearingChange <= -135) {
			return "UTURN";
		} else {
			return "STRAIGHT";
		}
	}

	// Calculate bearing between two points
	private double calculateBearing(double lat1, double lon1, double lat2, double lon2) {
		lat1 = Math.toRadians(lat1);
		lon1 = Math.toRadians(lon1);
		lat2 = Math.toRadians(lat2);
		lon2 = Math.toRadians(lon2);

		double dLon = lon2 - lon1;

		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

		double bearing = Math.atan2(y, x);

		bearing = Math.toDegrees(bearing);
		bearing = (bearing + 360) % 360;

		return bearing;
	}

	// Calculate distance between two points using Haversine formula
	private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
		final int R = 6371000; // Earth radius in meters

		double latDistance = Math.toRadians(lat2 - lat1);
		double lonDistance = Math.toRadians(lon2 - lon1);

		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return R * c; // Distance in meters
	}
}