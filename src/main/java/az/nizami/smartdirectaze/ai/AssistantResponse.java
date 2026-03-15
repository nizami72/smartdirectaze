package az.nizami.smartdirectaze.ai;

import az.nizami.smartdirectaze.catalog.ProductDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AssistantResponse {
    
    // Текстовое сообщение, которое увидит клиент
    private String message;
    
    // Код языка, на котором сформирован ответ (az, ru, en)
    private String languageCode;
    
    // Список найденных продуктов (если запрос был поисковым)
    // Это позволит фронтенду или мессенджеру отобразить карточки товаров
    private List<ProductDTO> foundProducts;
    
    // Тип ответа: PRODUCT_INFO, GREETING, PRICE_QUERY, NOT_FOUND, HUMAN_NEEDED
    private ResponseType responseType;
    
    // Флаг уверенности ИИ (от 0.0 до 1.0)
    // Если уверенность низкая, мы можем переключить чат на живого оператора в Баку
    private Double confidenceScore;

    public enum ResponseType {
        GREETING,      // Приветствие
        PRODUCT_INFO,  // Информация о конкретном товаре
        SEARCH_RESULTS,// Список товаров по запросу
        NOT_FOUND,     // Ничего не нашли
        HUMAN_NEEDED,  // Нужна помощь человека (сложный вопрос)
        ERROR          // Техническая ошибка
    }
}