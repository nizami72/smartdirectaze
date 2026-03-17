package az.nizami.smartdirectaze.ai.internal;

import az.nizami.smartdirectaze.catalog.ProductDTO;
import az.nizami.smartdirectaze.catalog.ProductService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CatalogTools {

    private final ProductService productService;

    @Tool("Search for products in the store catalog by name or SKU to get current prices and stock.")
    public List<ProductDTO> searchProduct(String query) {
        // Вызываем твой метод из модуля Catalog
        return productService.searchForAiAssistant(query);
    }
}

