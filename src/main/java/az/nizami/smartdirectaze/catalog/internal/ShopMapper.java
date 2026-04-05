package az.nizami.smartdirectaze.catalog.internal;

import az.nizami.smartdirectaze.catalog.ShopDto;
import az.nizami.smartdirectaze.catalog.entities.ShopEntity;
import org.springframework.stereotype.Component;

@Component
public class ShopMapper {


    public ShopDto toDto(ShopEntity entity) {
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
                .zonesText(entity.getZonesText())
                .build();


    }
}
