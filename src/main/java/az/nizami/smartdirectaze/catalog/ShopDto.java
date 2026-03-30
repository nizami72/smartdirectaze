package az.nizami.smartdirectaze.catalog;

import lombok.Builder;

@Builder
public record ShopDto(
        Long id,
        Long ownerId,
        String botToken,
        String botUuid,
        Boolean isActive,
        String knowledgeBase,
        String adminAccessToken
) {
}
