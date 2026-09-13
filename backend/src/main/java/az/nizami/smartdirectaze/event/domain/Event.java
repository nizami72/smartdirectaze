package az.nizami.smartdirectaze.event.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Business-агрегат (модуль business), к которому принадлежит ивент. Связь по ID.
    private UUID businessId;

    private String name;
    private LocalDateTime dateTime;
    private String place;
    private String description;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EventGuest> eventGuests = new ArrayList<>();
}
