package az.nizami.smartdirectaze.ai.internal;

import az.nizami.smartdirectaze.catalog.ProductDTO;
import az.nizami.smartdirectaze.catalog.ProductService;
import az.nizami.smartdirectaze.catalog.ShopDto;
import dev.langchain4j.agent.tool.P;
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

    @Tool("Get the rules, prices, and delivery times for the current shop.")
    public String getDeliveryInfo(@P("The ID of the shop to get delivery info for") Long shopId) {
        ShopDto shop = productService.getShopById(shopId);
        return String.format("Delivery price: %s %s. Free delivery from: %s %s. Delivery zones: %s.",
                shop.deliveryPrice() != null ? shop.deliveryPrice() : "0",
                "AZN",
                shop.freeDeliveryThreshold() != null ? shop.freeDeliveryThreshold() : "0",
                "AZN",
                shop.zonesText() != null ? shop.zonesText() : "Not specified");
    }
}

