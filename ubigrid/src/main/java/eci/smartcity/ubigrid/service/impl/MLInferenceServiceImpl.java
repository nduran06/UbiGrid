package eci.smartcity.ubigrid.service.impl;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sagemakerruntime.AmazonSageMakerRuntime;
import com.amazonaws.services.sagemakerruntime.model.InvokeEndpointRequest;
import com.amazonaws.services.sagemakerruntime.model.InvokeEndpointResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;

import eci.smartcity.ubigrid.model.traffic.TrafficData;
import eci.smartcity.ubigrid.service.MLInferenceService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the MLInferenceService
 */
@Service
@Slf4j
public class MLInferenceServiceImpl implements MLInferenceService {

    private final AmazonSageMakerRuntime sageMakerRuntime;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public MLInferenceServiceImpl(
            AmazonSageMakerRuntime sageMakerRuntime,
            ObjectMapper objectMapper) {
        this.sageMakerRuntime = sageMakerRuntime;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public Map<String, Object> predictTrafficConditions(TrafficData currentData, List<TrafficData> recentData) {
        try {
            // Prepare input data
            Map<String, Object> inputData = prepareModelInput(currentData, recentData);
            String requestBody = objectMapper.writeValueAsString(inputData);
            
            // Call SageMaker endpoint
            InvokeEndpointRequest request = new InvokeEndpointRequest()
                    .withEndpointName("traffic-prediction-endpoint")
                    .withContentType("application/json")
                    .withBody(ByteBuffer.wrap(requestBody.getBytes()));
            
            InvokeEndpointResult result = sageMakerRuntime.invokeEndpoint(request);
            
            // Parse response
            String responseBody = new String(result.getBody().array());
            return objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            
        } catch (Exception e) {
            log.error("Error calling ML endpoint: {}", e.getMessage(), e);
            return getFallbackPrediction(currentData, recentData);
        }
    }
    
    private Map<String, Object> prepareModelInput(TrafficData currentData, List<TrafficData> recentData) {
        Map<String, Object> input = new HashMap<>();
        
        // Add current data
        input.put("locationId", currentData.getLocationId());
        input.put("timestamp", currentData.getTimestamp().toString());
        input.put("congestionLevel", currentData.getCongestionLevel());
        input.put("vehicleCount", currentData.getVehicleCount());
        input.put("averageSpeed", currentData.getAverageSpeed());
        
        // Add historical context
        List<Map<String, Object>> history = recentData.stream()
                .map(data -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("timestamp", data.getTimestamp().toString());
                    item.put("congestionLevel", data.getCongestionLevel());
                    item.put("vehicleCount", data.getVehicleCount());
                    item.put("averageSpeed", data.getAverageSpeed());
                    return item;
                })
                .collect(Collectors.toList());
        input.put("historicalData", history);
        
        return input;
    }
    
    @Override
    public Map<String, Object> getFallbackPrediction(TrafficData currentData, List<TrafficData> recentData) {
        log.info("Using fallback prediction for location: {}", currentData.getLocationId());
        
        Map<String, Object> result = new HashMap<>();
        
        // Simple linear extrapolation based on recent trends
        if (!recentData.isEmpty() && recentData.size() > 1) {
            // Calculate average rate of change
            double congestionChangeRate = calculateAverageChangeRate(recentData, TrafficData::getCongestionLevel);
            double vehicleCountChangeRate = calculateAverageChangeRate(recentData, data -> (double) data.getVehicleCount());
            double speedChangeRate = calculateAverageChangeRate(recentData, TrafficData::getAverageSpeed);
            
            // Predict for the next 30 minutes
            result.put("predictedCongestionLevel", 
                    Math.min(1.0, Math.max(0.0, currentData.getCongestionLevel() + congestionChangeRate * 2)));
            result.put("predictedVehicleCount", 
                    Math.max(0, (int) (currentData.getVehicleCount() + vehicleCountChangeRate * 2)));
            result.put("predictedAverageSpeed", 
                    Math.max(0.0, currentData.getAverageSpeed() + speedChangeRate * 2));
        } else {
            // If no historical data, just use current values
            result.put("predictedCongestionLevel", currentData.getCongestionLevel());
            result.put("predictedVehicleCount", currentData.getVehicleCount());
            result.put("predictedAverageSpeed", currentData.getAverageSpeed());
        }
        
        // Conservative anomaly detection
        result.put("isAnomaly", false);
        result.put("anomalyType", "NONE");
        result.put("anomalyScore", 0.0);
        
        // Low confidence scores
        Map<String, Double> confidenceScores = new HashMap<>();
        confidenceScores.put("congestionPrediction", 0.4);
        confidenceScores.put("vehicleCountPrediction", 0.4);
        confidenceScores.put("speedPrediction", 0.4);
        result.put("confidenceScores", confidenceScores);
        
        return result;
    }
    
    private double calculateAverageChangeRate(List<TrafficData> data, Function<TrafficData, Double> valueExtractor) {
        if (data.size() < 2) {
            return 0.0;
        }
        
        double totalChange = 0.0;
        for (int i = 1; i < data.size(); i++) {
            double current = valueExtractor.apply(data.get(i));
            double previous = valueExtractor.apply(data.get(i-1));
            totalChange += (current - previous);
        }
        
        return totalChange / (data.size() - 1);
    }
}