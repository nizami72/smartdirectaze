package az.nizami.smartdirectaze.shop.dto.channel;

import az.nizami.smartdirectaze.shop.AiMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSettingsDto {
    @NotNull
    private AiMode aiMode;
    @NotNull
    @Size(max = 10)
    private Set<String> testPhones;
}
