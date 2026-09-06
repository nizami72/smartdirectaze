package az.nizami.smartdirectaze.event.service;

import az.nizami.smartdirectaze.event.dto.EventRequest;
import az.nizami.smartdirectaze.event.dto.EventResponse;
import az.nizami.smartdirectaze.event.dto.dashboard.EventsDashboardResponse;

import java.util.List;
import java.util.UUID;

public interface EventService {
    EventResponse createEvent(EventRequest request);
    EventResponse getEvent(UUID id);
    List<EventResponse> getAllEvents();
    EventResponse updateEvent(UUID id, EventRequest request);
    void deleteEvent(UUID id);

    EventsDashboardResponse getEventsDashboard();
}
