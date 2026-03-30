package az.nizami.smartdirectaze.catalog.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "shops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Внутренний ID тенанта

    @Column(name = "owner_id", nullable = false)
    private Long ownerId; // Telegram ID владельца (для связи и пересылки сложных вопросов)

    @JsonIgnore
    @Column(name = "bot_token", nullable = false, unique = true)
    private String botToken; // Токен, который выдал BotFather

    @Column(name = "bot_uuid", nullable = false, unique = true)
    private String botUuid; // Тот самый UUID для Webhook URL

    @Column(name = "knowledge_base", columnDefinition = "TEXT")
    private String knowledgeBase; // Прайс-лист, описание или системный промпт. Используем TEXT, так как строка может быть длинной

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true; // Флаг включения/отключения бота (например, если не оплачена подписка)

    @JsonIgnore
    @Column(name = "admin_access_token", unique = true)
    private String adminAccessToken;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}