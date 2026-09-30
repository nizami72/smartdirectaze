package az.nizami.smartdirectaze.whatsapp;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface IncomingWhatsappMessageRepository extends JpaRepository<IncomingWhatsappMessage, Long> {
    boolean existsByInstanceIdAndMessageId(String instanceId, String messageId);

    // One head per chat. FAILED/PROCESSING heads block following messages for manual review.
    @Query("""
        select m from IncomingWhatsappMessage m where m.status = 'PENDING' and not exists
        (select p.id from IncomingWhatsappMessage p where p.instanceId = m.instanceId
         and p.chatId = m.chatId and p.id < m.id and p.status <> 'DONE') order by m.id
        """)
    List<IncomingWhatsappMessage> findReady(Pageable page);

    @Modifying @Transactional
    @Query("update IncomingWhatsappMessage m set m.status = :status where m.id = :id")
    void setStatus(@Param("id") Long id, @Param("status") String status);

    @Modifying @Transactional
    @Query("update IncomingWhatsappMessage m set m.status = 'FAILED' where m.status = 'PROCESSING'")
    int failInterrupted();
}
