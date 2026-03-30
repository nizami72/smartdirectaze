package az.nizami.smartdirectaze.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class UserDto {
    private Long id; // Внутренний ID тенанта
    private Long ownerId; // Telegram ID владельца (для связи и пересылки сложных вопросов)
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
