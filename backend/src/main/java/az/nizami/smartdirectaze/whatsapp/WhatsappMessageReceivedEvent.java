package az.nizami.smartdirectaze.whatsapp;

/**
 * Published when a customer sends a message to a shop's WhatsApp number (Green API webhook).
 *
 * @param text        null for media (voice, photo, ...)
 * @param messageType Green API typeMessage, e.g. "textMessage", "audioMessage", "imageMessage"
 */
public record WhatsappMessageReceivedEvent(String instanceId, String chatId, String senderName,
                                           String text, String messageType) {
}
