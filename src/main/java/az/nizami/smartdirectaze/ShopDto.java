package az.nizami.smartdirectaze;

import lombok.Builder;

@Builder
public record ShopDto(
        Long id,
        Long ownerChatId,
        String botToken,
        String botUuid,
        Boolean isActive,
        String knowledgeBase,
        String adminAccessToken
) {
}
