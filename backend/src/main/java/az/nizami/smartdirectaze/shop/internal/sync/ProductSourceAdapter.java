
package az.nizami.smartdirectaze.shop.internal.sync;

import az.nizami.smartdirectaze.shop.ProductDTO;
import java.util.List;

public interface ProductSourceAdapter {
    // Получить список товаров из внешнего источника
    List<ProductDTO> fetchProducts();
    
    // Поддерживаемый тип источника
    SourceType getSourceType();
}