package eci.smartcity.ubigrid.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sagemakerruntime.AmazonSageMakerRuntime;
import com.amazonaws.services.sagemakerruntime.AmazonSageMakerRuntimeClient;
import com.amazonaws.services.sagemakerruntime.AmazonSageMakerRuntimeClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for AWS SageMaker Runtime client.
 * This version provides alternative approaches to creating the client to handle dependency issues.
 */
@Configuration
public class AwsSageMakerConfig {
    
    @Value("${aws.region:us-east-1}")
    private String awsRegion;
    
    @Value("${aws.accessKey:#{null}}")
    private String awsAccessKey;
    
    @Value("${aws.secretKey:#{null}}")
    private String awsSecretKey;
    
    @Value("${aws.endpoint.url:#{null}}")
    private String endpointUrl;
    
    /**
     * Creates an AmazonSageMakerRuntime bean using the direct client constructor
     * rather than the builder to avoid certain dependency issues.
     * 
     * @return An AmazonSageMakerRuntime client
     */
    @Bean
    public AmazonSageMakerRuntime amazonSageMakerRuntime() {
        try {
            AWSCredentialsProvider credentialsProvider;
            
            if (awsAccessKey != null && awsSecretKey != null) {
                BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
                credentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
            } else {
                credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
            }
            
            ClientConfiguration clientConfig = new ClientConfiguration();
            // Add an explicit timeout to avoid stalling
            clientConfig.setConnectionTimeout(5000);
            clientConfig.setSocketTimeout(5000);
            
            // If endpoint URL is provided, use it (for testing/debugging)
            if (endpointUrl != null && !endpointUrl.isEmpty()) {
                EndpointConfiguration endpointConfig = new EndpointConfiguration(endpointUrl, awsRegion);
                return AmazonSageMakerRuntimeClient.builder()
                        .withEndpointConfiguration(endpointConfig)
                        .withCredentials(credentialsProvider)
                        .withClientConfiguration(clientConfig)
                        .build();
            } else {
                // Standard approach using region
                return AmazonSageMakerRuntimeClient.builder()
                        .withRegion(Regions.fromName(awsRegion))
                        .withCredentials(credentialsProvider)
                        .withClientConfiguration(clientConfig)
                        .build();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create AmazonSageMakerRuntime bean: " + e.getMessage(), e);
        }
    }
}