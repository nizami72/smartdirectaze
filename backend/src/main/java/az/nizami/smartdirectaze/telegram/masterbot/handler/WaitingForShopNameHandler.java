package az.nizami.smartdirectaze.telegram.masterbot.handler;

import az.nizami.smartdirectaze.shop.ProductService;
import az.nizami.smartdirectaze.shop.ShopDto;
import az.nizami.smartdirectaze.telegram.dto.AdminState;
import az.nizami.smartdirectaze.telegram.masterbot.TelegramApiClient;
import az.nizami.smartdirectaze.telegram.masterbot.service.AdminSessionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WaitingForShopNameHandler implements AdminStateHandler {

    private final TelegramApiClient telegramClient;
    private final AdminSessionService sessionService;
    private final String masterBotToken;
    private final String clientWebHookUrl;
    private final String frontendUrl;
    private final ProductService productService;

    public WaitingForShopNameHandler(TelegramApiClient telegramClient,
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

    @Override
    public AdminState getHandledState() {
        return AdminState.WAITING_FOR_SHOP_NAME;
    }

    @Override
    public void handle(Long ownerId, String text) {
        String shopName = text.trim();
        String botToken = sessionService.getTempData(ownerId);

        if (botToken == null) {
            telegramClient.sendMessage(masterBotToken, ownerId, "❌ Произошла ошибка: токен не найден. Начни регистрацию заново с команды /register_new_shop");
            sessionService.reset(ownerId);
            return;
        }

        // Сохраняем новый магазин в базу
        ShopDto newShopDto = ShopDto.builder()
                .ownerId(ownerId)
                .telegramBotToken(botToken)
                .shopName(shopName)
                .isActive(true)
                .build();
        ShopDto shopDto = productService.createShop(newShopDto);

        // MARK: Set up webhook for a new shop, uuid is used to identify the shop
        telegramClient.setWebhook(botToken, clientWebHookUrl + "/" + shopDto.botUuid());

        // Формируем ссылку на ваш React-фронтенд
        String frontendUrlForClient = String.format(frontendUrl, shopDto.id());

        // Отправляем сообщение с кнопкой
        String replyText = String.format("✅ Магазин «%s» успешно создан и бот подключен!\n\n🛍 Теперь давай наполним твою витрину. Нажми на кнопку ниже, чтобы открыть панель управления товарами.", shopName);
        telegramClient.sendWebAppButton(masterBotToken, ownerId, replyText, "Управление товарами 📦", frontendUrlForClient);

        sessionService.updateState(ownerId, AdminState.READY);
    }
}
