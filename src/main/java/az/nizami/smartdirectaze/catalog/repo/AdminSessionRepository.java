package az.nizami.smartdirectaze.catalog.repo;

import az.nizami.smartdirectaze.catalog.entities.AdminSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminSessionRepository extends JpaRepository<AdminSessionEntity, Long> {
}