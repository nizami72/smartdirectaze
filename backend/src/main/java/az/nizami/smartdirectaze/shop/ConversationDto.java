package az.nizami.smartdirectaze.shop;

import java.time.LocalDateTime;

/**
 * A chat waiting for the seller.
 *
 * @param customerPhone digits only, for tel: and wa.me links
 */
public record ConversationDto(Long id, String customerPhone, String customerName, String reason,
                              String lastMessage, LocalDateTime lastMessageAt, LocalDateTime pausedUntil) {
}
