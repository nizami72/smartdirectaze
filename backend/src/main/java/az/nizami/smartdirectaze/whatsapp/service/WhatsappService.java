package az.nizami.smartdirectaze.whatsapp.service;

import az.nizami.smartdirectaze.whatsapp.GreenApiResponseDto;

public interface WhatsappService {
    GreenApiResponseDto getQrCode(String instanceId, String token);
    String getStateInstance(String instanceId, String token);
    String getSettings(String instanceId, String token);
    void logout(String instanceId, String token);
}
