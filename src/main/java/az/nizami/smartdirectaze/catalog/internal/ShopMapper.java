package az.nizami.smartdirectaze.catalog.internal;

import az.nizami.smartdirectaze.catalog.DeliveryZoneDto;
import az.nizami.smartdirectaze.catalog.JsonUtil;
import az.nizami.smartdirectaze.catalog.ShopDto;
import az.nizami.smartdirectaze.catalog.entities.ShopEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShopMapper {


    public ShopDto toDto(ShopEntity entity) {
        if (entity == null) return null;

        List<DeliveryZoneDto> zones = JsonUtil.fromJson(entity.getZonesText(), JsonUtil.getListType(DeliveryZoneDto.class));

        return ShopDto.builder()
                .id(entity.getId())
                .ownerId(entity.getOwnerId())
                .botToken(entity.getBotToken())
                .botUuid(entity.getBotUuid())
                .isActive(entity.getIsActive())
                .knowledgeBase(entity.getKnowledgeBase())
                .adminAccessToken(entity.getAdminAccessToken())
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
                .build();
    }
}
