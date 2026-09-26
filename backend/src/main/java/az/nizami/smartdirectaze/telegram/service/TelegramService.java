package az.nizami.smartdirectaze.telegram.service;

public interface TelegramService {
    String getBotUsername(String token);

    /**
     * Message to the platform operator (SD_TELEGRAM_ADMIN_ID) through the master bot.
     */
    void notifyAdmin(String text);
}
