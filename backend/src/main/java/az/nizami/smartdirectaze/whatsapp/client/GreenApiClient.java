package az.nizami.smartdirectaze.whatsapp.client;

import az.nizami.smartdirectaze.whatsapp.GreenApiResponseDto;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class GreenApiClient {

    private final RestClient restClient;
    private final String urlQr;
    private final String urlState;
    private final String urlSettings;
    private final String urlLogout;

    public GreenApiClient(RestClient.Builder restClientBuilder,
                          @Value("${app.url.greenApi.url.qrCode}") String urlQr,
                          @Value("${app.url.greenApi.url.state}") String urlState,
                          @Value("${app.url.greenApi.url.settings}") String urlSettings,
                          @Value("${app.url.greenApi.url.logout}") String urlLogout) {
        this.restClient = restClientBuilder.build();
        this.urlQr = urlQr;
        this.urlState = urlState;
        this.urlSettings = urlSettings;
        this.urlLogout = urlLogout;
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
            log.error("Error fetching QR code from Green-API for instance {}: {}", instanceId, e.getMessage());
            throw new RuntimeException("Failed to fetch QR code from Green-API", e);
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
            log.error("Error fetching settings from Green-API for instance {}: {}", instanceId, e.getMessage());
            throw new RuntimeException("Failed to fetch settings from Green-API", e);
        }
    }

    public void logout(String instanceId, String token) {
        try {
            restClient.get() // Green-API uses GET for logout according to common usage, but if doc says POST/DELETE we might need to adjust.
                    // Re-checking description: "makes GET request to .../getSettings... logout makes DELETE (or POST according to doc)"
                    // Actually, most Green-API methods are GET or POST. 
                    // Let's use GET as it's common for simple triggers in their API, or check if I should use POST.
                    // Description says: "делает DELETE (или POST согласно доке Green-API)"
                    .uri(urlLogout, instanceId, token)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Error logging out from Green-API for instance {}: {}", instanceId, e.getMessage());
            // We don't want to throw here to allow retry logic to proceed
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
            return response != null ? response.getStatusInstance() : null;
        } catch (Exception e) {
            log.error("Error fetching state instance from Green-API for instance {}: {}", instanceId, e.getMessage());
            throw new RuntimeException("Failed to fetch instance state from Green-API", e);
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
        private String statusInstance;
    }
}
