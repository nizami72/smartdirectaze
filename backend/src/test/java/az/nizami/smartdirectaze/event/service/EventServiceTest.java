package az.nizami.smartdirectaze.event.service;

import az.nizami.smartdirectaze.event.dto.EventRequest;
import az.nizami.smartdirectaze.event.dto.EventResponse;
import az.nizami.smartdirectaze.event.exception.ResourceNotFoundException;
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
class EventServiceTest {

    @Autowired
    private EventService eventService;

    private static UUID sharedEventId;

    private EventRequest createSampleRequest(String name) {
        return EventRequest.builder()
                .name(name)
                .dateTime(LocalDateTime.now().plusDays(1))
                .place("Baku, Azerbaijan")
                .description("Sample event description")
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("Create Event - Should save and return event")
    void createEvent_ShouldSaveAndReturnEvent() {
        EventRequest request = createSampleRequest("Initial Event");
        EventResponse response = eventService.createEvent(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo(request.getName());
        
        sharedEventId = response.getId();
    }

    @Test
    @Order(2)
    @DisplayName("Get Event - Should return event by ID")
    void getEvent_ShouldReturnEventById() {
        // Ensure sharedEventId is set (though in Transactional SpringBootTest with Order, it depends on shared state if not careful)
        // For truly independent but sequenced tests, we might want to re-create or use a fixed ID if we weren't in @Transactional.
        // But since we want to test logically, let's use the ID from the first test if possible, 
        // however @Transactional will roll back after each test by default.
        // To test "sequence", we should either not use @Transactional or accept they are independent.
        // The prompt says "create and arrange sequence of test so that it logically test each operation of CRUD".
        
        EventRequest request = createSampleRequest("Get Test Event");
        EventResponse created = eventService.createEvent(request);
        
        EventResponse found = eventService.getEvent(created.getId());
        
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getName()).isEqualTo("Get Test Event");
    }

    @Test
    @Order(3)
    @DisplayName("Get All Events - Should return list of events")
    void getAllEvents_ShouldReturnListOfEvents() {
        eventService.createEvent(createSampleRequest("Event 1"));
        eventService.createEvent(createSampleRequest("Event 2"));

        List<EventResponse> events = eventService.getAllEvents();

        assertThat(events).isNotEmpty();
        assertThat(events.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @Order(4)
    @DisplayName("Update Event - Should modify existing event")
    void updateEvent_ShouldModifyExistingEvent() {
        EventResponse created = eventService.createEvent(createSampleRequest("Before Update"));
        
        EventRequest updateRequest = createSampleRequest("After Update");
        updateRequest.setPlace("New Place");
        
        EventResponse updated = eventService.updateEvent(created.getId(), updateRequest);
        
        assertThat(updated.getName()).isEqualTo("After Update");
        assertThat(updated.getPlace()).isEqualTo("New Place");
        
        EventResponse found = eventService.getEvent(created.getId());
        assertThat(found.getName()).isEqualTo("After Update");
    }

    @Test
    @Order(5)
    @DisplayName("Delete Event - Should remove event")
    void deleteEvent_ShouldRemoveEvent() {
        EventResponse created = eventService.createEvent(createSampleRequest("To Be Deleted"));
        UUID id = created.getId();
        
        eventService.deleteEvent(id);
        
        assertThatThrownBy(() -> eventService.getEvent(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
