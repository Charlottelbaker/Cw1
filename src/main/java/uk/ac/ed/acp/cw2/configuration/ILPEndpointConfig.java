package uk.ac.ed.acp.cw2.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ILPEndpointConfig {

    @Bean
    public String ilpEndpoint() {
        // Read environment variable ILP_ENDPOINT, default to ILP-service URL
        String endpoint = System.getenv("ILP_ENDPOINT");
        if (endpoint == null || endpoint.isBlank()) {
            endpoint = "https://ilp-rest-2025-bvh6e9hschfagrgy.ukwest-01.azurewebsites.net/";
        }
        return endpoint;
    }
}
