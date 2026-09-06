package az.nizami.smartdirectaze.telegram.masterbot.repo;

import az.nizami.smartdirectaze.telegram.masterbot.entity.AdminSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminSessionRepository extends JpaRepository<AdminSessionEntity, Long> {
}