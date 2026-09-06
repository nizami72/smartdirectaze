package az.nizami.smartdirectaze.instagram.internal.client;

import az.nizami.smartdirectaze.html.ApiService;
import az.nizami.smartdirectaze.instagram.internal.dto.InstagramSendRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@Service
@Log4j2
public class InstagramClient {

    private final ApiService apiService;

    InstagramClient(ApiService apiService) {
        this.apiService = apiService;
    }

    @Async
    public void sendMessageToClient(String recipientId, String text) {
        var request = InstagramSendRequest.buildewr(recipientId, text);
        String payload = """
                {
                  "recipient": {
                    "id": "17841405376618492"
                  },
                  "message": {
                    "text": "Салам!"
                  }
                }
                """;

        String d = sendRequest("", payload);
        log.debug("Return answer to the client [{}]", d);
    }

    public String getSenderIdByMid(String mid) {
//        String url = "https://graph.facebook.com/v19.0/" + mid + "?fields=from&access_token=" + accessToken;

        // Используем RestTemplate или WebClient
//        var response = restTemplate.getForObject(url, Map.class);
//
//        if (response != null && response.containsKey("from")) {
//            Map<String, String> from = (Map<String, String>) response.get("from");
//            return from.get("id"); // Это и есть искомый Sender ID
//        }
        return null;
    }


    public String sendRequest(String url, String jsonBody ) {
        return apiService.sendPostRequest(url, jsonBody, Map.of("Content-Type", "application/json"));
    }







}
