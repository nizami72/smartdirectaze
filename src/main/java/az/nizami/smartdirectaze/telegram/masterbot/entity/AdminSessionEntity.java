package az.nizami.smartdirectaze.telegram.masterbot.entity;

import az.nizami.smartdirectaze.telegram.dto.AdminState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminSessionEntity {

    @Id
    @Column(name = "chat_id")
    private Long chatId; // Telegram ID владельца

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private AdminState state; // Наш Enum (START, WAITING_FOR_TOKEN, и т.д.)

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Полезно, чтобы удалять "зависшие" сессии через cron job
}