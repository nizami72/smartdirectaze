package az.nizami.smartdirectaze.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseShopDto {
    private String shopName;
    private String address;
    private String workingHours;
    private BigDecimal deliveryPrice;
    private BigDecimal freeDeliveryThreshold;
}
