package az.nizami.smartdirectaze.catalog.internal.sync;

import az.nizami.smartdirectaze.catalog.ProductDTO;
import az.nizami.smartdirectaze.catalog.internal.ProductEntity;
import az.nizami.smartdirectaze.catalog.internal.ProductMapper;
import az.nizami.smartdirectaze.catalog.internal.ProductRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Log4j2
public class CatalogSyncService {
    private final List<ProductSourceAdapter> adapters;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public CatalogSyncService(List<ProductSourceAdapter> adapters,
                              ProductRepository productRepository,
                              ProductMapper productMapper) {
        this.adapters = adapters;
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }


    @Scheduled(cron = "${app.catalog.sync.cron}")
    public void scheduleGoogleSheetsSynchronisation() {
        synchroniseGoogleSheetProducts();
    }

    @Transactional
    public void synchroniseGoogleSheetProducts() {
        adapters.stream()
                .filter(a -> a.getSourceType() == SourceType.GOOGLE_SHEETS)
                .findFirst()
                .ifPresent(adapter -> {
                    List<ProductDTO> externalProducts = adapter.fetchProducts();
                    log.debug("Google sheet [{}]", externalProducts);
                    externalProducts.forEach(this::saveProduct);
                });
    }

    protected void saveProduct(ProductDTO productDTO) {
        ProductEntity entity = productRepository.findBySku(productDTO.getSku())
                .orElseGet(ProductEntity::new);
        if(productDTO.getSku() == null) return;// todo check not null fields and ignore those with null
        productMapper.updateEntityFromDto(productDTO, entity);
        productRepository.save(entity);
    }
}