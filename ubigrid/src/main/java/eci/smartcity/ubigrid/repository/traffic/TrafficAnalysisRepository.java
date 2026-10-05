package eci.smartcity.ubigrid.repository.traffic;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import eci.smartcity.ubigrid.model.traffic.TrafficAnalysisResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrafficAnalysisRepository extends MongoRepository<TrafficAnalysisResult, String>, TrafficAnalysisCustomRepository {
    
    Optional<TrafficAnalysisResult> findTopByLocationIdOrderByAnalysisTimestampDesc(String locationId);
    
    List<TrafficAnalysisResult> findByLocationIdAndAnalysisTimestampBetween(
            String locationId, LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("{'roadSegmentId': ?0}")
    List<TrafficAnalysisResult> findByRoadSegmentId(String roadSegmentId);
    
    @Query("{'isAnomaly': true, 'analysisTimestamp': {$gt: ?0}}")
    List<TrafficAnalysisResult> findRecentAnomalies(LocalDateTime cutoffTime);
    
    @Query("{'anomalyType': ?0, 'analysisTimestamp': {$gt: ?1}}")
    List<TrafficAnalysisResult> findRecentAnomaliesByType(String anomalyType, LocalDateTime cutoffTime);
    
    @Query("{'predictionTimestamp': {$gt: ?0, $lt: ?1}}")
    List<TrafficAnalysisResult> findPredictionsForTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("{'currentCongestionLevel': {$gte: ?0}, 'analysisTimestamp': {$gt: ?1}}")
    List<TrafficAnalysisResult> findHighCongestionAreas(double threshold, LocalDateTime cutoffTime);
    
    @Query(value = "{'analysisTimestamp': {$gt: ?0}}", sort = "{'currentCongestionLevel': -1}")
    List<TrafficAnalysisResult> findTopCongestedAreas(LocalDateTime cutoffTime, Pageable pageable);
}
