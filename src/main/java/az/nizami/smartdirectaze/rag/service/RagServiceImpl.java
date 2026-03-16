package az.nizami.smartdirectaze.rag.service;

import az.nizami.smartdirectaze.rag.RagResponse;
import az.nizami.smartdirectaze.rag.RagService;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//@Component
@Log4j2
class RagServiceImpl implements RagService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient; // Используем ChatClient вместо RestClient
    private final JdbcTemplate jdbcTemplate;
//    private final SemanticCacheService semanticCacheService;
    private static final int ANSWER_SIZE = 2000;

    public RagServiceImpl(
            VectorStore vectorStore,
            ChatClient.Builder chatClientBuilder, // Внедряем Builder
//            SemanticCacheService semanticCacheService,
            JdbcTemplate jdbcTemplate
    ) {
        this.vectorStore = vectorStore;
        // Настраиваем ChatClient один раз при инициализации
        this.chatClient = chatClientBuilder.build();
        this.jdbcTemplate = jdbcTemplate;
//        this.semanticCacheService = semanticCacheService;
    }

    @Override
    public RagResponse ask(String question) {
        try {
            // 1. Semantic Cache Lookup
//            var cachedAnswer = semanticCacheService.getCachedAnswer(question, 0.95);
//            if (cachedAnswer.isPresent()) {
//                log.info("Returning cached response for: {}", question);
//                String answer = cachedAnswer.get();
//                return RagResponse.builder().answer(answer).isLarge(answer.length() > ANSWER_SIZE).build();
//            }

            // 2. Проверка базы данных
            if (isVectorStoreEmpty()) {
                return RagResponse.builder()
                        .answer("📭 <b>Məlumat bazası boşdur:</b> Hazırda sistemdə heç bir hüquqi sənəd yoxdur.")
                        .isLarge(false)
                        .build();
            }

            // 3. Similarity Search
            var docs = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(question)
                            .topK(5)
                            .similarityThreshold(0.35)
                            .build()
            );

            assert docs != null;
            if (docs.isEmpty()) {
                return RagResponse.builder()
                        .answer("🔍 <b>Məlumat tapılmadı:</b> Verilən sual üzrə Vergi Məcəlləsində uyğun bənd tapılmadı.")
                        .isLarge(false)
                        .build();
            }

            List<String> articleNumbers = docs.stream()
                    .map(d -> (String) d.getMetadata().get("article"))
                    .filter(a -> a != null && !a.equals("unknown"))
                    .distinct()
                    .toList();

            String context = docs.stream()
                    .map(Document::getContent)
                    .collect(Collectors.joining("\n\n"));

            // 4. Вызов AI через Spring AI ChatClient
            String aiResponse = chatClient.prompt()
                    .user(u -> u.text(prompt())
                            .params(Map.of(
                                    "context", context,
                                    "question", question
                            )))
                    .call()
                    .content();

            // 5. Очистка от возможных Markdown-артефактов
            assert aiResponse != null;
            String finalResult = aiResponse.replaceAll("(?i)```html|```markdown|```", "").trim();

            // 6. Сохранение в кэш
//            semanticCacheService.cacheResponse(question, finalResult);

            return RagResponse.builder()
                    .answer(finalResult)
                    .articles(articleNumbers)
                    .isLarge(finalResult.length() > ANSWER_SIZE)
                    .build();

        } catch (Exception e) {
            log.error("RAG Error: ", e);
            if (e.getMessage().contains("429")) {
                return RagResponse.builder()
                        .answer("⚠️ <b>Limit:</b> API sorğu limiti dolub. Bir az sonra yenidən cəhd edin.")
                        .isLarge(false)
                        .build();
            }
            return RagResponse.builder()
                    .answer("❌ <b>Xəta baş verdi:</b> Cavabı hazırlamaq mümkün olmadı.")
                    .isLarge(false)
                    .build();
        }
    }

    private boolean isVectorStoreEmpty() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT 1 FROM vector_store LIMIT 1", Integer.class);
            return count == null;
        } catch (Exception e) {
            return true;
        }
    }

    private String prompt() {
        return """
                                # ROLE
                                Сен — Smart-Direct AZE помощнигисян. Твоя цель — помогать клиентам покупать товары.
                                У тебя есть доступ к каталогу товаров.
    
                                # KONTEKST (Məcəllədən çıxarışlar):
                                {context}
                
                                # TƏLİMATLAR
                                Правила:
                                    1. Отвечай на том языке, на котором пишет клиент (Azerbaijani или Russian).
                                    2. Будь вежлив (используй "Hürmətli müştəri", "Buyurun").
                                    3. Если клиент спрашивает цену, ищи её в предоставленных данных.
                                    4. Если товара нет в списке, вежливо предложи оставить контакты.
                
                                # İSTİFADƏÇİ SUALI:
                                {question}
                """;
    }
}
