package az.nizami.smartdirectaze.event.repository;

import az.nizami.smartdirectaze.event.domain.Guest;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GuestRepository extends JpaRepository<Guest, UUID> {
    @NotNull List<Guest> findAll();
}
