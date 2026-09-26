package az.nizami.smartdirectaze.telegram.service;

import az.nizami.smartdirectaze.telegram.masterbot.TelegramApiClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class TelegramServiceImpl implements TelegramService {

    private final TelegramApiClient telegramApiClient;
    private final String masterBotToken;
    private final Long adminId;

    public TelegramServiceImpl(TelegramApiClient telegramApiClient,
                               @Value("${telegram.token}") String masterBotToken,
                               @Value("${telegram.bot.admin.id}") Long adminId) {
        this.telegramApiClient = telegramApiClient;
        this.masterBotToken = masterBotToken;
        this.adminId = adminId;
    }

    @Override
    public void notifyAdmin(String text) {
        try {
            telegramApiClient.sendMessage(masterBotToken, adminId, text);
        } catch (Exception e) {
            log.error("Failed to notify the admin in Telegram: {}", e.getMessage());
        }
    }

    @Override
    public String getBotUsername(String token) {
        return telegramApiClient.getBotUsername(token);
    }
}
