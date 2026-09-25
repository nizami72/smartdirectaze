package az.nizami.smartdirectaze.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CatalogItemRequestDto {
    @NotNull(message = "Магазин обязателен")
    private Long shopId;

    @NotBlank(message = "Название товара обязательно")
    private String name;

    @NotNull(message = "Цена обязательна")
    @Positive(message = "Цена должна быть положительной")
    private BigDecimal price;

    private String description;
}
