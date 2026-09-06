package az.nizami.smartdirectaze.whatsapp.service;

import az.nizami.smartdirectaze.whatsapp.GreenApiResponseDto;
import az.nizami.smartdirectaze.whatsapp.client.GreenApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WhatsappServiceImpl implements WhatsappService {

    private final GreenApiClient greenApiClient;

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
}
