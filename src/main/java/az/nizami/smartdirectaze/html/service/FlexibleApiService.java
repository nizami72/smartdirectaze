package az.nizami.smartdirectaze.html.service;

import az.nizami.smartdirectaze.html.ApiService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Component
@Log4j2
public class FlexibleApiService implements ApiService {

    private final RestClient restClient;

    public FlexibleApiService(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Универсальный POST запрос
     *
     * @param endpoint путь (например, "/data")
     * @param body     объект для отправки в JSON
     * @param headers  Map с заголовками
     * @return Ответ в виде строки (или замените на нужный класс)
     */
    public String sendPostRequest(String endpoint, Object body, Map<String, String> headers) {

        try {
            return restClient.post()
                    .uri(endpoint)
                    .headers(httpHeaders -> {
                        // Расширяемость: добавляем все заголовки из Map
                        headers.forEach(httpHeaders::add);
                    })
                    .body(body)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            log.error("Failed to send request [{}], error [{}]", endpoint, e.getMessage(), e);
            return "Error, see error logs";
        }
    }

}