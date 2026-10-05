package eci.smartcity.ubigrid.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import eci.smartcity.ubigrid.model.traffic.TrafficAnalysisResult;
import eci.smartcity.ubigrid.model.traffic.TrafficData;
import eci.smartcity.ubigrid.repository.traffic.TrafficAnalysisRepository;
import eci.smartcity.ubigrid.repository.traffic.TrafficDataRepository;
import eci.smartcity.ubigrid.service.MLInferenceService;
import eci.smartcity.ubigrid.service.TrafficAnalysisService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the TrafficAnalysisService
 */
@Service
@Slf4j
public class TrafficAnalysisServiceImpl implements TrafficAnalysisService {

    private final TrafficDataRepository trafficDataRepository;
    private final TrafficAnalysisRepository trafficAnalysisRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MLInferenceService mlInferenceService;
    
    @Autowired
    public TrafficAnalysisServiceImpl(
            TrafficDataRepository trafficDataRepository,
            TrafficAnalysisRepository trafficAnalysisRepository,
            RedisTemplate<String, Object> redisTemplate,
            MLInferenceService mlInferenceService) {
        this.trafficDataRepository = trafficDataRepository;
        this.trafficAnalysisRepository = trafficAnalysisRepository;
        this.redisTemplate = redisTemplate;
        this.mlInferenceService = mlInferenceService;
    }
    
    @Override
    public void processTrafficData(TrafficData trafficData) {
        // Save raw traffic data
        trafficDataRepository.save(trafficData);
        
        // Analyze the data asynchronously
        analyzeTrafficData(trafficData);
    }
    
    @Override
    public void analyzeTrafficData(TrafficData trafficData) {
        log.info("Analyzing traffic data for location: {}", trafficData.getLocationId());
        
        // Retrieve historical data for context
        List<TrafficData> recentData = trafficDataRepository.findByLocationIdAndTimestampGreaterThan(
                trafficData.getLocationId(), 
                trafficData.getTimestamp().minusHours(1));
        
        // Prepare the analysis result
        TrafficAnalysisResult result = TrafficAnalysisResult.builder()
                .analysisId(UUID.randomUUID().toString())
                .locationId(trafficData.getLocationId())
                .analysisTimestamp(LocalDateTime.now())
                .currentCongestionLevel(trafficData.getCongestionLevel())
                .currentVehicleCount(trafficData.getVehicleCount())
                .currentAverageSpeed(trafficData.getAverageSpeed())
                .build();
        
        // Enhance with ML predictions
        try {
            Map<String, Object> mlResults = mlInferenceService.predictTrafficConditions(trafficData, recentData);
            
            // Update result with ML predictions
            result.setPredictedCongestionLevel((Double) mlResults.get("predictedCongestionLevel"));
            result.setPredictedVehicleCount((Integer) mlResults.get("predictedVehicleCount"));
            result.setPredictedAverageSpeed((Double) mlResults.get("predictedAverageSpeed"));
            result.setIsAnomaly((Boolean) mlResults.get("isAnomaly"));
            result.setAnomalyType((String) mlResults.get("anomalyType"));
            result.setAnomalyScore((Double) mlResults.get("anomalyScore"));
            
            // Add model confidence scores
            @SuppressWarnings("unchecked")
            Map<String, Double> confidenceScores = (Map<String, Double>) mlResults.get("confidenceScores");
            result.setModelConfidenceScores(confidenceScores);
        } catch (Exception e) {
            log.error("Error during ML prediction: {}", e.getMessage(), e);
        }
        
        // Save and cache the result
        trafficAnalysisRepository.save(result);
        cacheAnalysisResult(result);
        
        log.info("Traffic analysis completed for location: {}", trafficData.getLocationId());
    }
    
    private void cacheAnalysisResult(TrafficAnalysisResult result) {
        String key = "traffic:analysis:" + result.getLocationId();
        redisTemplate.opsForValue().set(key, result, Duration.ofMinutes(30));
    }
    
    @Override
    public Optional<TrafficAnalysisResult> getLatestAnalysisForLocation(String locationId) {
        // Try to get from cache first
        String key = "traffic:analysis:" + locationId;
        TrafficAnalysisResult cachedResult = (TrafficAnalysisResult) redisTemplate.opsForValue().get(key);
        
        if (cachedResult != null) {
            return Optional.of(cachedResult);
        }
        
        // If not in cache, get from repository
        Optional<TrafficAnalysisResult> result = trafficAnalysisRepository.findTopByLocationIdOrderByAnalysisTimestampDesc(locationId);
        
        // Cache the result if found
        result.ifPresent(this::cacheAnalysisResult);
        
        return result;
    }
    
    @Override
    public Map<String, Double> getCurrentCongestionMap() {
        // Get all the latest analysis results
        List<TrafficAnalysisResult> latestResults = trafficAnalysisRepository.findLatestAnalysisForAllLocations();
        
        // Convert to map of location ID to congestion level
        return latestResults.stream()
                .filter(r -> r.getCurrentCongestionLevel() != null)
                .collect(Collectors.toMap(
                    TrafficAnalysisResult::getLocationId,
                    TrafficAnalysisResult::getCurrentCongestionLevel
                ));
    }
    
    @Override
    public List<TrafficAnalysisResult> getHistoricalAnalysis(String locationId, LocalDateTime startTime, LocalDateTime endTime) {
        return trafficAnalysisRepository.findByLocationIdAndAnalysisTimestampBetween(locationId, startTime, endTime);
    }
}
