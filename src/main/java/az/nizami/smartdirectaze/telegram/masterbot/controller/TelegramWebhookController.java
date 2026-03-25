package az.nizami.smartdirectaze.telegram.masterbot.controller;

import az.nizami.smartdirectaze.catalog.ShopDto;
import az.nizami.smartdirectaze.ai.AiService;
import az.nizami.smartdirectaze.catalog.ProductService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Optional;

@RestController
@RequestMapping("${app.url.component.webhook}")
@Log4j2
public class TelegramWebhookController {

    private final AiService aiService;
    private final ProductService productService; // Сервис, который достанет токен по UUID

    public TelegramWebhookController(AiService aiService,
                                     ProductService productService) {
        this.aiService = aiService;
        this.productService = productService;
    }

    @PostMapping("/{botUuid}")
    public ResponseEntity<Void> receiveUpdate(
            @PathVariable String botUuid,
            @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String secretToken,
            @RequestBody Update update
    ) {
        log.debug("Update received for bot [{}]: {}", botUuid, update);

        // 1. Проверка безопасности (Secret Token)
        if (!isValidToken(botUuid, secretToken)) {
            log.error("Invalid secret token for bot UUID [{}]", botUuid);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. Базовая проверка, что это текстовое сообщение
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return ResponseEntity.ok().build(); // Игнорируем не-текстовые апдейты пока что
        }

        long chatId = update.getMessage().getChatId();
        String userMessage = update.getMessage().getText();

        // 3. Достаем токен клиентского бота из БД
        Optional<ShopDto> botTokenOp = productService.findByBotUuid(botUuid);
        if (botTokenOp.isEmpty()) {
            log.error("Bot token not found for UUID [{}]", botUuid);
            return ResponseEntity.notFound().build();
        }

        // 4. Создаем клиент именно для этого бота
        TelegramClient dynamicTelegramClient = new OkHttpTelegramClient(botTokenOp.get().botToken());

        // 5. Асинхронная обработка AI
        aiService.processQuery(String.valueOf(chatId), userMessage)
                .thenAccept(aiResponse -> {
                    sendMessage(dynamicTelegramClient, chatId, aiResponse.getMessage());
                })
                .exceptionally(ex -> {
                    log.error("Error processing AI query: ", ex);
                    sendMessage(dynamicTelegramClient, chatId, "Извините, сервис временно недоступен. Попробуйте позже.");
                    return null;
                });

        return ResponseEntity.ok().build();
    }

    // Передаем TelegramClient в метод как аргумент
    private Integer sendMessage(TelegramClient client, long chatId, String text) {
        SendMessage sm = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode(ParseMode.HTML)
                .build();
        try {
            return client.execute(sm).getMessageId();
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chat {}: {}", chatId, e.getMessage());
            return null;
        }
    }

    private boolean isValidToken(String botUuid, String secretToken) {
        // Логика сверки токена из БД (например, shopService.getSecretToken(botUuid).equals(secretToken))
        return true;
    }
}
