package az.nizami.smartdirectaze.ai.internal;

import az.nizami.smartdirectaze.ai.AiService;
import az.nizami.smartdirectaze.ai.AssistantResponse;
import az.nizami.smartdirectaze.shop.ProductService;
import az.nizami.smartdirectaze.util.HtmlUtils;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.model.chat.ChatLanguageModel;

import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final ProductService productService;
    private final ChatLanguageModel chatModel;
    private final CatalogTools catalogTools;
    private SmartAssistantAgent agent; // Интерфейс-агент

    @PostConstruct
    public void init() {
        // Создаем фабрику памяти: храним последние 10 сообщений (5 вопросов и 5 ответов)
        ChatMemoryProvider chatMemoryProvider = memoryId ->
                MessageWindowChatMemory.withMaxMessages(10);

        // Собираем агента: Модель + Инструменты + Промпт
        this.agent = AiServices.builder(SmartAssistantAgent.class)
                .chatLanguageModel(chatModel)
                .tools(catalogTools)
                .chatMemoryProvider(chatMemoryProvider) // Включаем память!
                .build();
    }

    @Override
    @Async
    public CompletableFuture<AssistantResponse> processQuery(String botUuid, String chatId, String userMessage) {
        // 1. Get shopId from botUuid
        Long shopId = productService.findByBotUuid(botUuid)
                .map(az.nizami.smartdirectaze.shop.ShopDto::id)
                .orElseThrow(() -> new RuntimeException("Shop not found for botUuid: " + botUuid));

        // 2. Send it to Agent
        String aiTextMessage = agent.chat(new ConversationKey(shopId, "tg:" + botUuid + ":" + chatId), userMessage);
        
        // 2.1 Convert Markdown to HTML for Telegram
        String htmlMessage = HtmlUtils.convertMdToTelegramHtml(aiTextMessage);

        // 3. Wrap in DTO
        AssistantResponse response = AssistantResponse.builder()
                .message(htmlMessage)
                .responseType(AssistantResponse.ResponseType.PRODUCT_INFO)
                .build();

        return CompletableFuture.completedFuture(response);
    }


    // No transaction here: it would hold a DB connection for the whole LLM call; tools open their own
    @Override
    public String answer(Long shopId, String conversationId, String userMessage) {
        return agent.chat(new ConversationKey(shopId, conversationId), userMessage);
    }
}
