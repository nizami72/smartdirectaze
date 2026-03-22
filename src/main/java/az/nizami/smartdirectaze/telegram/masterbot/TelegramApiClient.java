package az.nizami.smartdirectaze.telegram.masterbot;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class TelegramApiClient {

    private final RestClient restClient;

    public TelegramApiClient() {
        this.restClient = RestClient.create("https://api.telegram.org");
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

}

