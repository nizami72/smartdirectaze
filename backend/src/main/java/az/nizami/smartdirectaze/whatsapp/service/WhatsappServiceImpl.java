package az.nizami.smartdirectaze.whatsapp.service;

import az.nizami.smartdirectaze.whatsapp.GreenApiResponseDto;
import az.nizami.smartdirectaze.whatsapp.client.GreenApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsappServiceImpl implements WhatsappService {

    private final GreenApiClient greenApiClient;
    private final String webhookUrl;
    private final String webhookToken;

    public WhatsappServiceImpl(GreenApiClient greenApiClient,
                               @Value("${app.whatsapp.webhook-url}") String webhookUrl,
                               @Value("${app.whatsapp.webhook-token:}") String webhookToken) {
        this.greenApiClient = greenApiClient;
        this.webhookUrl = webhookUrl;
        this.webhookToken = webhookToken;
    }

    @Override
    public GreenApiResponseDto getQrCode(String instanceId, String token) {
        return greenApiClient.getQrCode1(instanceId, token);
    }

    @Override
    public String getStateInstance(String instanceId, String token) {
        return greenApiClient.getStateInstance(instanceId, token);
    }

    @Override
    public String getSettings(String instanceId, String token) {
        return greenApiClient.getSettings(instanceId, token);
    }

    @Override
    public void logout(String instanceId, String token) {
        greenApiClient.logout(instanceId, token);
    }

    @Override
    public void sendMessage(String instanceId, String token, String chatId, String message) {
        greenApiClient.sendMessage(instanceId, token, chatId, message);
    }

    @Override
    public void configureWebhooks(String instanceId, String token) {
        Map<String, Object> settings = new HashMap<>();
        settings.put("webhookUrl", webhookUrl);
        settings.put("webhookUrlToken", webhookToken);
        settings.put("incomingWebhook", "yes");
        // Messages the seller sends from the phone: the AI pauses in that chat
        settings.put("outgoingMessageWebhook", "yes");
        // Our own API messages and delivery statuses are not needed
        settings.put("outgoingAPIMessageWebhook", "no");
        settings.put("outgoingWebhook", "no");
        settings.put("stateWebhook", "yes");
        greenApiClient.setSettings(instanceId, token, settings);
    }
}
