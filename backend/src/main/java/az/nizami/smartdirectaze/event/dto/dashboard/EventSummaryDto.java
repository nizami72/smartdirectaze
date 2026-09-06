package az.nizami.smartdirectaze.event.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventSummaryDto {
    private UUID id;
    private String name;
    private LocalDateTime dateTime;
    private String place;
    private long guestsCount;
}
