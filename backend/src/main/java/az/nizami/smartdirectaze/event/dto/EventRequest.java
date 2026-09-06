package az.nizami.smartdirectaze.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {
    @NotBlank(message = "Event name is required")
    private String name;

    @NotNull(message = "Event date and time is required")
    private LocalDateTime dateTime;

    @NotBlank(message = "Event place is required")
    private String place;

    private String description;
}
