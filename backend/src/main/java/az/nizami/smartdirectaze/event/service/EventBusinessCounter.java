package az.nizami.smartdirectaze.event.service;

import az.nizami.smartdirectaze.business.BusinessChildCounter;
import az.nizami.smartdirectaze.business.Industry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
class EventBusinessCounter implements BusinessChildCounter {

    private final EventService eventService;

    @Override
    public Industry industry() {
        return Industry.EVENTS;
    }

    @Override
    public long countByBusiness(UUID businessId) {
        return eventService.countByBusiness(businessId);
    }
}
