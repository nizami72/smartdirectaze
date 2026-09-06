package az.nizami.smartdirectaze.shop;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderDTO {
    private Long id;
    private Long shopId;
    private String customerName;
    private String phoneNumber;
    private String deliveryAddress;
    private String itemsSummary;
    private String paymentMethod;
    private LocalDateTime createdAt;
}
