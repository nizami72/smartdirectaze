package az.nizami.smartdirectaze.catalog.internal;

import az.nizami.smartdirectaze.ShopDto;
import az.nizami.smartdirectaze.catalog.BotNotFoundException;
import az.nizami.smartdirectaze.catalog.ProductDTO;
import az.nizami.smartdirectaze.catalog.ProductService;
import az.nizami.smartdirectaze.catalog.entities.ShopEntity;
import az.nizami.smartdirectaze.catalog.internal.sync.CatalogSyncService;
import az.nizami.smartdirectaze.catalog.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final ShopRepository shopRepository;
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
    public List<ProductDTO> findProductDtoForShop(String botUuid) {

        return shopRepository.findByBotUuid(botUuid)
        .map(shop -> productRepository.findByShopId(shop.getId()).stream()
                .map(productMapper::toDto)
                .toList())
        .orElseThrow(() -> new BotNotFoundException("Bot not found for UUID: " + botUuid));
    }

    @Override
    @Transactional
    public ProductDTO addProduct(String xShopToken, ProductDTO productDto) {
        return shopRepository.findByBotUuid(xShopToken)
                .map(shop -> {
                    ProductEntity entity = new ProductEntity();
                    productMapper.updateEntityFromDto(productDto, entity);
                    entity.setShopId(shop.getId());
                    if (entity.getSku() == null) {
                        entity.setSku(UUID.randomUUID().toString());
                    }
                    ProductEntity saved = productRepository.save(entity);
                    return productMapper.toDto(saved);
                })
                .orElseThrow(() -> new RuntimeException("Shop not found with token: " + xShopToken));
    }

    @Override
    public String createShop(ShopDto shopDto) {
        String botUuid = UUID.randomUUID().toString();
        ShopEntity newShopEntity = ShopEntity.builder()
                .ownerChatId(shopDto.ownerChatId())
                .botToken(shopDto.botToken())
                .botUuid(botUuid)
                .isActive(shopDto.isActive())
                .build();
        shopRepository.save(newShopEntity);
        return botUuid;
    }


}