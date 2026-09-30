package az.nizami.smartdirectaze.whatsapp;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Durable receipt. Never automatically replay a message with uncertain side effects. */
@Entity
@Table(name = "whatsapp_inbox", indexes = @Index(name = "idx_whatsapp_inbox_chat", columnList = "instance_id,chat_id,id"), uniqueConstraints = @UniqueConstraint(
        name = "uc_whatsapp_inbox_message", columnNames = {"instance_id", "message_id"}))
@Getter
@NoArgsConstructor
public class IncomingWhatsappMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "instance_id", nullable = false)
    private String instanceId;
    @Column(name = "message_id", nullable = false)
    private String messageId;
    @Column(name = "chat_id", nullable = false)
    private String chatId;
    private String senderName;
    @Column(columnDefinition = "TEXT")
    private String text;
    private String messageType;
    @Column(nullable = false)
    private String status = "PENDING";

    public IncomingWhatsappMessage(String messageId, WhatsappMessageReceivedEvent event) {
        this.messageId = messageId;
        this.instanceId = event.instanceId();
        this.chatId = event.chatId();
        this.senderName = event.senderName();
        this.text = event.text();
        this.messageType = event.messageType();
    }

    public WhatsappMessageReceivedEvent event() {
        return new WhatsappMessageReceivedEvent(instanceId, chatId, senderName, text, messageType);
    }
}
