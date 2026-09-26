package az.nizami.smartdirectaze.whatsapp;

/**
 * The seller wrote to a customer from the shop's phone (Green API "outgoingMessageReceived").
 */
public record WhatsappSellerMessageEvent(String instanceId, String chatId) {
}
