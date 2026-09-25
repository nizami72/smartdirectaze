package az.nizami.smartdirectaze.shop.service;

import az.nizami.smartdirectaze.business.BusinessChildCounter;
import az.nizami.smartdirectaze.business.Industry;
import az.nizami.smartdirectaze.shop.repositories.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
class ShopBusinessCounter implements BusinessChildCounter {

    // Repository, not ShopService: ShopService depends on BusinessService, which collects these counters
    private final ShopRepository shopRepository;

    @Override
    public Industry industry() {
        return Industry.SHOP;
    }

    @Override
    public long countByBusiness(UUID businessId) {
        return businessId == null ? 0L : shopRepository.countByBusinessId(businessId);
    }
}
