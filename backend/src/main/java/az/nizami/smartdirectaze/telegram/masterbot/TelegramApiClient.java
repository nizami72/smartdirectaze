package az.nizami.smartdirectaze.telegram.masterbot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TelegramApiClient {

    private final RestClient restClient;

    public TelegramApiClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("https://api.telegram.org").build();
    }

    /**
     * URL: https://api.telegram.org/bot{token}/getMe
     */
    @SuppressWarnings("unchecked")
    public String getBotUsername(String token) {
        String url = "/bot{token}/getMe";
        try {
            Map<String, Object> response = restClient.get()
                    .uri(url, token)
                    .retrieve()
                    .body(Map.class);

            if (response != null && Boolean.TRUE.equals(response.get("ok"))) {
                Map<String, Object> result = (Map<String, Object>) response.get("result");
                return (String) result.get("username");
            }
            throw new RuntimeException("Telegram API returned error: " + response);
        } catch (Exception e) {
            log.error("Error fetching bot info from Telegram API: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch bot info from Telegram API", e);
        }
    }

    public void sendMessage(String botToken, Long chatId, String text) {
        Map<String, Object> body = Map.of(
                "chat_id", chatId,
                "text", text
        );

        restClient.post()
                .uri("/bot{token}/sendMessage", botToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    public boolean isTokenValid(String botToken) {
        try {
            restClient.get()
                    .uri("/bot{token}/getMe", botToken)
                    .retrieve()
                    .toBodilessEntity();
            return true; // Если вернулся 200 OK
        } catch (HttpClientErrorException e) {
            return false; // Если вернулся 401 Unauthorized
        }
    }

    public void setWebhook(String botToken, String webhookUrl) {
        Map<String, String> body = Map.of("url", webhookUrl);

        restClient.post()
                .uri("/bot{token}/setWebhook", botToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    public void sendWebAppButton(String masterBotToken, Long chatId, String text, String buttonText, String webAppUrl) {
        Map<String, Object> webApp = Map.of("url", webAppUrl);
        Map<String, Object> button = Map.of(
                "text", buttonText,
                "web_app", webApp
        );
        Map<String, Object> inlineKeyboard = Map.of(
                "inline_keyboard", List.of(List.of(button)) // Массив массивов кнопок
        );

        Map<String, Object> body = Map.of(
                "chat_id", chatId,
                "text", text,
                "reply_markup", inlineKeyboard
        );

        restClient.post()
                .uri("/bot{token}/sendMessage", masterBotToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    public void sendWebAppButtons(String masterBotToken, Long chatId, String text, Map<String, String> buttonConfigs) {
        // Создаем основной список строк клавиатуры
        List<List<Map<String, Object>>> keyboardRows = new ArrayList<>();

        // Проходим по мапе и создаем по одной кнопке в каждой строке
        buttonConfigs.forEach((buttonText, webAppUrl) -> {
            Map<String, Object> webApp = Map.of("url", webAppUrl);
            Map<String, Object> button = Map.of(
                    "text", buttonText,
                    "web_app", webApp
            );
            // Каждую кнопку кладем в отдельный список (ряд), чтобы они шли друг под другом
            keyboardRows.add(List.of(button));
        });

        Map<String, Object> inlineKeyboard = Map.of(
                "inline_keyboard", keyboardRows
        );

        Map<String, Object> body = Map.of(
                "chat_id", chatId,
                "text", text,
                "reply_markup", inlineKeyboard
        );

        restClient.post()
                .uri("/bot{token}/sendMessage", masterBotToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

}

