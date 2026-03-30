package az.nizami.smartdirectaze.telegram.masterbot.handler;

import az.nizami.smartdirectaze.catalog.ShopDto;
import az.nizami.smartdirectaze.telegram.dto.AdminState;
import az.nizami.smartdirectaze.catalog.ProductService;
import az.nizami.smartdirectaze.telegram.masterbot.TelegramApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ReadyStateHandler implements AdminStateHandler {

    private final TelegramApiClient telegramClient;
    private final ProductService productService;
    private final String frontendUrl;

    @Value("${telegram.token}")
    private String masterBotToken;

    public ReadyStateHandler(
            TelegramApiClient telegramClient,
            @Value("${app.url.frontend}") String frontendUrl,
            ProductService productService) {
        this.telegramClient = telegramClient;
        this.frontendUrl = frontendUrl;
        this.productService = productService;
    }

    @Override
    public AdminState getHandledState() {
        return AdminState.READY;
    }

    @Override
    public void handle(Long chatId, String text) {
        // 1. Находим магазин по ID владельца
        Optional<ShopDto> shopOpt = productService.findByOwnerId(chatId);

        if (shopOpt.isEmpty()) {
            telegramClient.sendMessage(masterBotToken, chatId, "❌ Магазин не найден. Напиши /start для новой регистрации.");
            // Здесь можно сбросить стейт в START
            return;
        }

        // 2. Формируем безопасную ссылку (как мы делали в предыдущем шаге)
        String frontendUrlForClient = String.format(frontendUrl, shopOpt.get().id());

        // 3. Отправляем сообщение с Inline-кнопкой Web App
        String replyMessage = "📦 Управление витриной\n\nЗдесь ты можешь добавлять товары, менять цены и указывать наличие.";
        telegramClient.sendWebAppButton(masterBotToken, chatId, replyMessage, "Открыть панель ⚙️", frontendUrlForClient);
    }
}