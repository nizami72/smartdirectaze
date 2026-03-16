package az.nizami.smartdirectaze.ai;

import java.util.concurrent.CompletableFuture;

public interface AiService {
    CompletableFuture<AssistantResponse> processQuery(String userMessage);
}