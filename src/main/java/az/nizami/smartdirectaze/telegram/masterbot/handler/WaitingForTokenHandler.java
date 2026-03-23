package az.nizami.smartdirectaze.telegram.masterbot.handler;

import az.nizami.smartdirectaze.ShopDto;
import az.nizami.smartdirectaze.catalog.AdminState;
import az.nizami.smartdirectaze.catalog.ProductService;
import az.nizami.smartdirectaze.catalog.AdminSessionService;
import az.nizami.smartdirectaze.telegram.masterbot.TelegramApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WaitingForTokenHandler implements AdminStateHandler {

    //<editor-fold desc="Fields">
    private final TelegramApiClient telegramClient;
    private final AdminSessionService sessionService;
    private final String masterBotToken;
    private final String baseUrl;
    private final String frontendUrl;
    private final ProductService productService;
    //</editor-fold>

    //<editor-fold desc="Constructor">
    public WaitingForTokenHandler(TelegramApiClient telegramClient,
                                  AdminSessionService sessionService,
                                  @Value("${telegram.token}") String masterBotToken,
                                  @Value("${telegram.webhook.base-url}") String baseUrl,
                                  @Value("${app.url.frontend}") String frontendUrl,
                                   ProductService productService) {
        this.telegramClient = telegramClient;
        this.sessionService = sessionService;
        this.masterBotToken = masterBotToken;
        this.baseUrl = baseUrl;
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

        // TODO: Сохранить магазин в БД (Shop entity)
        // Сохраняем новый магазин в базу с помощью Builder
        ShopDto newShopEntity = ShopDto.builder()
                .ownerChatId(chatId)
                .botToken(newBotToken)
                .isActive(true)
                .build();
        String botUuid = productService.createShop(newShopEntity);

        // Устанавливаем вебхук для НОВОГО бота клиента
        String clientWebhookUrl = baseUrl + "/api/webhooks/clients/" + botUuid;
        telegramClient.setWebhook(newBotToken, clientWebhookUrl);

        //  Формируем ссылку на ваш React-фронтенд
        String frontendUrlForClient = String.format(frontendUrl, botUuid);

        // Отправляем сообщение с кнопкой
        String replyText = "✅ Бот успешно подключен!\n\n🛍 Теперь давай наполним твою витрину. Нажми на кнопку ниже, чтобы открыть панель управления товарами.";
        telegramClient.sendWebAppButton(masterBotToken, chatId, replyText, "Управление товарами 📦", frontendUrlForClient);

        sessionService.updateState(chatId, AdminState.WAITING_FOR_INFO);
    }
}