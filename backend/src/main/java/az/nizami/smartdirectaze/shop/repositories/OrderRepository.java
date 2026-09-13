package az.nizami.smartdirectaze.shop.repositories;

import az.nizami.smartdirectaze.shop.entities.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    /**
     * Заказы конкретного магазина, свежие сверху.
     */
    List<OrderEntity> findByShopIdOrderByCreatedAtDesc(Long shopId);

    /**
     * Поиск заказа с проверкой принадлежности магазину (изоляция владельца).
     */
    Optional<OrderEntity> findByIdAndShopId(Long id, Long shopId);
}
