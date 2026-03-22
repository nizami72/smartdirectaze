package az.nizami.smartdirectaze.telegram.service;

import az.nizami.smartdirectaze.ai.AiService;
import az.nizami.smartdirectaze.catalog.ProductService;
import az.nizami.smartdirectaze.telegram.dto.ChannelType;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

//@Service
@Log4j2
class TelegramIntegration implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private static final String UPDATE_CATALOG = "/update";

    private final String botUsername;
    private final String botToken;
    private final ProductService catalog; // Your service from the Catalog module
    private final TelegramClient telegramClient;
    private final ProductService productService;
    private final long adminId;
    private final AiService aiService;

    public TelegramIntegration(
            @Value("${telegram.token}") String botToken,
            @Value("${telegram.username}") String botUsername,
            @Value("${telegram.bot.admin.id}") long adminId,
            ProductService catalog, ProductService productService, AiService aiService) {
//        super(options, botToken);
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.catalog = catalog;
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.productService = productService;
        this.adminId = adminId;
        this.aiService = aiService;
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String userText = update.getMessage().getText();
            if (userText.startsWith("/")) {
                sendMessage(chatId, technical(userText, chatId));
                return;
            }
            log.debug("Message from user [{}]", userText);
            sendTypingStatus(chatId);
            aiService.processQuery(chatId.toString(), userText)
                    .thenAccept(aiResponse -> {
                        // Успешный ответ
                        sendMessage(chatId, aiResponse.getMessage());
                    })
                    .exceptionally(ex -> {
                        // Если что-то пошло не так (ошибка сети, API и т.д.)
                        log.error("Error processing AI query: ", ex);
                        sendMessage(chatId, "Извините, сервис временно недоступен. Попробуйте позже.");
                        return null;
                    });

        }
    }

    private void sendTypingStatus(long chatId) {
        SendChatAction action = SendChatAction.builder()
                .chatId(chatId)
                .action(ActionType.TYPING.toString())
                .build();
        try {
            telegramClient.execute(action);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private Integer sendMessage(long chatId, String text) {
        SendMessage sm = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode(ParseMode.HTML)
                .build();
        try {
            return telegramClient.execute(sm).getMessageId();
        } catch (TelegramApiException e) {
            return null;
        }
    }

    public boolean supports(ChannelType type) {
        return type == ChannelType.TELEGRAM;
    }

    public String getBotUsername() {
        return "SmartDirectAzBot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingSingleThreadUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    private String technical(String userText, long chatId) {
        if (chatId == adminId) {
            if (userText.equalsIgnoreCase(UPDATE_CATALOG)) {
                productService.synchroniseProducts();
                return "Catalog updated";
            } else {
                return "Ok";
            }
        }
        return "You have no right";
    }

}