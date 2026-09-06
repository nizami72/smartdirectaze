package az.nizami.smartdirectaze.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record ShopCreateDto(
    @NotBlank(message = "Shop name is required")
    String shopName,

    @NotBlank(message = "Address is required")
    String address,

    @NotBlank(message = "Working hours are required")
    String workingHours,

    @PositiveOrZero(message = "Delivery price must be positive or zero")
    BigDecimal deliveryPrice,

    @PositiveOrZero(message = "Free delivery threshold must be positive or zero")
    BigDecimal freeDeliveryThreshold,

    Boolean fittingAllowed,

    @PositiveOrZero(message = "Refusal fee must be positive or zero")
    BigDecimal refusalFee
) {}
