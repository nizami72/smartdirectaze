package az.nizami.smartdirectaze.whatsapp.client;

import az.nizami.smartdirectaze.whatsapp.GreenApiResponseDto;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Slf4j
@Component
public class GreenApiClient {

    private final RestClient restClient;
    private final String urlQr;
    private final String urlState;
    private final String urlSettings;
    private final String urlLogout;
    private final String urlSendMessage;
    private final String urlSetSettings;

    public GreenApiClient(RestClient.Builder restClientBuilder,
                          @Value("${app.url.greenApi.url.qrCode}") String urlQr,
                          @Value("${app.url.greenApi.url.state}") String urlState,
                          @Value("${app.url.greenApi.url.settings}") String urlSettings,
                          @Value("${app.url.greenApi.url.logout}") String urlLogout,
                          @Value("${app.url.greenApi.url.sendMessage}") String urlSendMessage,
                          @Value("${app.url.greenApi.url.setSettings}") String urlSetSettings) {
        this.restClient = restClientBuilder.build();
        this.urlQr = urlQr;
        this.urlState = urlState;
        this.urlSettings = urlSettings;
        this.urlLogout = urlLogout;
        this.urlSendMessage = urlSendMessage;
        this.urlSetSettings = urlSetSettings;
    }

    /**
     * URL: https://api.green-api.com/waInstance{instanceId}/qrCode/{token}
     */
    public GreenApiResponseDto getQrCode1(String instanceId, String token) {
        try {
            QrResponse response = restClient.get()
                    .uri(urlQr, instanceId, token)
                    .retrieve()
                    .body(QrResponse.class);
            if(response!=null) {
                return GreenApiResponseDto.builder()
                        .type(response.type)
                        .message(response.message)
                        .build();
            }
        } catch (Exception e) {
            throw failure("qr", instanceId, e);
        }
        return null;
    }

    public String getSettings(String instanceId, String token) {
        try {
            SettingsResponse response = restClient.get()
                    .uri(urlSettings, instanceId, token)
                    .retrieve()
                    .body(SettingsResponse.class);
            return response != null ? response.getWid() : null;
        } catch (Exception e) {
            throw failure("getSettings", instanceId, e);
        }
    }

    public void logout(String instanceId, String token) {
        try {
            // Green API: GET .../waInstance{id}/logout/{token}
            restClient.get()
                    .uri(urlLogout, instanceId, token)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            // Not thrown: the caller goes on (shows a new QR, unbinds the instance) even if the phone was already logged out
            failure("logout", instanceId, e);
        }
    }

    /**
     * URL: https://api.green-api.com/waInstance{instanceId}/getStateInstance/{token}
     */
    public String getStateInstance(String instanceId, String token) {
        try {
            StateResponse response = restClient.get()
                    .uri(urlState, instanceId, token)
                    .retrieve()
                    .body(StateResponse.class);
            return response != null ? response.getStateInstance() : null;
        } catch (Exception e) {
            throw failure("getStateInstance", instanceId, e);
        }
    }

    /**
     * URL: https://api.green-api.com/waInstance{instanceId}/sendMessage/{token}
     */
    public void sendMessage(String instanceId, String token, String chatId, String message) {
        try {
            restClient.post()
                    .uri(urlSendMessage, instanceId, token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("chatId", chatId, "message", message))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw failure("sendMessage", instanceId, e);
        }
    }

    /**
     * URL: https://api.green-api.com/waInstance{instanceId}/setSettings/{token}
     */
    public void setSettings(String instanceId, String token, Map<String, Object> settings) {
        try {
            restClient.post()
                    .uri(urlSetSettings, instanceId, token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(settings)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw failure("setSettings", instanceId, e);
        }
    }

    /**
     * Logs a failed call so that the cause is clear from the log alone, and returns an exception with the same text.
     * The token is part of every Green API URL and Spring puts the URL into I/O error messages,
     * so only the HTTP status or the network cause is used, never the original message. Message texts are not logged.
     */
    private RuntimeException failure(String method, String instanceId, Exception e) {
        String cause = describe(e);
        log.error("Green API {} failed for instance {}: {}", method, instanceId, cause);
        return new GreenApiException("Green API " + method + " failed: " + cause);
    }

    static String describe(Exception e) {
        if (e instanceof RestClientResponseException http) {
            String hint = switch (http.getStatusCode().value()) {
                case 400 -> "bad request";
                case 401, 403 -> "wrong idInstance or apiTokenInstance";
                case 429 -> "too many requests";
                case 466 -> "instance limit or tariff exceeded";
                default -> http.getStatusCode().is5xxServerError() ? "Green API server error" : "";
            };
            return "HTTP " + http.getStatusCode().value() + (hint.isEmpty() ? "" : " (" + hint + ")");
        }
        if (e instanceof ResourceAccessException) {
            Throwable root = e;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }
            return "network error: " + root.getClass().getSimpleName();
        }
        return e.getClass().getSimpleName();
    }

    /** A Green API call failed; the message is safe to log and show to the operator (no token, no message text) */
    public static class GreenApiException extends RuntimeException {
        public GreenApiException(String message) {
            super(message);
        }
    }

    @Data
    private static class QrResponse {
        private String type;
        private String message;
    }

    @Data
    private static class SettingsResponse {
        private String wid;
        private String phone;
    }

    @Data
    private static class StateResponse {
        // Green API: {"stateInstance": "authorized"}
        private String stateInstance;
    }
}
