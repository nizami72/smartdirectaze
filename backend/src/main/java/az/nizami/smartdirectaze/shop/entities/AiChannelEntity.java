package az.nizami.smartdirectaze.shop.entities;

import az.nizami.smartdirectaze.shop.AiMode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ai_channels", indexes = {
        @Index(name = "idx_ai_channel_external_id", columnList = "instance_external_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiChannelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false)
    private ChannelType channelType;

    @Column(name = "wid")
    private String wid;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_status", nullable = false)
    private ChannelStatus channelStatus;

    @Column(name = "api_token", columnDefinition = "TEXT")
    private String apiToken;

    @Column(name = "instance_external_id", unique = true)
    private String instanceExternalId;

    // New channels start in TEST: the AI must not answer real customers before the merchant checked it
    @Enumerated(EnumType.STRING)
    @Column(name = "ai_mode", nullable = false, length = 16)
    @Builder.Default
    private AiMode aiMode = AiMode.TEST;

    // Where order alerts go, digits only; null = the merchant's own WhatsApp ("message yourself", no push)
    @Column(name = "notification_phone", length = 32)
    private String notificationPhone;

    // Digits only, e.g. "994551112233"
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ai_channel_test_phones", joinColumns = @JoinColumn(name = "ai_channel_id"))
    @Column(name = "phone", nullable = false)
    @Builder.Default
    private Set<String> testPhones = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    @ToString.Exclude
    private ShopEntity shop;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
