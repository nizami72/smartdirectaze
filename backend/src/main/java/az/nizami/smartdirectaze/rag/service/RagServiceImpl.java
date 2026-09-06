/*
package az.nizami.smartdirectaze.rag.service;

import az.nizami.smartdirectaze.rag.RagResponse;
import az.nizami.smartdirectaze.rag.RagService;
import lombok.extern.log4j.Log4j2;
// import org.springframework.ai.chat.client.ChatClient;
// import org.springframework.ai.document.Document;
// import org.springframework.ai.vectorstore.SearchRequest;
// import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//@Component
@Log4j2
class RagServiceImpl implements RagService {

    // private final VectorStore vectorStore;
    // private final ChatClient chatClient; // Используем ChatClient вместо RestClient
    private final JdbcTemplate jdbcTemplate;
//    private final SemanticCacheService semanticCacheService;
    private static final int ANSWER_SIZE = 2000;

    public RagServiceImpl(
            // VectorStore vectorStore,
            // ChatClient.Builder chatClientBuilder, // Внедряем Builder
//            SemanticCacheService semanticCacheService,
            JdbcTemplate jdbcTemplate
    ) {
        // this.vectorStore = vectorStore;
        // Настраиваем ChatClient один раз при инициализации
        // this.chatClient = chatClientBuilder.build();
        this.jdbcTemplate = jdbcTemplate;
//        this.semanticCacheService = semanticCacheService;
    }

    @Override
    public RagResponse ask(String question) {
        return RagResponse.builder()
                .answer("RAG service is currently disabled.")
                .isLarge(false)
                .build();
    }

    private boolean isVectorStoreEmpty() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT 1 FROM vector_store LIMIT 1", Integer.class);
            return count == null;
        } catch (Exception e) {
            return true;
        }
    }
}
*/
