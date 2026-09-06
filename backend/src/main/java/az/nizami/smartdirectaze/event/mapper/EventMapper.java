package az.nizami.smartdirectaze.event.mapper;

import az.nizami.smartdirectaze.event.domain.Event;
import az.nizami.smartdirectaze.event.domain.Guest;
import az.nizami.smartdirectaze.event.dto.EventRequest;
import az.nizami.smartdirectaze.event.dto.EventResponse;
import az.nizami.smartdirectaze.event.dto.GuestRequest;
import az.nizami.smartdirectaze.event.dto.GuestResponse;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public Event toEntity(EventRequest request) {
        if (request == null) return null;
        return Event.builder()
                .name(request.getName())
                .dateTime(request.getDateTime())
                .place(request.getPlace())
                .description(request.getDescription())
                .build();
    }

    public EventResponse toResponse(Event event) {
        if (event == null) return null;
        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .dateTime(event.getDateTime())
                .place(event.getPlace())
                .description(event.getDescription())
                .build();
    }

    public void updateEntity(EventRequest request, Event event) {
        if (request == null || event == null) return;
        event.setName(request.getName());
        event.setDateTime(request.getDateTime());
        event.setPlace(request.getPlace());
        event.setDescription(request.getDescription());
    }

    public Guest toEntity(GuestRequest request) {
        if (request == null) return null;
        return Guest.builder()
                .salutation(request.getSalutation())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .whatsapp(request.getWhatsapp())
                .email(request.getEmail())
                .language(request.getLanguage())
                .status(request.getStatus())
                .build();
    }

    public GuestResponse toResponse(Guest guest) {
        if (guest == null) return null;
        return GuestResponse.builder()
                .id(guest.getId())
                .salutation(guest.getSalutation())
                .firstName(guest.getFirstName())
                .lastName(guest.getLastName())
                .phone(guest.getPhone())
                .whatsapp(guest.getWhatsapp())
                .email(guest.getEmail())
                .language(guest.getLanguage())
                .status(guest.getStatus())
                .build();
    }

    public void updateEntity(GuestRequest request, Guest guest) {
        if (request == null || guest == null) return;
        guest.setSalutation(request.getSalutation());
        guest.setFirstName(request.getFirstName());
        guest.setLastName(request.getLastName());
        guest.setPhone(request.getPhone());
        guest.setWhatsapp(request.getWhatsapp());
        guest.setEmail(request.getEmail());
        guest.setLanguage(request.getLanguage());
        guest.setStatus(request.getStatus());
    }
}
