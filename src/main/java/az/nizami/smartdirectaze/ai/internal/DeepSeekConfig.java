package az.nizami.smartdirectaze.ai.internal;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeepSeekConfig {

    @Bean
    public OpenAiChatModel chatModel(@Value("${app.ai.deepseek.api-key}") String apiKey) {

        var openAiApi = new OpenAiApi("https://api.deepseek.com", apiKey);
        return new OpenAiChatModel(openAiApi, OpenAiChatOptions.builder()
                .model("deepseek-chat") // Или deepseek-reasoner для сложных задач
                .temperature(0.0) // Ставим 0 для точных данных по ценам
                .build());
    }

}