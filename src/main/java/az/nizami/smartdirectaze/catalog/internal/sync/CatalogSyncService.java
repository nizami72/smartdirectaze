package az.nizami.smartdirectaze.catalog.internal.sync;

import az.nizami.smartdirectaze.catalog.ProductDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Log4j2
public class CatalogSyncService {
    private final List<ProductSourceAdapter> adapters;
    private final ProductSourceAdapter adminService;

    public CatalogSyncService(List<ProductSourceAdapter> adapters, ProductSourceAdapter adminService) {
        this.adapters = adapters;
        this.adminService = adminService;
    }

    @Scheduled(cron = "${app.catalog.sync.cron}")
    public void syncGoogleSheets() {
        adapters.stream()
            .filter(a -> a.getSourceType() == SourceType.GOOGLE_SHEETS)
            .findFirst()
            .ifPresent(adapter -> {
                List<ProductDTO> externalProducts = adapter.fetchProducts();
                log.debug("Google sheet [{}]", externalProducts);
                // save products in db
            });
    }
}