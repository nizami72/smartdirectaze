package az.nizami.smartdirectaze.event.service;

import az.nizami.smartdirectaze.event.domain.Event;
import az.nizami.smartdirectaze.event.domain.Guest;
import az.nizami.smartdirectaze.event.domain.EventGuest;
import az.nizami.smartdirectaze.event.dto.GuestRequest;
import az.nizami.smartdirectaze.event.dto.GuestResponse;
import az.nizami.smartdirectaze.event.exception.ResourceNotFoundException;
import az.nizami.smartdirectaze.event.mapper.EventMapper;
import az.nizami.smartdirectaze.event.repository.EventRepository;
import az.nizami.smartdirectaze.event.repository.GuestRepository;
import az.nizami.smartdirectaze.event.repository.EventGuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuestServiceImpl implements GuestService {

    private final GuestRepository guestRepository;
    private final EventRepository eventRepository;
    private final EventGuestRepository eventGuestRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public GuestResponse createGuest(GuestRequest request) {
        Guest guest = eventMapper.toEntity(request);
        // No event assignment here (business-level guest)
        Guest saved = guestRepository.save(guest);
        return eventMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public GuestResponse addGuestToEvent(UUID eventId, GuestRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        
        Guest guest = eventMapper.toEntity(request);
        Guest savedGuest = guestRepository.save(guest);

        // create association entry between event and guest
        EventGuest eg = EventGuest.builder()
                .event(event)
                .guest(savedGuest)
                .build();
        eventGuestRepository.save(eg);

        return eventMapper.toResponse(savedGuest);
    }

    @Override
    @Transactional
    public void linkExistingGuestToEvent(UUID eventId, UUID guestId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + guestId));

        // Avoid duplicate links thanks to unique constraint; check first to be explicit.
        boolean exists = eventGuestRepository.findByEventIdAndGuestId(eventId, guestId).isPresent();
        if (exists) {
            return; // no-op
        }

        EventGuest eg = EventGuest.builder()
                .event(event)
                .guest(guest)
                .build();
        eventGuestRepository.save(eg);
    }

    @Override
    @Transactional
    public void removeGuestFromEvent(UUID eventId, UUID guestId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }
        if (!guestRepository.existsById(guestId)) {
            throw new ResourceNotFoundException("Guest not found with id: " + guestId);
        }
        eventGuestRepository.deleteByEventIdAndGuestId(eventId, guestId);
    }

    @Override
    @Transactional(readOnly = true)
    public GuestResponse getGuest(UUID id) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + id));
        return eventMapper.toResponse(guest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GuestResponse> getGuestsByEvent(UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }
        return eventGuestRepository.findByEventId(eventId).stream()
                .map(EventGuest::getGuest)
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GuestResponse> getAllGuests() {
        return guestRepository.findAll().stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GuestResponse updateGuest(UUID id, GuestRequest request) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + id));
        
        eventMapper.updateEntity(request, guest);
        Guest updatedGuest = guestRepository.save(guest);
        return eventMapper.toResponse(updatedGuest);
    }

    @Override
    @Transactional
    public void deleteGuest(UUID id) {
        if (!guestRepository.existsById(id)) {
            throw new ResourceNotFoundException("Guest not found with id: " + id);
        }
        guestRepository.deleteById(id);
    }
}
