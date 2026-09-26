package az.nizami.smartdirectaze.shop.dto.channel;

import jakarta.validation.constraints.NotBlank;

public record BindInstanceRequest(@NotBlank String instanceId, @NotBlank String apiToken) {
}
