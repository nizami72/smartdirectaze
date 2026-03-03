package az.nizami.smartdirectaze.web;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class InstagramService {

    private final RestTemplate restTemplate = new RestTemplate();
    
    // Токен страницы из Facebook Developer Console
    private final String PAGE_ACCESS_TOKEN = "YOUR_PAGE_ACCESS_TOKEN"; 

    public void sendStubResponse(String incomingJson) {
        // В реальном коде мы распарсим json, чтобы найти ID отправителя
        // Здесь мы просто имитируем логику
        String recipientId = "RECIPIENT_ID_FROM_JSON"; 
        String url = "https://graph.facebook.com/v19.0/me/messages?access_token=" + PAGE_ACCESS_TOKEN;

        Map<String, Object> body = new HashMap<>();
        body.put("recipient", Map.of("id", recipientId));
        body.put("message", Map.of("text", "Салам! Это тестовый ответ от вашего ИИ-помощника. Пока я в режиме настройки."));

        try {
            restTemplate.postForEntity(url, body, String.class);
            System.out.println("Stub response sent successfully.");
        } catch (Exception e) {
            System.err.println("Error sending stub: " + e.getMessage());
        }
    }
}