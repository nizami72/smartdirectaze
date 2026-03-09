package az.nizami.smartdirectaze.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {

    private final String baseUrl;
    private final String accessToken;

    public AppConfig(@Value("${instagram.url}") String baseUrl, @Value("${instagram.marker.access-token2}") String accessToken) {
        this.baseUrl = baseUrl;
        this.accessToken = accessToken;
    }

    @Bean
    public RestClient restClient(RestClient.Builder restClientBuilder) {
        return restClientBuilder
                .baseUrl(baseUrl + "?access_token=" + accessToken)
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().add("Content-Type", "application/json");
                    return execution.execute(request, body);
                })
                .build();
    }
}