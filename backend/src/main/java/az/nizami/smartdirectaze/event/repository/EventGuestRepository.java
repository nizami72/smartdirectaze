package az.nizami.smartdirectaze.event.repository;

import az.nizami.smartdirectaze.event.domain.EventGuest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventGuestRepository extends JpaRepository<EventGuest, UUID> {
    List<EventGuest> findByEventId(UUID eventId);
    java.util.Optional<EventGuest> findByEventIdAndGuestId(UUID eventId, UUID guestId);
    void deleteByEventIdAndGuestId(UUID eventId, UUID guestId);

    @Query("select count(eg) from EventGuest eg")
    long countAllGuests();
}
