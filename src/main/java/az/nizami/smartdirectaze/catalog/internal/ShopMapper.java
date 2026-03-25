package az.nizami.smartdirectaze.catalog.internal;

import az.nizami.smartdirectaze.catalog.ShopDto;
import az.nizami.smartdirectaze.catalog.entities.ShopEntity;
import org.springframework.stereotype.Component;

@Component
public class ShopMapper {


    public ShopDto toDto(ShopEntity entity) {
        return ShopDto.builder()
                .id(entity.getId())
                .ownerChatId(entity.getOwnerChatId())
                .botToken(entity.getBotToken())
                .botUuid(entity.getBotUuid())
                .isActive(entity.getIsActive())
                .knowledgeBase(entity.getKnowledgeBase())
                .adminAccessToken(entity.getAdminAccessToken())
                .build();


    }
}
