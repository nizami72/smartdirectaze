package az.nizami.smartdirectaze.catalog;

import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public interface ProductService {

    @Async
    @Transactional
    void synchroniseProducts();

    List<ProductDTO> searchForAiAssistant(String message);

    List<ProductDTO> findProductDtoForShop(Long botUuid);

    ProductDTO addProduct(Long shopId, ProductDTO product, org.springframework.web.multipart.MultipartFile photo);

    ProductDTO updateProduct(Long shopId, Long productId, ProductDTO productDto, org.springframework.web.multipart.MultipartFile photo);

    void deleteProduct(Long shopId, Long productId);

    ShopDto createShop(ShopDto shopDto);

    boolean isShopExist(Long botUuid);

    boolean isShopBelongToUser(Long shopUuid, Long currentUserId);

    byte[] loadProductPhoto(Long shopId, Long productId, String filename);

    Optional<ShopDto> findByBotUuid(String botUuid);

    Optional<ShopDto> findByOwnerId(Long ownerId);

    void loadProducts(List<ProductDTO> products);

    ShopDto getShopById(Long shopId);

    void updateDeliveryConfig(Long shopId, java.math.BigDecimal deliveryPrice, java.math.BigDecimal freeDeliveryThreshold, java.util.List<DeliveryZoneDto> zones,
                              String regionsDeliveryInfo, String processingTimeRules, String deliveryWorkingHours,
                              Boolean collectPhone, Boolean collectAddress, Boolean collectLandmark, Boolean collectLocation,
                              Integer courierWaitingTime, Boolean fittingAllowed, java.math.BigDecimal refusalFee);
}