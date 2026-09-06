package az.nizami.smartdirectaze.event.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventsDashboardResponse {
    private long totalEvents;
    private long upcomingEvents;
    private long totalGuests;
    private List<EventSummaryDto> events;
}
