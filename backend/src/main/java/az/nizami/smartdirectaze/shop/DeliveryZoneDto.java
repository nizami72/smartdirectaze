package az.nizami.smartdirectaze.shop;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record DeliveryZoneDto(
        String name,
        BigDecimal price
) {
}
