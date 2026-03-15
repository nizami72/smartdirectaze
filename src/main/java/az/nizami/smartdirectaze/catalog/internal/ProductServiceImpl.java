package az.nizami.smartdirectaze.catalog.internal;

import az.nizami.smartdirectaze.catalog.ProductDTO;
import az.nizami.smartdirectaze.catalog.ProductService;
import az.nizami.smartdirectaze.catalog.internal.sync.CatalogSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
class ProductServiceImpl implements ProductService {

    private final CatalogSyncService catalogSyncService;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public void synchroniseProducts(){
        catalogSyncService.synchroniseGoogleSheetProducts();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductDTO> findForAiAssistant(String message) {
        List<ProductEntity> product = productRepository.searchByKeyword(message);
        if(!product.isEmpty() && product.getFirst()!=null) {
            return Optional.of(productMapper.toDto(product.getFirst()));
        }
        return Optional.empty();
    }


}