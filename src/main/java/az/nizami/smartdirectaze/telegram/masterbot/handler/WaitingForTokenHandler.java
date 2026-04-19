package az.nizami.smartdirectaze.telegram.masterbot.handler;

import az.nizami.smartdirectaze.catalog.ShopDto;
import az.nizami.smartdirectaze.telegram.dto.AdminState;
import az.nizami.smartdirectaze.catalog.ProductService;
import az.nizami.smartdirectaze.telegram.masterbot.service.AdminSessionService;
import az.nizami.smartdirectaze.telegram.masterbot.TelegramApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WaitingForTokenHandler implements AdminStateHandler {

    //<editor-fold desc="Fields">
    private final TelegramApiClient telegramClient;
    private final AdminSessionService sessionService;
    private final String masterBotToken;
    private final String clientWebHookUrl;
    private final String frontendUrl;
    private final ProductService productService;
    //</editor-fold>

    //<editor-fold desc="Constructor">
    public WaitingForTokenHandler(TelegramApiClient telegramClient,
                                  AdminSessionService sessionService,
                                  @Value("${telegram.token}") String masterBotToken,
                                  @Value("${app.url.public}") String clientWebHookUrl,
                                  @Value("${app.url.frontend}") String frontendUrl,
                                  ProductService productService) {
        this.telegramClient = telegramClient;
        this.sessionService = sessionService;
        this.masterBotToken = masterBotToken;
        this.clientWebHookUrl = clientWebHookUrl;
        this.frontendUrl = frontendUrl;
        this.productService = productService;
    }
    //</editor-fold>

    @Override
    public AdminState getHandledState() {
        return AdminState.WAITING_FOR_TOKEN;
    }

    @Override
    public void handle(Long chatId, String text) {
        String newBotToken = text.trim();

        if (!telegramClient.isTokenValid(newBotToken)) {
            telegramClient.sendMessage(masterBotToken, chatId, "❌ Неверный токен. Проверь и отправь еще раз.");
            return;
        }

        // Сохраняем токен в сессии и запрашиваем название магазина
        sessionService.updateTempData(chatId, newBotToken);
        sessionService.updateState(chatId, AdminState.WAITING_FOR_SHOP_NAME);

        telegramClient.sendMessage(masterBotToken, chatId, "✅ Токен принят! Теперь введи название твоего магазина.");
    }
}