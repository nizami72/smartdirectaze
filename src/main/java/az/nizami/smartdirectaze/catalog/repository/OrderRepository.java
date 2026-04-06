package az.nizami.smartdirectaze.catalog.repository;

import az.nizami.smartdirectaze.catalog.entities.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
}
