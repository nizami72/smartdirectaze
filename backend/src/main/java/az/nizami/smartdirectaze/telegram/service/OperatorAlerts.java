package az.nizami.smartdirectaze.telegram.service;

import az.nizami.smartdirectaze.identity.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Operator messages in Telegram about what happens on the platform.
 */
@Component
@RequiredArgsConstructor
class OperatorAlerts {

    private final TelegramService telegramService;

    @Async
    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        String phones = event.phones() == null || event.phones().isEmpty() ? "—" : String.join(", ", event.phones());
        telegramService.notifyAdmin(String.format("🆕 Новая регистрация: %s, имя: %s, телефон: %s",
                event.email(), event.name() != null ? event.name() : "—", phones));
    }
}
