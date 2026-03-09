package az.nizami.smartdirectaze.catalog;

import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public interface ProductService {
    Optional<ProductDTO> getProductBySku(String sku);
    // Другие методы, доступные другим модулям
}