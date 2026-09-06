package az.nizami.smartdirectaze.event.service;

import az.nizami.smartdirectaze.event.domain.Event;
import az.nizami.smartdirectaze.event.dto.EventRequest;
import az.nizami.smartdirectaze.event.dto.EventResponse;
import az.nizami.smartdirectaze.event.dto.dashboard.EventsDashboardResponse;
import az.nizami.smartdirectaze.event.exception.ResourceNotFoundException;
import az.nizami.smartdirectaze.event.mapper.EventMapper;
import az.nizami.smartdirectaze.event.repository.EventRepository;
import az.nizami.smartdirectaze.event.repository.EventGuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventGuestRepository eventGuestRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public EventResponse createEvent(EventRequest request) {
        Event event = eventMapper.toEntity(request);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toResponse(savedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEvent(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventResponse updateEvent(UUID id, EventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        eventMapper.updateEntity(request, event);
        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toResponse(updatedEvent);
    }

    @Override
    @Transactional
    public void deleteEvent(UUID id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public EventsDashboardResponse getEventsDashboard() {
        long totalEvents = eventRepository.countAllEvents();
        long upcomingEvents = eventRepository.countUpcomingEvents();
        long totalGuests = eventGuestRepository.countAllGuests();

        var eventSummaries = eventRepository.findEventSummaries();

        return new EventsDashboardResponse(
                totalEvents,
                upcomingEvents,
                totalGuests,
                eventSummaries
        );
    }
}
