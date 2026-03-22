package az.nizami.smartdirectaze;

import lombok.Builder;

@Builder
public record ShopDto(Long ownerChatId, String botToken, String botUuid, Boolean isActive) { }
