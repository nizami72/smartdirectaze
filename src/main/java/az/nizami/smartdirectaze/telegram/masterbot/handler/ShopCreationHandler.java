package az.nizami.smartdirectaze.telegram.masterbot.handler;

import az.nizami.smartdirectaze.catalog.ProductService;
import az.nizami.smartdirectaze.catalog.ShopDto;
import az.nizami.smartdirectaze.telegram.dto.AdminState;
import az.nizami.smartdirectaze.telegram.masterbot.TelegramApiClient;
import az.nizami.smartdirectaze.telegram.masterbot.service.AdminSessionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ShopCreationHandler implements AdminStateHandler {

    //<editor-fold desc="Fields">
    private final TelegramApiClient telegramClient;
    private final AdminSessionService sessionService;
    private final String masterBotToken;
    private final String frontendUrl;
    private final ProductService productService;
    //</editor-fold>

    //<editor-fold desc="Constructor">
    public ShopCreationHandler(TelegramApiClient telegramClient,
                               AdminSessionService sessionService,
                               @Value("${telegram.token}") String masterBotToken,
                               @Value("${app.url.frontend}") String frontendUrl,
                               ProductService productService) {
        this.telegramClient = telegramClient;
        this.sessionService = sessionService;
        this.masterBotToken = masterBotToken;
        this.frontendUrl = frontendUrl;
        this.productService = productService;
    }
    //</editor-fold>

    @Override
    public AdminState getHandledState() {
        return AdminState.WAITING_FOR_SHOP_CREATION;
    }

    @Override
    public void handle(Long chatId, String text) {
        String secret = text.trim();

        if (!checkSecretValid(secret)) {
            telegramClient.sendMessage(masterBotToken, chatId, "❌ The secret is not valid, get info form admin.");
            return;
        }

        // Сохраняем новый магазин в базу с помощью Builder
        ShopDto newShopEntity = ShopDto.builder()
                .ownerId(chatId)
                .botToken(secret)
                .isActive(true)
                .build();
        ShopDto shopDto = productService.createShop(newShopEntity);

        //  Формируем ссылку на ваш React-фронтенд
        String frontendUrlForClient = String.format(frontendUrl, shopDto.id());

        // Отправляем сообщение с кнопкой
        String replyText = "✅ New shop created!\n\n🛍 Теперь давай наполним твою витрину. Нажми на кнопку ниже, чтобы открыть панель управления товарами.";
        telegramClient.sendWebAppButton(masterBotToken, chatId, replyText, "Управление товарами 📦", frontendUrlForClient);

        sessionService.updateState(chatId, AdminState.READY);
    }

    private boolean checkSecretValid(String secret) {
//        return telegramClient.isTokenValid(secret);
        return true; // todo think if validation really needed
    }
}