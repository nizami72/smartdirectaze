package az.nizami.smartdirectaze.shop.dto.channel;

/**
 * A shop and its WhatsApp channel on the admin page.
 *
 * @param connectedPhone digits only; null until the number is connected
 */
public record AdminWhatsappShopDto(Long shopId, String shopName, String ownerEmail, String channelStatus,
                                   String instanceId, String connectedPhone, String aiMode) {
}
