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

    List<ProductDTO> findProductDtoForShop(Long botUuid);

    ProductDTO addProduct(Long shopId, ProductDTO product);

    ProductDTO updateProduct(Long shopId, Long productId, ProductDTO productDto);

    void deleteProduct(Long shopId, Long productId);

    ShopDto createShop(ShopDto shopDto);

    boolean isShopExist(Long botUuid);

    boolean isShopBelongToUser(Long shopUuid, Long currentUserId);
}