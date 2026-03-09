
package az.nizami.smartdirectaze.catalog.internal.sync;

import az.nizami.smartdirectaze.catalog.ProductDTO;
import java.util.List;

public interface ProductSourceAdapter {
    // Получить список товаров из внешнего источника
    List<ProductDTO> fetchProducts();
    
    // Поддерживаемый тип источника
    SourceType getSourceType();
}