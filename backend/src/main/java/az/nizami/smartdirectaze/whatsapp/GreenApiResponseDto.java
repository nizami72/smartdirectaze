package az.nizami.smartdirectaze.whatsapp;

import lombok.Builder;

@Builder
public record GreenApiResponseDto(String type, String message) {
}
