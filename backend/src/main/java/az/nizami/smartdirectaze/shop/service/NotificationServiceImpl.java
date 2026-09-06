package az.nizami.smartdirectaze.shop.service;

import az.nizami.smartdirectaze.shop.NotificationService;
import az.nizami.smartdirectaze.shop.OrderDTO;
import az.nizami.smartdirectaze.shop.ProductService;
import az.nizami.smartdirectaze.shop.ShopDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final ProductService productService;

    @Override
    public void sendNewOrderAlertToOwner(Long shopId, OrderDTO order) {
        ShopDto shop = productService.getShopById(shopId);
        if (shop == null || shop.ownerId() == null || shop.telegramBotToken() == null) {
            log.warn("Cannot send notification: Shop {} has missing details", shopId);
            return;
        }

        TelegramClient telegramClient = new OkHttpTelegramClient(shop.telegramBotToken());
        String messageText = formatOrderMessage(order);

        SendMessage sendMessage = SendMessage.builder()
                .chatId(shop.ownerId())
                .text(messageText)
                .parseMode(ParseMode.HTML)
                .build();

        try {
            telegramClient.execute(sendMessage);
            log.info("Notification sent to owner {} for shop {}", shop.ownerId(), shopId);
        } catch (TelegramApiException e) {
            log.error("Failed to send Telegram notification to owner {}: {}", shop.ownerId(), e.getMessage());
        }
    }

    private String formatOrderMessage(OrderDTO order) {
        return String.format(
                "<b>New Order #%d</b>\n\n" +
                "<b>Customer:</b> %s\n" +
                "<b>Phone:</b> %s\n" +
                "<b>Address:</b> %s\n" +
                "<b>Items:</b>\n%s\n\n" +
                "<b>Payment Method:</b> %s",
                order.getId(),
                order.getCustomerName(),
                order.getPhoneNumber(),
                order.getDeliveryAddress(),
                order.getItemsSummary(),
                order.getPaymentMethod()
        );
    }
}
