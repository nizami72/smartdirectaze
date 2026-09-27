package az.nizami.smartdirectaze.identity;

import java.util.Set;

/**
 * A new user signed up (the operator is told in Telegram).
 */
public record UserRegisteredEvent(Long userId, String email, String name, Set<String> phones) {
}
