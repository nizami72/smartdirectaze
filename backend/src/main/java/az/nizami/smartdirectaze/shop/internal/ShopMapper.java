package az.nizami.smartdirectaze.shop.internal;

import az.nizami.smartdirectaze.shop.DeliveryZoneDto;
import az.nizami.smartdirectaze.shop.JsonUtil;
import az.nizami.smartdirectaze.shop.PaymentMethod;
import az.nizami.smartdirectaze.shop.ShopDto;
import az.nizami.smartdirectaze.shop.entities.ShopEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShopMapper {


    public ShopDto toDto(ShopEntity entity) {
        if (entity == null) return null;

        List<DeliveryZoneDto> zones = JsonUtil.fromJson(entity.getZonesText(), JsonUtil.getListType(DeliveryZoneDto.class));
        List<PaymentMethod> paymentMethods = JsonUtil.fromJson(entity.getPaymentMethodsJson(), JsonUtil.getListType(PaymentMethod.class));

        return ShopDto.builder()
                .id(entity.getId())
                .ownerId(entity.getOwnerId())
                .shopName(entity.getShopName())
                .isActive(entity.getIsActive())
                .knowledgeBase(entity.getKnowledgeBase())
                .deliveryPrice(entity.getDeliveryPrice())
                .freeDeliveryThreshold(entity.getFreeDeliveryThreshold())
                .zones(zones)
                .regionsDeliveryInfo(entity.getRegionsDeliveryInfo())
                .processingTimeRules(entity.getProcessingTimeRules())
                .deliveryWorkingHours(entity.getDeliveryWorkingHours())
                .collectPhone(entity.getCollectPhone())
                .collectAddress(entity.getCollectAddress())
                .collectLandmark(entity.getCollectLandmark())
                .collectLocation(entity.getCollectLocation())
                .courierWaitingTime(entity.getCourierWaitingTime())
                .fittingAllowed(entity.getFittingAllowed())
                .refusalFee(entity.getRefusalFee())
                .tryingReturnsPolicy(entity.getTryingReturnsPolicy())
                .workingHours(entity.getWorkingHours())
                .address(entity.getAddress())
                .paymentMethods(paymentMethods)
                .botUuid(entity.getBotUuid())
                .build();
    }
}
