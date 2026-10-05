package eci.smartcity.ubigrid.service;

import com.amazonaws.services.sagemakerruntime.AmazonSageMakerRuntime;
import com.amazonaws.services.sagemakerruntime.model.InvokeEndpointRequest;
import com.amazonaws.services.sagemakerruntime.model.InvokeEndpointResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Service for interacting with Amazon SageMaker machine learning models.
 */
@Service
public class SageMakerService {
    
    private static final Logger logger = LoggerFactory.getLogger(SageMakerService.class);
    
    private final AmazonSageMakerRuntime sageMakerRuntime;
    
    @Value("${api.sagemaker.endpoint}")
    private String endpointName;
    
    @Value("${sagemaker.content-type:application/json}")
    private String contentType;
    
    @Value("${sagemaker.accept:application/json}")
    private String accept;
    
    @Autowired
    public SageMakerService(AmazonSageMakerRuntime sageMakerRuntime) {
        this.sageMakerRuntime = sageMakerRuntime;
    }
    
    /**
     * Invokes a SageMaker endpoint with the given payload.
     * 
     * @param payload The input data to send to the model
     * @return The model's prediction result as a string
     */
    public String invokeEndpoint(String payload) {
        try {
            // Convert the payload string to ByteBuffer
            ByteBuffer payloadBuffer = ByteBuffer.wrap(payload.getBytes(StandardCharsets.UTF_8));
            
            // Create the request
            InvokeEndpointRequest request = new InvokeEndpointRequest()
                    .withEndpointName(endpointName)
                    .withContentType(contentType)
                    .withAccept(accept)
                    .withBody(payloadBuffer);
            
            // Invoke the endpoint
            logger.info("Invoking SageMaker endpoint: {}", endpointName);
            InvokeEndpointResult result = sageMakerRuntime.invokeEndpoint(request);
            
            // Convert the result to a string
            ByteBuffer responseBuffer = result.getBody();
            byte[] responseBytes = new byte[responseBuffer.remaining()];
            responseBuffer.get(responseBytes);
            String response = new String(responseBytes, StandardCharsets.UTF_8);
            
            logger.debug("SageMaker response received with content type: {}", result.getContentType());
            return response;
            
        } catch (Exception e) {
            logger.error("Error invoking SageMaker endpoint: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to invoke SageMaker endpoint", e);
        }
    }
    
    /**
     * Invokes a SageMaker endpoint for traffic prediction.
     * 
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate
     * @param timeOfDay The time of day (e.g., "morning", "evening")
     * @param dayOfWeek The day of the week (1-7)
     * @return The predicted traffic level
     */
    public String predictTraffic(double latitude, double longitude, String timeOfDay, int dayOfWeek) {
        // Create a JSON payload for the model
        String payload = String.format(
                "{\"latitude\": %f, \"longitude\": %f, \"time_of_day\": \"%s\", \"day_of_week\": %d}",
                latitude, longitude, timeOfDay, dayOfWeek);
        
        // Invoke the endpoint and return the result
        return invokeEndpoint(payload);
    }
}