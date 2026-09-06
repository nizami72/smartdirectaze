package az.nizami.smartdirectaze.shop.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
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
    private Long id; // Внутренний ID магазина (тенанта)

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "shop_name", nullable = false)
    private String shopName;

    @Column(name = "bot_uuid", unique = true)
    private String botUuid;

    @Column(name = "knowledge_base", columnDefinition = "TEXT")
    private String knowledgeBase; // Системный промпт / Базовые знания для ИИ

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // --- ЛОГИСТИКА И ДОСТАВКА В БАКУ ---
    @Column(name = "address", columnDefinition = "TEXT")
    private String address; // Физический адрес (например, Низами 115, Landmark)

    @Column(name = "working_hours", columnDefinition = "TEXT")
    private String workingHours; // Часы работы магазина (10:00 - 20:00)

    @Column(name = "delivery_price")
    private BigDecimal deliveryPrice;

    @Column(name = "free_delivery_threshold")
    private BigDecimal freeDeliveryThreshold;

    @Column(name = "zones_text", columnDefinition = "TEXT")
    private String zonesText;

    @Column(name = "regions_delivery_info", columnDefinition = "TEXT")
    private String regionsDeliveryInfo;

    @Column(name = "processing_time_rules", columnDefinition = "TEXT")
    private String processingTimeRules;

    @Column(name = "delivery_working_hours", columnDefinition = "TEXT")
    private String deliveryWorkingHours;

    @Column(name = "payment_methods", columnDefinition = "TEXT")
    private String paymentMethodsJson; // Настройки оплаты (m10, карты, нал)

    // --- НАСТРОЙКИ СБОРА ДАННЫХ ИИ ---
    @Column(name = "collect_phone")
    private Boolean collectPhone;

    @Column(name = "collect_address")
    private Boolean collectAddress;

    @Column(name = "collect_landmark")
    private Boolean collectLandmark;

    @Column(name = "collect_location")
    private Boolean collectLocation;

    // --- ПРАВИЛА ПРИМЕРКИ И ВОЗВРАТА (АКТУАЛЬНО ДЛЯ ШОУРУМОВ) ---
    @Column(name = "courier_waiting_time")
    private Integer courierWaitingTime;

    @Column(name = "fitting_allowed")
    private Boolean fittingAllowed;

    @Column(name = "refusal_fee")
    private BigDecimal refusalFee;

    @Column(name = "trying_returns_policy", columnDefinition = "TEXT")
    private String tryingReturnsPolicy;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<AiChannelEntity> channels = new ArrayList<>();

    // --- СИСТЕМНЫЕ ТАЙМСТЕМПЫ ---
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
