package az.nizami.smartdirectaze.ai;

import java.util.concurrent.CompletableFuture;

public interface AiService {
    /**
     * Answers a Telegram customer of the shop behind the bot; the message is Telegram HTML.
     */
    CompletableFuture<AssistantResponse> processQuery(String botUuid, String chatId, String userMessage);

    /**
     * Answers a customer of the given shop. Returns the model's raw (Markdown) text.
     *
     * @param conversationId key of the chat memory, must be unique per customer conversation
     */
    String answer(Long shopId, String conversationId, String userMessage);
}