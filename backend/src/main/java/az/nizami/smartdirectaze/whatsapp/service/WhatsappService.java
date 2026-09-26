package az.nizami.smartdirectaze.whatsapp.service;

import az.nizami.smartdirectaze.whatsapp.GreenApiResponseDto;

public interface WhatsappService {
    GreenApiResponseDto getQrCode(String instanceId, String token);
    String getStateInstance(String instanceId, String token);
    String getSettings(String instanceId, String token);
    void logout(String instanceId, String token);
    void sendMessage(String instanceId, String token, String chatId, String message);

    /**
     * Points the instance's webhooks to this backend: incoming messages, messages the seller sends
     * from the phone, connection state.
     */
    void configureWebhooks(String instanceId, String token);
}
