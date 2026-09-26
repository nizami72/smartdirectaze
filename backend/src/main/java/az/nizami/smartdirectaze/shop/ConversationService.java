package az.nizami.smartdirectaze.shop;

import java.util.List;

/**
 * Who answers a customer's WhatsApp chat: the AI seller or the seller himself.
 */
public interface ConversationService {

    /**
     * Stores the customer's message and tells whether the AI must stay silent in this chat
     * (handed over to the seller and the pause is not over yet).
     *
     * @param message text, or a label like "[голосовое сообщение]"
     */
    boolean recordIncomingAndCheckPaused(Long shopId, String chatId, String customerName, String message);

    /**
     * Hands the chat over to the seller: the AI goes silent in it and the seller gets an alert
     * (at most one per chat every 30 minutes).
     */
    void handOverToSeller(Long shopId, String chatId, String reason);

    List<ConversationDto> findWaitingForSeller(Long shopId);

    /**
     * "Вернуть AI" in the dashboard.
     */
    void resumeAi(Long shopId, Long conversationId);
}
