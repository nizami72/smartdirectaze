package az.nizami.smartdirectaze.whatsapp;

/**
 * Published when a customer sends a text message to a shop's WhatsApp number (Green API webhook).
 */
public record WhatsappMessageReceivedEvent(String instanceId, String chatId, String text) {
}
