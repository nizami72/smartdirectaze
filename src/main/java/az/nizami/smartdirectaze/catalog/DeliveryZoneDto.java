package az.nizami.smartdirectaze.catalog;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record DeliveryZoneDto(
        String name,
        BigDecimal price
) {
}
