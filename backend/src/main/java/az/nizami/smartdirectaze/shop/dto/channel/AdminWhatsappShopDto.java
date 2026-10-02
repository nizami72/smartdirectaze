package az.nizami.smartdirectaze.shop.dto.channel;

/**
 * A shop and its WhatsApp channel on the admin page.
 *
 * @param connectedPhone digits only; null until the number is connected
 * @param riskAcceptedAt when the merchant confirmed the WhatsApp ban risk; null = not yet
 */
public record AdminWhatsappShopDto(Long shopId, String shopName, String ownerEmail, String channelStatus,
                                   String instanceId, String connectedPhone, String aiMode, String riskAcceptedAt) {
}
