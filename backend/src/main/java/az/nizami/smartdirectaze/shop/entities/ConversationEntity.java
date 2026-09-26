package az.nizami.smartdirectaze.shop.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * A customer's chat with a shop (WhatsApp): who answers it now, the AI or the seller.
 */
@Entity
@Table(name = "conversations", uniqueConstraints = {
        @UniqueConstraint(name = "uc_conversation_shop_chat", columnNames = {"shop_id", "chat_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    // WhatsApp chat id, e.g. "994551112233@c.us"
    @Column(name = "chat_id", nullable = false)
    private String chatId;

    @Column(name = "customer_name")
    private String customerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private ConversationStatus status = ConversationStatus.AI;

    // HUMAN only: after this moment the AI answers again
    @Column(name = "paused_until")
    private LocalDateTime pausedUntil;

    @Column(name = "handoff_reason")
    private String handoffReason;

    @Column(name = "last_customer_message", columnDefinition = "TEXT")
    private String lastCustomerMessage;

    @Column(name = "last_customer_message_at")
    private LocalDateTime lastCustomerMessageAt;

    // Throttles "the seller is needed" alerts for one chat
    @Column(name = "last_alert_at")
    private LocalDateTime lastAlertAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
