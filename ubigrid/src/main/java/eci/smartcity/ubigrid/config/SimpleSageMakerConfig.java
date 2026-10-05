package eci.smartcity.ubigrid.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sagemakerruntime.AmazonSageMakerRuntime;
import com.amazonaws.services.sagemakerruntime.AmazonSageMakerRuntimeClientBuilder;

/**
 * Minimal AWS SageMaker client configuration that avoids Jackson dependency issues.
 */
@Configuration
public class SimpleSageMakerConfig {

    @Value("${aws.region:us-east-1}")
    private String region;

    /**
     * Creates a bare minimum SageMaker Runtime client with default credential chain.
     * This approach minimizes dependencies and potential conflicts.
     */
    
}
