package az.nizami.smartdirectaze.integration.service;

import az.nizami.smartdirectaze.catalog.ProductService;
import az.nizami.smartdirectaze.integration.dto.ChannelType;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
@Log4j2
class TelegramIntegration implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final String botUsername;
    private final String botToken;
    private final ProductService catalog; // Твой сервис из модуля Catalog
    private final TelegramClient telegramClient;

    public TelegramIntegration(
            @Value("${telegram.token}") String botToken,
            @Value("${telegram.username}") String botUsername,
            ProductService catalog) {
//        super(options, botToken);
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.catalog = catalog;
        this.telegramClient = new OkHttpTelegramClient(botToken);
    }

    @Override
    public void consume(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String userText = update.getMessage().getText();
            log.debug("Message from user [{}]", userText);

            // ЛОГИКА: Ищем цену, если в тексте есть SKU или вопрос о цене
            // Пока сделаем заглушку, использующую твой DTO
            String response = "Salam! Məhsul haqqında məlumat axtarılır...";

            sendMessage(chatId, response);
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

}