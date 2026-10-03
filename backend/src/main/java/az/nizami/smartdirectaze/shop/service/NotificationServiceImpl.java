package az.nizami.smartdirectaze.shop.service;

import az.nizami.smartdirectaze.identity.Locales;
import az.nizami.smartdirectaze.identity.UserDto;
import az.nizami.smartdirectaze.identity.UserService;
import az.nizami.smartdirectaze.shop.PhoneUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import az.nizami.smartdirectaze.shop.entities.ChannelStatus;
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
    private final UserService userService;

    public NotificationServiceImpl(ProductService productService,
                                   AiChannelRepository aiChannelRepository,
                                   WhatsappService whatsappService,
                                   @Value("${app.frontend.base-url}") String frontendBaseUrl,
                                   UserService userService) {
        this.productService = productService;
        this.aiChannelRepository = aiChannelRepository;
        this.whatsappService = whatsappService;
        this.frontendBaseUrl = frontendBaseUrl;
        this.userService = userService;
    }

    /** Language of the shop owner's interface: alerts to the merchant are written in it */
    private boolean ownerReadsRussian(Long shopId) {
        try {
            ShopDto shop = productService.getShopById(shopId);
            return shop != null && shop.ownerId() != null && Locales.RU.equals(userService.findById(shop.ownerId())
                    .map(UserDto::getLocale).map(Locales::supported).orElse(Locales.DEFAULT));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Shops with a WhatsApp channel get the alert in WhatsApp; Telegram-bot shops (master bot flow) in Telegram.
     */
    @Override
    public void sendNewOrderAlertToOwner(Long shopId, OrderDTO order) {
        Optional<AiChannelEntity> whatsapp = aiChannelRepository.findByShopIdAndChannelType(shopId, ChannelType.WHATSAPP)
                .filter(channel -> channel.getInstanceExternalId() != null && channel.getApiToken() != null);
        if (whatsapp.isPresent()) {
            sendToMerchant(whatsapp.get(), formatWhatsappMessage(order, ownerReadsRussian(shopId)), "order #" + order.getId() + " alert");
        } else {
            sendViaTelegram(shopId, order);
        }
    }

    @Override
    public void sendHumanHelpAlert(Long shopId, String customerChatId, String customerName, String reason, String lastMessage,
                                   boolean aiPaused) {
        aiChannelRepository.findByShopIdAndChannelType(shopId, ChannelType.WHATSAPP)
                .filter(channel -> channel.getInstanceExternalId() != null && channel.getApiToken() != null)
                .ifPresentOrElse(
                        channel -> sendToMerchant(channel, formatHumanHelpMessage(customerChatId, customerName, reason, lastMessage, aiPaused,
                                ownerReadsRussian(shopId)),
                                "help alert for chat " + customerChatId),
                        () -> log.warn("Help alert for shop {} not sent: no WhatsApp channel", shopId));
    }

    @Override
    public String sendTestNotification(Long shopId) {
        AiChannelEntity channel = aiChannelRepository.findByShopIdAndChannelType(shopId, ChannelType.WHATSAPP)
                .filter(c -> c.getInstanceExternalId() != null && c.getApiToken() != null)
                .filter(c -> c.getChannelStatus() == ChannelStatus.CONNECTED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "error.whatsappNotConnected"));
        String target = channel.getNotificationPhone() != null ? channel.getNotificationPhone() : channel.getWid();
        if (target == null || target.isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error.shopNumberUnknown");
        }
        try {
            whatsappService.sendMessage(channel.getInstanceExternalId(), channel.getApiToken(), target + "@c.us",
                    ownerReadsRussian(shopId)
                            ? "✅ *Это номер для уведомлений SmartDirect*\n\n" +
                              "Сюда будут приходить новые заказы и вопросы покупателей, на которые нужен ваш ответ."
                            : "✅ *Bu, SmartDirect bildirişləri üçün nömrədir*\n\n" +
                              "Yeni sifarişlər və sizin cavabınız lazım olan müştəri sualları bura gələcək.");
        } catch (Exception e) {
            log.error("Test notification of shop {} not sent: {}", shopId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "error.whatsappRejected");
        }
        log.info("Test notification of shop {} sent", shopId);
        return target;
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
            log.error("NotificationServiceImpl: operation failed");
        }
    }

    // One action and one link: the seller wants to answer this customer right now
    String formatHumanHelpMessage(String customerChatId, String customerName, String reason, String lastMessage,
                                  boolean aiPaused, boolean russian) {
        return String.format(
                (russian
                        ? "🙋 *Нужна ваша помощь*\n\n*Клиент:* %s%s\n*Причина:* %s\n*Последнее сообщение:* %s\n\n%s\n\n👉 Ответить клиенту:\n"
                        : "🙋 *Köməyiniz lazımdır*\n\n*Müştəri:* %s%s\n*Səbəb:* %s\n*Son mesaj:* %s\n\n%s\n\n👉 Müştəriyə cavab verin:\n")
                        + "https://wa.me/%s",
                PhoneUtils.pretty(customerChatId),
                customerName != null && !customerName.isBlank() ? " (" + customerName + ")" : "",
                reason,
                lastMessage != null ? "«" + lastMessage + "»" : "—",
                russian
                        ? (aiPaused ? "AI в этом чате молчит, пока вы не ответите."
                                    : "AI отвечает клиенту на другие вопросы, а на этот ответьте вы.")
                        : (aiPaused ? "Siz cavab verənə qədər AI bu çatda susur."
                                    : "AI müştərinin digər suallarına cavab verir, bu suala isə siz cavab verin."),
                PhoneUtils.digits(customerChatId)
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
            log.error("NotificationServiceImpl: operation failed");
        }
    }

    String formatWhatsappMessage(OrderDTO order, boolean russian) {
        return String.format(russian
                        ? "🛒 *Новая заявка на заказ #%d*\n\n*Клиент:* %s\n*Телефон:* %s\n*Адрес:* %s\n\n*Товары:*\n%s\n\n" +
                          "*Оплата:* %s\n\nПодтвердите наличие и окончательную стоимость покупателю.\nЗаказы магазина: %s/shops/%d/orders"
                        : "🛒 *Yeni sifariş sorğusu #%d*\n\n*Müştəri:* %s\n*Telefon:* %s\n*Ünvan:* %s\n\n*Məhsullar:*\n%s\n\n" +
                          "*Ödəniş:* %s\n\nMüştəriyə stoku və yekun qiyməti təsdiqləyin.\nMağazanın sifarişləri: %s/shops/%d/orders",
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
                "<b>New order request #%d</b>\n\n" +
                "<b>Customer:</b> %s\n" +
                "<b>Phone:</b> %s\n" +
                "<b>Address:</b> %s\n" +
                "<b>Items:</b>\n%s\n\n" +
                "<b>Payment Method:</b> %s\nSeller must confirm availability and final price.",
                order.getId(),
                order.getCustomerName(),
                order.getPhoneNumber(),
                order.getDeliveryAddress(),
                order.getItemsSummary(),
                order.getPaymentMethod()
        );
    }
}
