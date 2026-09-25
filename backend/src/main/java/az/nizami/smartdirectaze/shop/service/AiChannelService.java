package az.nizami.smartdirectaze.shop.service;

import az.nizami.smartdirectaze.shop.dto.channel.AiSettingsDto;
import az.nizami.smartdirectaze.shop.dto.channel.WhatsAppQrResponse;
import az.nizami.smartdirectaze.shop.entities.AiChannelEntity;
import az.nizami.smartdirectaze.shop.entities.ShopEntity;

import java.util.List;

public interface AiChannelService {
    void connectTelegram(Long shopId, String token);
    void initWhatsApp(Long shopId, String instanceId, String token);
    WhatsAppQrResponse getWhatsAppQr(Long shopId);
    AiSettingsDto getWhatsAppAiSettings(Long shopId);
    AiSettingsDto updateWhatsAppAiSettings(Long shopId, AiSettingsDto settings);

    List<AiChannelEntity> createBaseChannels(ShopEntity shopEntity);
}
