package az.nizami.smartdirectaze.shop.repositories;

import az.nizami.smartdirectaze.shop.entities.ShopEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopRepository extends JpaRepository<ShopEntity, Long> {

    /**
     * Поиск всех магазинов конкретного владельца.
     */
    List<ShopEntity> findAllByOwnerId(Long ownerId);

    // Этот метод будет использоваться контроллером покупателей для поиска настроек бота по URL
    Optional<ShopEntity> findByBotUuid(String botUuid);

    Optional<ShopEntity> findById(Long shopId);

    @Query(value = "SELECT DISTINCT id FROM shops WHERE owner_id = :ownerId", nativeQuery = true)
    List<Long> findShopIdsByOwnerId(Long ownerId);

    @Query(value = "SELECT * FROM shops WHERE owner_id = :ownerId", nativeQuery = true)
    List<ShopEntity> findShopsByOwnerId(Long ownerId);

    // А этот метод пригодится, чтобы обновлять прайс-лист по запросу владельца
    Optional<ShopEntity> findByOwnerId(Long ownerId);

    /*
     SQL DDL для Liquibase/Flyway миграции:

     CREATE INDEX idx_shops_owner_id ON shops(owner_id);
     CREATE INDEX idx_shops_whatsapp_instance_id ON shops(whatsapp_instance_id);
     CREATE INDEX idx_shops_telegram_bot_username ON shops(telegram_bot_username);
     CREATE INDEX idx_shops_instagram_page_id ON shops(instagram_page_id);
    */
}
