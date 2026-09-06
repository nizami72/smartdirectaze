package az.nizami.smartdirectaze.shop.dto.channel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TelegramChannelRequest {
    @NotNull
    private Long shopId;
    @NotBlank
    private String token;
}
