package az.nizami.smartdirectaze.shop.repositories;

import az.nizami.smartdirectaze.shop.entities.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<ConversationEntity, Long> {

    Optional<ConversationEntity> findByShopIdAndChatId(Long shopId, String chatId);

    Optional<ConversationEntity> findByIdAndShopId(Long id, Long shopId);

    // Open requests to the seller: AI paused or not (pausedUntil also bounds how long a request is listed)
    List<ConversationEntity> findByShopIdAndHandoffReasonIsNotNullAndPausedUntilAfterOrderByLastCustomerMessageAtDesc(
            Long shopId, LocalDateTime now);
}
