package az.nizami.smartdirectaze.shop.dto;

import az.nizami.smartdirectaze.shop.entities.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateDto(
        @NotNull OrderStatus status
) {
}
