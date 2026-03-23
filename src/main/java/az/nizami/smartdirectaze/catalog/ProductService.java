package az.nizami.smartdirectaze.catalog;

import az.nizami.smartdirectaze.ShopDto;
import az.nizami.smartdirectaze.catalog.entities.ShopEntity;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ProductService {

    @Async
    @Transactional
    void synchroniseProducts();

    List<ProductDTO> searchForAiAssistant(String message);

    List<ProductDTO> findProductDtoForShop(String botUuid);

    ProductDTO addProduct(String xShopToken, ProductDTO product);

    String createShop(ShopDto shopDto);
}