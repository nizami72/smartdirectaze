package az.nizami.smartdirectaze.shop.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

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
