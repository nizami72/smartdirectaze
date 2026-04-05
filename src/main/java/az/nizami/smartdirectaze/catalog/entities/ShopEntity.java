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

    @Column(name = "delivery_price")
    private java.math.BigDecimal deliveryPrice;

    @Column(name = "free_delivery_threshold")
    private java.math.BigDecimal freeDeliveryThreshold;

    @Column(name = "zones_text", columnDefinition = "TEXT")
    private String zonesText;

    @Column(name = "regions_delivery_info", columnDefinition = "TEXT")
    private String regionsDeliveryInfo;

    @Column(name = "processing_time_rules", columnDefinition = "TEXT")
    private String processingTimeRules;

    @Column(name = "delivery_working_hours", columnDefinition = "TEXT")
    private String deliveryWorkingHours;

    @Column(name = "collect_phone")
    private Boolean collectPhone;

    @Column(name = "collect_address")
    private Boolean collectAddress;

    @Column(name = "collect_landmark")
    private Boolean collectLandmark;

    @Column(name = "collect_location")
    private Boolean collectLocation;

    @Column(name = "courier_waiting_time")
    private Integer courierWaitingTime;

    @Column(name = "fitting_allowed")
    private Boolean fittingAllowed;

    @Column(name = "refusal_fee")
    private java.math.BigDecimal refusalFee;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}