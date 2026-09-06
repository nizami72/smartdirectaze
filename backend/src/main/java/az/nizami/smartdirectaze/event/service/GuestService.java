package az.nizami.smartdirectaze.event.service;

import az.nizami.smartdirectaze.event.dto.GuestRequest;
import az.nizami.smartdirectaze.event.dto.GuestResponse;

import java.util.List;
import java.util.UUID;

public interface GuestService {
    GuestResponse createGuest(GuestRequest request);
    GuestResponse addGuestToEvent(UUID eventId, GuestRequest request);
    void linkExistingGuestToEvent(UUID eventId, UUID guestId);
    void removeGuestFromEvent(UUID eventId, UUID guestId);
    GuestResponse getGuest(UUID id);
    List<GuestResponse> getGuestsByEvent(UUID eventId);
    List<GuestResponse> getAllGuests();
    GuestResponse updateGuest(UUID id, GuestRequest request);
    void deleteGuest(UUID id);
}
