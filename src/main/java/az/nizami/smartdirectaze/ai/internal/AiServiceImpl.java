package az.nizami.smartdirectaze.ai.internal;

import az.nizami.smartdirectaze.ai.AiService;
import az.nizami.smartdirectaze.catalog.ProductDTO;
import az.nizami.smartdirectaze.catalog.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final ProductService productService;

    public String processQuery(String userMessage) {
        // 1. Пытаемся найти товар в базе
        Optional<ProductDTO> productOpt = findProductInMessage(userMessage);

        if (productOpt.isPresent()) {
            ProductDTO product = productOpt.get();
            // 2. Формируем ответ на основе данных из БД
            return formatProductInfo(product, userMessage);
        }

        // 3. Если ничего не нашли - стандартный вежливый ответ
        return "Salam! İstədiyiniz məhsulu tapa bilmədim. Zəhmət olmasa adını və ya SKU kodunu dəqiqləşdirin.";
    }

    private Optional<ProductDTO> findProductInMessage(String message) {
        // Простейшая логика: ищем по SKU или ключевым словам
        // В будущем здесь будет вызов LLM (OpenAI/Gemini) для понимания контекста
        return productService.findForAiAssistant(message);
    }

    private String formatProductInfo(ProductDTO product, String originalMessage) {
        // Определяем язык (упрощенно: если есть кириллица - RU, иначе - AZ)
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