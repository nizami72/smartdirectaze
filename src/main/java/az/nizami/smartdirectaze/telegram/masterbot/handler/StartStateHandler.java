package az.nizami.smartdirectaze.telegram.masterbot.handler;

import az.nizami.smartdirectaze.catalog.AdminSessionService;
import az.nizami.smartdirectaze.catalog.AdminState;
import az.nizami.smartdirectaze.telegram.masterbot.TelegramApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StartStateHandler implements AdminStateHandler {

    private final TelegramApiClient telegramClient;
    private final AdminSessionService sessionService;
    
    @Value("${telegram.token}")
    private String masterBotToken;

    public StartStateHandler(TelegramApiClient telegramClient, AdminSessionService sessionService) {
        this.telegramClient = telegramClient;
        this.sessionService = sessionService;
    }

    @Override
    public AdminState getHandledState() {
        return AdminState.START;
    }

    @Override
    public void handle(Long chatId, String text) {
        String reply = "Привет! Я Мастер-бот Smart Direct. 🚀\n" +
                "Пришли мне токен твоего бота от @BotFather, и я превращу его в AI-продавца.";
        
        telegramClient.sendMessage(masterBotToken, chatId, reply);
        sessionService.updateState(chatId, AdminState.WAITING_FOR_TOKEN);
    }
}