package az.nizami.smartdirectaze.catalog.repo;

import az.nizami.smartdirectaze.catalog.entities.ShopEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<ShopEntity, Long> {
    // Этот метод будет использоваться контроллером покупателей для поиска настроек бота по URL
    Optional<ShopEntity> findByBotUuid(String botUuid);

    Optional<ShopEntity> findById(Long shopId);

    Optional<ShopEntity> findByBotUuidAndBotToken(String botUuid, String botToken);

    // А этот метод пригодится, чтобы обновлять прайс-лист по запросу владельца
    Optional<ShopEntity> findByOwnerId(Long ownerChatId);

    Optional<ShopEntity> findByAdminAccessToken(String adminAccessToken);
}