package az.nizami.smartdirectaze.identity.repo;

import az.nizami.smartdirectaze.identity.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"profile", "profile.phones", "roles"})
    Optional<User> findById(Long id);

    @EntityGraph(attributePaths = {"profile", "profile.phones", "roles"})
    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = {"profile", "profile.phones", "roles"})
    List<User> findAll();

    boolean existsByEmail(String email);
}