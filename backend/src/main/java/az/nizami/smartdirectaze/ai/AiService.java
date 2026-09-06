package az.nizami.smartdirectaze.ai;

import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.CompletableFuture;

public interface AiService {
    CompletableFuture<AssistantResponse> processQuery(String chatId, String userMessage);
    CompletableFuture<AssistantResponse> processQuery(String botUuid, Update userMessage);
}