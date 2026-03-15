package az.nizami.smartdirectaze.catalog.internal;

import az.nizami.smartdirectaze.catalog.ProductService;
import az.nizami.smartdirectaze.catalog.internal.sync.CatalogSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class ProductServiceImpl implements ProductService {

    private final CatalogSyncService catalogSyncService;

    public void synchroniseProducts(){
        catalogSyncService.synchroniseGoogleSheetProducts();
    }
}