package az.nizami.smartdirectaze.catalog;

import java.util.Optional;

public interface ProductService {
    Optional<ProductDTO> getProductBySku(String sku);
    // Другие методы, доступные другим модулям
}