package az.nizami.smartdirectaze.event.controller;

import az.nizami.smartdirectaze.event.dto.GuestRequest;
import az.nizami.smartdirectaze.event.dto.GuestResponse;
import az.nizami.smartdirectaze.event.service.GuestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;

    @PostMapping("/guests")
    @ResponseStatus(HttpStatus.CREATED)
    public GuestResponse createGuest(@Valid @RequestBody GuestRequest request) {
        return guestService.createGuest(request);
    }

    @PostMapping("/events/{eventId}/guests")
    @ResponseStatus(HttpStatus.CREATED)
    public GuestResponse addGuestToEvent(@PathVariable UUID eventId, @Valid @RequestBody GuestRequest request) {
        return guestService.addGuestToEvent(eventId, request);
    }

    // Link an existing guest to the event
    @PostMapping("/events/{eventId}/guests/{guestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void linkExistingGuestToEvent(@PathVariable UUID eventId, @PathVariable UUID guestId) {
        guestService.linkExistingGuestToEvent(eventId, guestId);
    }

    // Remove guest from the event (only the relationship, not deleting the guest)
    @DeleteMapping("/events/{eventId}/guests/{guestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeGuestFromEvent(@PathVariable UUID eventId, @PathVariable UUID guestId) {
        guestService.removeGuestFromEvent(eventId, guestId);
    }

    @GetMapping("/events/{eventId}/guests")
    public List<GuestResponse> getGuestsByEvent(@PathVariable UUID eventId) {
        return guestService.getGuestsByEvent(eventId);
    }

    @GetMapping("/guests")
    public List<GuestResponse> getAllGuests() {
        return guestService.getAllGuests();
    }

    @GetMapping("/guests/{id}")
    public GuestResponse getGuest(@PathVariable UUID id) {
        return guestService.getGuest(id);
    }

    @PutMapping("/guests/{id}")
    public GuestResponse updateGuest(@PathVariable UUID id, @Valid @RequestBody GuestRequest request) {
        return guestService.updateGuest(id, request);
    }

    @DeleteMapping("/guests/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGuest(@PathVariable UUID id) {
        guestService.deleteGuest(id);
    }
}
