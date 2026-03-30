package az.nizami.smartdirectaze.user.repo;

import az.nizami.smartdirectaze.catalog.entities.ShopEntity;
import az.nizami.smartdirectaze.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByOwnerId(Long ownerId);

}