package az.nizami.smartdirectaze.shop.service;

import az.nizami.smartdirectaze.shop.dto.channel.AdminWhatsappShopDto;
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

    // --- Admin: Green API instances of shops

    List<AdminWhatsappShopDto> listShopsForAdmin();

    /**
     * Binds a Green API instance to the shop and points its webhooks to this backend.
     */
    AdminWhatsappShopDto bindInstance(Long shopId, String instanceId, String apiToken);

    /**
     * Logs the number out and frees the instance, so it can be given to another shop.
     */
    AdminWhatsappShopDto unbindInstance(Long shopId);
    void disconnectWhatsApp(Long shopId);

    /**
     * Sets the current webhook URL and token in every bound instance (after moving to another address).
     *
     * @return per shop: "ok" or the error
     */
    java.util.Map<Long, String> reconfigureAllWebhooks();
}
