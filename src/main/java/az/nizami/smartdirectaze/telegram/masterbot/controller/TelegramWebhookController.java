package az.nizami.smartdirectaze.telegram.masterbot.controller;

import az.nizami.smartdirectaze.ai.AiService;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequestMapping("${app.url.public}")
@Log4j2
public class TelegramWebhookController {

    private final AiService aiService;

    public TelegramWebhookController(AiService aiService) {
        this.aiService = aiService;
    }


    // Динамический путь для поддержки множества ботов
    @PostMapping("/{botUuid}")
    public ResponseEntity<Void> receiveUpdate(
            @PathVariable String botUuid,
            @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String secretToken,
            @RequestBody Update update // Можно мапить сразу в DTO (например, класс Update из библиотеки)
    ) {
        // 1. (Опционально) Проверка secretToken для безопасности
        if (!isValidToken(botUuid, secretToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. Асинхронная передача в обработку
        aiService.processQuery("chatId".toString(), "userText")
                .thenAccept(aiResponse -> {
                    // Успешный ответ
//                    sendMessage(chatId, aiResponse.getMessage());
                })
                .exceptionally(ex -> {
                    // Если что-то пошло не так (ошибка сети, API и т.д.)
                    log.error("Error processing AI query: ", ex);
//                    sendMessage(chatId, "Извините, сервис временно недоступен. Попробуйте позже.");
                    return null;
                });

        // 3. Мгновенный ответ Telegram, что сообщение получено
        return ResponseEntity.ok().build();
    }

    private boolean isValidToken(String botUuid, String secretToken) {
        // Логика сверки токена из БД
        return true;
    }
}