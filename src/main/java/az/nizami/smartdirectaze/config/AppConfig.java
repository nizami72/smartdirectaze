package az.nizami.smartdirectaze.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Configuration
public class AppConfig {

    private final String baseUrl;
    private final String accessToken;
    private final String productAliasesFilePath;

    public AppConfig(@Value("${instagram.url}") String baseUrl,
                     @Value("${instagram.marker.access-token2}") String accessToken,
                     @Value("${app.productAliasesFilePath}")String productAliasesFilePath) {
        this.baseUrl = baseUrl;
        this.accessToken = accessToken;
        this.productAliasesFilePath = productAliasesFilePath;
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

    @Bean
    public Map<String, List<String>> init() throws IOException {
        Map<String, List<String>> productAliases = new HashMap<>();
        List<String> lines = Files.readAllLines(Paths.get(productAliasesFilePath));
        for (String line : lines) {
            if (line.contains(":") && line.contains("[")) {
                String key = line.split(":")[0].trim();
                Pattern pattern = Pattern.compile("\\[(.*?)\\]");
                Matcher matcher = pattern.matcher(line);

                if (matcher.find()) {
                    String content = matcher.group(1);
                    List<String> values = Arrays.stream(content.split(","))
                            .map(s -> s.replace("\"", "").trim())
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());

                    productAliases.put(key, values);
                }
            }
        }
        return productAliases;
    }
}