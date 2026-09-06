package az.nizami.smartdirectaze.event.service;

import az.nizami.smartdirectaze.event.domain.GuestInvitationStatus;
import az.nizami.smartdirectaze.event.domain.Salutation;
import az.nizami.smartdirectaze.event.dto.EventRequest;
import az.nizami.smartdirectaze.event.dto.EventResponse;
import az.nizami.smartdirectaze.event.dto.GuestRequest;
import az.nizami.smartdirectaze.event.dto.GuestResponse;
import az.nizami.smartdirectaze.event.exception.ResourceNotFoundException;
import az.nizami.smartdirectaze.event.repository.EventGuestRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GuestServiceTest {

    @Autowired
    private GuestService guestService;

    @Autowired
    private EventService eventService;

    @Autowired
    private EventGuestRepository eventGuestRepository;

    private UUID eventId;

    @BeforeEach
    void setUp() {
        EventRequest eventRequest = EventRequest.builder()
                .name("Event for Guest Test")
                .dateTime(LocalDateTime.now().plusDays(1))
                .place("Baku")
                .build();
        EventResponse eventResponse = eventService.createEvent(eventRequest);
        eventId = eventResponse.getId();
    }

    @Test
    @Order(6)
    void createGuest_ShouldCreateGuestWithoutEvent() {
        GuestRequest request = createGuestRequest("Diana");

        GuestResponse response = guestService.createGuest(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getFirstName()).isEqualTo("Diana");

        // Ensure that business-level guest is not automatically linked to the event
        List<GuestResponse> eventGuests = guestService.getGuestsByEvent(eventId);
        assertThat(eventGuests).isEmpty();
    }

    private GuestRequest createGuestRequest(String firstName) {
        return GuestRequest.builder()
                .salutation(Salutation.MR)
                .firstName(firstName)
                .lastName("Doe")
                .email(firstName.toLowerCase() + "@example.com")
                .status(GuestInvitationStatus.INVITED)
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("Add Guest - Should add guest to event")
    void addGuestToEvent_ShouldAddGuest() {
        GuestRequest request = createGuestRequest("John");
        GuestResponse response = guestService.addGuestToEvent(eventId, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getFirstName()).isEqualTo("John");
    }

    @Test
    @Order(1)
    @DisplayName("Add Guest - Should create EventGuest association entry")
    void addGuestToEvent_ShouldCreateAssociation() {
        GuestRequest request = createGuestRequest("Assoc");
        guestService.addGuestToEvent(eventId, request);

        // One association must be created for the event
        assertThat(eventGuestRepository.findByEventId(eventId)).hasSize(1);
    }

    @Test
    @Order(2)
    @DisplayName("Get Guest - Should return guest by ID")
    void getGuest_ShouldReturnGuestById() {
        GuestResponse added = guestService.addGuestToEvent(eventId, createGuestRequest("Alice"));
        
        GuestResponse found = guestService.getGuest(added.getId());
        
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(added.getId());
        assertThat(found.getFirstName()).isEqualTo("Alice");
    }

    @Test
    @Order(3)
    @DisplayName("Get Guests By Event - Should return all guests for event")
    void getGuestsByEvent_ShouldReturnGuests() {
        guestService.addGuestToEvent(eventId, createGuestRequest("Guest1"));
        guestService.addGuestToEvent(eventId, createGuestRequest("Guest2"));

        List<GuestResponse> guests = guestService.getGuestsByEvent(eventId);

        assertThat(guests).hasSize(2);
    }

    @Test
    @Order(3)
    @DisplayName("Get Guests By Event - Should throw when event not found")
    void getGuestsByEvent_ShouldThrowWhenEventNotFound() {
        UUID missingEventId = UUID.randomUUID();
        assertThatThrownBy(() -> guestService.getGuestsByEvent(missingEventId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Event not found with id");
    }

    @Test
    @Order(4)
    @DisplayName("Update Guest - Should modify guest details")
    void updateGuest_ShouldModifyGuest() {
        GuestResponse added = guestService.addGuestToEvent(eventId, createGuestRequest("Bob"));
        
        GuestRequest updateRequest = createGuestRequest("Bobby");
        updateRequest.setStatus(GuestInvitationStatus.ACCEPTED);
        
        GuestResponse updated = guestService.updateGuest(added.getId(), updateRequest);
        
        assertThat(updated.getFirstName()).isEqualTo("Bobby");
        assertThat(updated.getStatus()).isEqualTo(GuestInvitationStatus.ACCEPTED);
    }

    @Test
    @Order(5)
    @DisplayName("Delete Guest - Should remove guest")
    void deleteGuest_ShouldRemoveGuest() {
        GuestResponse added = guestService.addGuestToEvent(eventId, createGuestRequest("Charlie"));
        UUID guestId = added.getId();
        
        guestService.deleteGuest(guestId);
        
        assertThatThrownBy(() -> guestService.getGuest(guestId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @Order(7)
    @DisplayName("Add Guest - Should throw when event not found")
    void addGuestToEvent_ShouldThrowWhenEventNotFound() {
        UUID missingEventId = UUID.randomUUID();
        GuestRequest request = createGuestRequest("Zed");

        assertThatThrownBy(() -> guestService.addGuestToEvent(missingEventId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Event not found with id");
    }

    @Test
    @Order(8)
    @DisplayName("Get All Guests - Should return both business-level and event-level guests")
    void getAllGuests_ShouldReturnAllGuests() {
        // One guest created without event assignment
        guestService.createGuest(createGuestRequest("BizGuest"));
        // Two guests assigned to the current event
        guestService.addGuestToEvent(eventId, createGuestRequest("E1"));
        guestService.addGuestToEvent(eventId, createGuestRequest("E2"));

        List<GuestResponse> all = guestService.getAllGuests();
        assertThat(all).hasSizeGreaterThanOrEqualTo(3);
        assertThat(all.stream().map(GuestResponse::getFirstName))
                .contains("BizGuest", "E1", "E2");
    }
}
