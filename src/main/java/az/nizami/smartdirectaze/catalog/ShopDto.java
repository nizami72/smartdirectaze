package az.nizami.smartdirectaze.catalog;

import lombok.Builder;

import java.util.List;

@Builder
public record ShopDto(
        Long id,
        Long ownerId,
        String botToken,
        String botUuid,
        Boolean isActive,
        String knowledgeBase,
        String adminAccessToken,
        java.math.BigDecimal deliveryPrice,
        java.math.BigDecimal freeDeliveryThreshold,
        List<DeliveryZoneDto> zones,
        String regionsDeliveryInfo,
        String processingTimeRules,
        String deliveryWorkingHours,
        Boolean collectPhone,
        Boolean collectAddress,
        Boolean collectLandmark,
        Boolean collectLocation,
        Integer courierWaitingTime,
        Boolean fittingAllowed,
        java.math.BigDecimal refusalFee
) {
}
