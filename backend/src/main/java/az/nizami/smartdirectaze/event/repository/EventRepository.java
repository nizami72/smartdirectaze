package az.nizami.smartdirectaze.event.repository;

import az.nizami.smartdirectaze.event.domain.Event;
import az.nizami.smartdirectaze.event.dto.dashboard.EventSummaryDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    @Query("select count(e) from Event e")
    long countAllEvents();

    @Query("select count(e) from Event e where e.dateTime >= current_timestamp")
    long countUpcomingEvents();

    @Query("""
           select new az.nizami.smartdirectaze.event.dto.dashboard.EventSummaryDto(
             e.id,
             e.name,
             e.dateTime,
             e.place,
             count(eg)
           )
           from Event e
           left join e.eventGuests eg
           group by e.id, e.name, e.dateTime, e.place
           order by e.dateTime asc
           """)
    List<EventSummaryDto> findEventSummaries();
}
