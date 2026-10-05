package eci.smartcity.ubigrid.repository.traffic;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import eci.smartcity.ubigrid.model.traffic.TrafficAnalysisResult;

/**
 * Custom repository interface for complex Traffic Analysis queries
 */
@Repository
public interface TrafficAnalysisCustomRepository {
    
    List<TrafficAnalysisResult> findLatestAnalysisForAllLocations();
    
    Map<String, Double> getAverageCongestionByTimeOfDay(String locationId, LocalDateTime startDate, LocalDateTime endDate);
    
    List<TrafficAnalysisResult> findSimilarHistoricalPatterns(TrafficAnalysisResult current, int limitResults);
    
    void updatePredictionAccuracy(String analysisId, Map<String, Double> metrics);
}
