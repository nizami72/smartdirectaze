package az.nizami.smartdirectaze.ai.internal;

import az.nizami.smartdirectaze.ai.AiService;
import az.nizami.smartdirectaze.ai.AssistantResponse;
import az.nizami.smartdirectaze.catalog.ProductDTO;
import az.nizami.smartdirectaze.catalog.ProductService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.model.chat.ChatLanguageModel;

import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
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
        // Собираем агента: Модель + Инструменты + Промпт
        this.agent = AiServices.builder(SmartAssistantAgent.class)
                .chatLanguageModel(chatModel)
                .tools(catalogTools)
                .build();
    }

    @Override
    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<AssistantResponse> processQuery(String userMessage) {
        // 1. Отправляем в DeepSeek
        String aiTextMessage = agent.chat(userMessage);

        // 2. Упаковываем в твой DTO
        AssistantResponse response = AssistantResponse.builder()
                .message(aiTextMessage)
                .responseType(AssistantResponse.ResponseType.PRODUCT_INFO)
                .build();

        return CompletableFuture.completedFuture(response);
    }


    private List<ProductDTO> findProductInMessage(String message) {
        // The simplest logic: search by SKU or keywords
        // In the future, there will be a call to LLM (OpenAI/Gemini) to understand the context
        return productService.searchForAiAssistant(message);
    }

    private String formatProductInfo(ProductDTO product, String originalMessage) {
        // Determine the language (simplified: if there is Cyrillic - RU, otherwise - AZ)
        boolean isRussian = originalMessage.matches(".*[а-яА-Я].*");
        
        String name = isRussian ? product.getTitles().get("ru") : product.getTitles().get("az");
        if (name == null) name = product.getTitles().get("az"); // fallback

        if (isRussian) {
            return String.format("Товар: %s\nЦена: %.2f %s\nВ наличии: %d шт.\nSKU: %s",
                    name, product.getSalePrice(), product.getCurrency(), product.getStockQuantity(), product.getSku());
        } else {
            return String.format("Məhsul: %s\nQiymət: %.2f %s\nStokda: %d ədəd\nSKU: %s",
                    name, product.getSalePrice(), product.getCurrency(), product.getStockQuantity(), product.getSku());
        }
    }
}