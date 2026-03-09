package az.nizami.smartdirectaze.catalog.internal;

import az.nizami.smartdirectaze.catalog.ProductDTO;
import az.nizami.smartdirectaze.catalog.ProductService;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

@Component
class ProductServiceImpl implements ProductService {

    @Override
    public Optional<ProductDTO> getProductBySku(String sku) {
        return Optional.empty();
    }
}