package az.nizami.smartdirectaze.shop.repositories;

import az.nizami.smartdirectaze.shop.entities.ConversationEntity;
import az.nizami.smartdirectaze.shop.entities.ConversationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<ConversationEntity, Long> {

    Optional<ConversationEntity> findByShopIdAndChatId(Long shopId, String chatId);

    Optional<ConversationEntity> findByIdAndShopId(Long id, Long shopId);

    List<ConversationEntity> findByShopIdAndStatusAndPausedUntilAfterOrderByLastCustomerMessageAtDesc(
            Long shopId, ConversationStatus status, LocalDateTime now);
}
