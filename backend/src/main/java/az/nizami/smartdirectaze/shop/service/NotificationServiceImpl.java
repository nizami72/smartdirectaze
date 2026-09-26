package az.nizami.smartdirectaze.shop.service;

import az.nizami.smartdirectaze.shop.NotificationService;
import az.nizami.smartdirectaze.shop.OrderDTO;
import az.nizami.smartdirectaze.shop.ProductService;
import az.nizami.smartdirectaze.shop.ShopDto;
import az.nizami.smartdirectaze.shop.entities.AiChannelEntity;
import az.nizami.smartdirectaze.shop.entities.ChannelType;
import az.nizami.smartdirectaze.shop.repositories.AiChannelRepository;
import az.nizami.smartdirectaze.whatsapp.service.WhatsappService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Optional;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final ProductService productService;
    private final AiChannelRepository aiChannelRepository;
    private final WhatsappService whatsappService;
    private final String frontendBaseUrl;

    public NotificationServiceImpl(ProductService productService,
                                   AiChannelRepository aiChannelRepository,
                                   WhatsappService whatsappService,
                                   @Value("${app.frontend.base-url}") String frontendBaseUrl) {
        this.productService = productService;
        this.aiChannelRepository = aiChannelRepository;
        this.whatsappService = whatsappService;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    /**
     * Shops with a WhatsApp channel get the alert in WhatsApp; Telegram-bot shops (master bot flow) in Telegram.
     */
    @Override
    public void sendNewOrderAlertToOwner(Long shopId, OrderDTO order) {
        Optional<AiChannelEntity> whatsapp = aiChannelRepository.findByShopIdAndChannelType(shopId, ChannelType.WHATSAPP)
                .filter(channel -> channel.getInstanceExternalId() != null && channel.getApiToken() != null);
        if (whatsapp.isPresent()) {
            sendViaWhatsapp(whatsapp.get(), order);
        } else {
            sendViaTelegram(shopId, order);
        }
    }

    @Override
    public void sendHumanHelpAlert(Long shopId, String customerChatId, String customerName, String reason, String lastMessage) {
        aiChannelRepository.findByShopIdAndChannelType(shopId, ChannelType.WHATSAPP)
                .filter(channel -> channel.getInstanceExternalId() != null && channel.getApiToken() != null)
                .ifPresentOrElse(
                        channel -> sendToMerchant(channel, formatHumanHelpMessage(shopId, customerChatId, customerName, reason, lastMessage),
                                "help alert for chat " + customerChatId),
                        () -> log.warn("Help alert for shop {} not sent: no WhatsApp channel", shopId));
    }

    private void sendViaWhatsapp(AiChannelEntity channel, OrderDTO order) {
        sendToMerchant(channel, formatWhatsappMessage(order), "order #" + order.getId() + " alert");
    }

    private void sendToMerchant(AiChannelEntity channel, String text, String what) {
        String instanceId = channel.getInstanceExternalId();
        String token = channel.getApiToken();
        try {
            // No notification phone: the merchant's own number ("message yourself" chat)
            String target = channel.getNotificationPhone() != null
                    ? channel.getNotificationPhone()
                    : whatsappService.getSettings(instanceId, token);
            if (target == null || target.isBlank()) {
                log.warn("{} not sent: WhatsApp instance {} is not connected", what, instanceId);
                return;
            }
            String chatId = target.contains("@") ? target : target + "@c.us";
            whatsappService.sendMessage(instanceId, token, chatId, text);
            log.info("{} sent to the merchant's WhatsApp", what);
        } catch (Exception e) {
            log.error("Failed to send {} to the merchant's WhatsApp: {}", what, e.getMessage());
        }
    }

    String formatHumanHelpMessage(Long shopId, String customerChatId, String customerName, String reason, String lastMessage) {
        String phone = customerChatId.replaceAll("@.*$", "");
        return String.format(
                "🙋 *Нужна ваша помощь*\n\n" +
                "*Клиент:* +%s%s\n" +
                "*Причина:* %s\n" +
                "*Последнее сообщение:* %s\n\n" +
                "AI в этом чате молчит, пока вы не ответите.\n" +
                "Написать клиенту: https://wa.me/%s\n" +
                "Все такие чаты: %s/shops/%d",
                phone,
                customerName != null && !customerName.isBlank() ? " (" + customerName + ")" : "",
                reason,
                lastMessage != null ? "«" + lastMessage + "»" : "—",
                phone,
                frontendBaseUrl,
                shopId
        );
    }

    private void sendViaTelegram(Long shopId, OrderDTO order) {
        ShopDto shop = productService.getShopById(shopId);
        if (shop == null || shop.ownerId() == null || shop.telegramBotToken() == null) {
            log.warn("Cannot send notification: Shop {} has neither WhatsApp nor Telegram bot", shopId);
            return;
        }

        TelegramClient telegramClient = new OkHttpTelegramClient(shop.telegramBotToken());
        SendMessage sendMessage = SendMessage.builder()
                .chatId(shop.ownerId())
                .text(formatTelegramMessage(order))
                .parseMode(ParseMode.HTML)
                .build();

        try {
            telegramClient.execute(sendMessage);
            log.info("Notification sent to owner {} for shop {}", shop.ownerId(), shopId);
        } catch (TelegramApiException e) {
            log.error("Failed to send Telegram notification to owner {}: {}", shop.ownerId(), e.getMessage());
        }
    }

    String formatWhatsappMessage(OrderDTO order) {
        return String.format(
                "🛒 *Новый заказ #%d*\n\n" +
                "*Клиент:* %s\n" +
                "*Телефон:* %s\n" +
                "*Адрес:* %s\n\n" +
                "*Товары:*\n%s\n\n" +
                "*Оплата:* %s\n\n" +
                "Заказы магазина: %s/shops/%d/orders",
                order.getId(),
                order.getCustomerName(),
                order.getPhoneNumber(),
                order.getDeliveryAddress(),
                order.getItemsSummary(),
                order.getPaymentMethod(),
                frontendBaseUrl,
                order.getShopId()
        );
    }

    private String formatTelegramMessage(OrderDTO order) {
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
