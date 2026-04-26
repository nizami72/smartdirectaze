package az.nizami.smartdirectaze.catalog.repo;

import az.nizami.smartdirectaze.catalog.ShopDto;
import az.nizami.smartdirectaze.catalog.entities.ShopEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<ShopEntity, Long> {
    // Этот метод будет использоваться контроллером покупателей для поиска настроек бота по URL
    Optional<ShopEntity> findByBotUuid(String botUuid);

    Optional<ShopEntity> findById(Long shopId);

    @Query(value = "SELECT DISTINCT id FROM shops WHERE owner_id = :ownerId", nativeQuery = true)
    List<Long> findShopIdsByOwnerId(Long ownerId);

    @Query(value = "SELECT * FROM shops WHERE owner_id = :ownerId", nativeQuery = true)
    List<ShopEntity> findShopsByOwnerId(Long ownerId);

    Optional<ShopEntity> findByBotUuidAndBotToken(String botUuid, String botToken);

    // А этот метод пригодится, чтобы обновлять прайс-лист по запросу владельца
    Optional<ShopEntity> findByOwnerId(Long ownerChatId);

    Optional<ShopEntity> findByAdminAccessToken(String adminAccessToken);
}