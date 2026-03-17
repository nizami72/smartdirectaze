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

    //<editor-fold desc="Fields">
    private final CatalogSyncService catalogSyncService;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
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


}