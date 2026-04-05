package az.nizami.smartdirectaze.catalog.service;

import az.nizami.smartdirectaze.catalog.*;
import az.nizami.smartdirectaze.catalog.entities.ProductEntity;
import az.nizami.smartdirectaze.catalog.entities.ShopEntity;
import az.nizami.smartdirectaze.catalog.internal.ProductMapper;
import az.nizami.smartdirectaze.catalog.internal.ProductRepository;
import az.nizami.smartdirectaze.catalog.internal.ShopMapper;
import az.nizami.smartdirectaze.catalog.internal.sync.CatalogSyncService;
import az.nizami.smartdirectaze.catalog.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class ProductServiceImpl implements ProductService {

    //<editor-fold desc="Fields">
    private final CatalogSyncService catalogSyncService;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ShopMapper shopMapper;
    private final ShopRepository shopRepository;
    private final FileStorageService fileStorageService;
    //</editor-fold>

    public void synchroniseProducts(){
        catalogSyncService.synchroniseGoogleSheetProducts();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> searchForAiAssistant(String message) {
        return productRepository.searchByKeyword(message).stream()
                .map(productMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> findProductDtoForShop(Long shopId) {
        return productRepository.findByShopId(shopId)
                .stream()
                .map(productMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ProductDTO addProduct(Long shopId, ProductDTO productDto, org.springframework.web.multipart.MultipartFile photo) {
        return shopRepository.findById(shopId)
                .map(shop -> {
                    ProductEntity entity = new ProductEntity();
                    productMapper.updateEntityFromDto(productDto, entity);
                    entity.setShopId(shop.getId());
                    if (entity.getSku() == null || productRepository.findBySku(entity.getSku()).isPresent()) {

                        entity.setSku(UUID.randomUUID().toString());
                    }
                    ProductEntity saved = productRepository.save(entity);

                    if (photo != null && !photo.isEmpty()) {
                        String photoPath = fileStorageService.storeProductPhoto(shopId, saved.getId(), photo);
                        saved.setMainImageUrl(photoPath);
                        saved = productRepository.save(saved);
                    }

                    return productMapper.toDto(saved);
                })
                .orElseThrow(() -> new RuntimeException("Shop not found with ID: " + shopId));
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long shopId, Long productId, ProductDTO productDto, org.springframework.web.multipart.MultipartFile photo) {
        return productRepository.findById(productId)
                .map(entity -> {
                    if (!entity.getShopId().equals(shopId)) {
                        throw new RuntimeException("Product " + productId + " does not belong to shop " + shopId);
                    }
                    productMapper.updateEntityFromDto(productDto, entity);

                    if (photo != null && !photo.isEmpty()) {
                        String photoPath = fileStorageService.storeProductPhoto(shopId, entity.getId(), photo);
                        entity.setMainImageUrl(photoPath);
                    }

                    ProductEntity saved = productRepository.save(entity);
                    return productMapper.toDto(saved);
                })
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
    }

    @Override
    @Transactional
    public void deleteProduct(Long shopId, Long productId) {
        productRepository.findById(productId)
                .ifPresent(entity -> {
                    if (!entity.getShopId().equals(shopId)) {
                        throw new RuntimeException("Product " + productId + " does not belong to shop " + shopId);
                    }
                    productRepository.delete(entity);
                });
    }

    @Override
    public ShopDto createShop(ShopDto shopDto) {
        String botUuid = UUID.randomUUID().toString();
        ShopEntity newShopEntity = ShopEntity.builder()
                .ownerId(shopDto.ownerId())
                .botToken(shopDto.botToken())
                .botUuid(botUuid)
                .isActive(shopDto.isActive())
                .build();
        return shopMapper.toDto(shopRepository.save(newShopEntity));
    }

    @Override
    public boolean isShopExist(Long shopId) {
        return shopRepository.findById(shopId).isPresent();
    }

    @Override
    public boolean isShopBelongToUser(Long shopId, Long currentUserId) {
        Optional<ShopEntity> shop = shopRepository.findById(shopId);
        return shop.isPresent() && shop.get().getOwnerId().equals(currentUserId);
    }

    @Override
    public byte[] loadProductPhoto(Long shopId, Long productId, String filename) {
        return fileStorageService.loadProductPhoto(shopId, productId, filename);
    }

    @Override
    public Optional<ShopDto> findByBotUuid(String botUuid) {
        Optional<ShopEntity> shop = shopRepository.findByBotUuid(botUuid);
        return shop.map(shopMapper::toDto);
    }

    @Override
    public Optional<ShopDto> findByOwnerId(Long ownerId) {
        Optional<ShopEntity> optionalShopEntity = shopRepository.findByOwnerId(ownerId);
        return optionalShopEntity.map(shopMapper::toDto);
    }

    @Override
    public void loadProducts(List<ProductDTO> products) {
        products.forEach(p -> {
            ProductEntity entity = new ProductEntity();
            productMapper.updateEntityFromDto(p, entity);
            productRepository.save(entity);
        });
    }

    @Override
    public ShopDto getShopById(Long shopId) {
        return shopRepository.findById(shopId)
                .map(shopMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Shop not found with ID: " + shopId));
    }

    @Override
    @Transactional
    public void updateDeliveryConfig(Long shopId,
                                     BigDecimal deliveryPrice,
                                     BigDecimal freeDeliveryThreshold,
                                     List<DeliveryZoneDto> zones,
                                     String regionsDeliveryInfo, String processingTimeRules, String deliveryWorkingHours,
                                     Boolean collectPhone, Boolean collectAddress, Boolean collectLandmark, Boolean collectLocation,
                                     Integer courierWaitingTime, Boolean fittingAllowed, java.math.BigDecimal refusalFee) {
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found with ID: " + shopId));
        shop.setDeliveryPrice(deliveryPrice);
        shop.setFreeDeliveryThreshold(freeDeliveryThreshold);
        shop.setZonesText(JsonUtil.toJson(zones));
        shop.setRegionsDeliveryInfo(regionsDeliveryInfo);
        shop.setProcessingTimeRules(processingTimeRules);
        shop.setDeliveryWorkingHours(deliveryWorkingHours);
        shop.setCollectPhone(collectPhone);
        shop.setCollectAddress(collectAddress);
        shop.setCollectLandmark(collectLandmark);
        shop.setCollectLocation(collectLocation);
        shop.setCourierWaitingTime(courierWaitingTime);
        shop.setFittingAllowed(fittingAllowed);
        shop.setRefusalFee(refusalFee);
        shopRepository.save(shop);
    }

}