package az.nizami.smartdirectaze.telegram.masterbot.handler;

import az.nizami.smartdirectaze.shop.ShopDto;
import az.nizami.smartdirectaze.telegram.dto.AdminState;
import az.nizami.smartdirectaze.shop.ProductService;
import az.nizami.smartdirectaze.telegram.masterbot.TelegramApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    public void handle(Long ownerId, String text) {
        // 1. Находим магазин по ID владельца
        List<ShopDto> shopDtos = productService.findShopsByOwnerId(ownerId);

        if (shopDtos == null || shopDtos.isEmpty()) {
        telegramClient.sendMessage(masterBotToken, ownerId,
            "❌ Магазины не найдены. Напиши /start для новой регистрации.");
        return;
    }
        String replyMessage = "📦 Управление витринами\nВыбери магазин:";
        // Формируем набор кнопок: Название -> URL
        Map<String, String> buttons = new LinkedHashMap<>();
        for (ShopDto shopDto : shopDtos) {
            String url = String.format(frontendUrl, shopDto.id());
            buttons.put(shopDto.shopName() + " ⚙️", url);
        }
        telegramClient.sendWebAppButtons(masterBotToken, ownerId, replyMessage, buttons);
    }
}