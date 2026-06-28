package cl.usm.taller1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${external.scale.api.base-url:http://localhost:8081}")
    private String externalScaleApiBaseUrl;

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder
                .baseUrl(externalScaleApiBaseUrl)
                .build();
    }
}
