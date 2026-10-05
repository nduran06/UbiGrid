package eci.smartcity.ubigrid.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eci.smartcity.ubigrid.model.RoadSegment;
import eci.smartcity.ubigrid.repository.RoadConnectionRepository;
import eci.smartcity.ubigrid.repository.RoadSegmentRepository;
import eci.smartcity.ubigrid.service.RoadNetworkService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of RoadNetworkService
 */
@Service
@Slf4j
public class RoadNetworkServiceImpl implements RoadNetworkService {

	private final RoadSegmentRepository roadSegmentRepository;
	private final RoadConnectionRepository roadConnectionRepository;

	@Autowired
	public RoadNetworkServiceImpl(RoadSegmentRepository roadSegmentRepository,
			RoadConnectionRepository roadConnectionRepository) {
		this.roadSegmentRepository = roadSegmentRepository;
		this.roadConnectionRepository = roadConnectionRepository;
	}

	@Override
	public void updateRoadSegmentCongestion(String roadSegmentId, double congestionLevel) {
		roadSegmentRepository.updateCongestionLevel(roadSegmentId, congestionLevel);
	}

	@Override
	public void updateRoadSegmentVehicleCount(String roadSegmentId, int vehicleCount) {
		roadSegmentRepository.updateVehicleCount(roadSegmentId, vehicleCount);
	}

	@Override
	public void updateRoadSegmentAverageSpeed(String roadSegmentId, double averageSpeed) {
		roadSegmentRepository.updateAverageSpeed(roadSegmentId, averageSpeed);
	}

	@Override
	public List<RoadSegment> findRoadSegmentsInArea(double minLon, double minLat, double maxLon, double maxLat) {
		return roadSegmentRepository.findRoadSegmentsInArea(minLon, minLat, maxLon, maxLat);
	}

	@Override
	public List<RoadSegment> findRoadSegmentsNearPoint(double longitude, double latitude, double radiusMeters) {
		return roadSegmentRepository.findRoadSegmentsNearPoint(longitude, latitude, radiusMeters);
	}

	@Override
	public void updateTrafficSignalState(String roadSegmentId, String signalId, String state) {
		Optional<RoadSegment> optionalSegment = roadSegmentRepository.findById(roadSegmentId);
		if (!optionalSegment.isPresent()) {
			log.warn("Road segment not found: {}", roadSegmentId);
			return;
		}

		RoadSegment segment = optionalSegment.get();

		// Update the signal state
		segment.getTrafficSignals().put(signalId, state);

		// Save the updated segment
		roadSegmentRepository.save(segment);

		log.info("Updated traffic signal {} on road segment {} to state {}", signalId, roadSegmentId, state);
	}
}
