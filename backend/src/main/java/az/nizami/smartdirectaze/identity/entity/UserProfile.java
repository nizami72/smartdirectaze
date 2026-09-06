package az.nizami.smartdirectaze.identity.entity;

import jakarta.persistence.*;


import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@NamedEntityGraph(
        name = "UserProfile.withPhones", // Даем имя графу
        attributeNodes = @NamedAttributeNode("phones") // Указываем, какое поле подгружать
)
public class UserProfile {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id") // referencedColumnName="id" избыточен, MapsId и так знает PK
    private User user;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "locale")
    private String locale;

    @Column(name = "email_subscription")
    private Integer emailSubscription;

    @ElementCollection
    @CollectionTable(name = "user_profile_phones", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "phone")
    @Builder.Default
    private Set<String> phones = new HashSet<>(); // <-- ИСПРАВЛЕНО: Set эффективнее List для ElementCollection

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updated;
}