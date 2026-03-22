package az.nizami.smartdirectaze.telegram.masterbot.controller;

import az.nizami.smartdirectaze.telegram.masterbot.service.MasterBotRouter;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks/")
@Log4j2
public class MasterBotController {

    private final MasterBotRouter router;

    public MasterBotController(MasterBotRouter router) {
        this.router = router;
    }

    @GetMapping(value = "/alive")
    public ResponseEntity<String> alive() {
        log.debug("Telegram alive called");
        return ResponseEntity.status(HttpStatus.OK).body("<h3>Telegram Master Bot is Here</h3>");
    }

    @PostMapping
    public ResponseEntity<Void> receiveUpdate(@RequestBody JsonNode update) {

        // Парсим JSON. В реальном проекте лучше использовать готовые DTO (например из библиотеки telegrambots)
        try {
            log.debug("Telegram receive update called.");
            if (update.has("message") && update.get("message").has("text")) {
                Long chatId = update.get("message").get("chat").get("id").asLong();
                String text = update.get("message").get("text").asText();
                router.processUpdate(chatId, text);
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке апдейта от Telegram [{}]", update, e);
        }
        return ResponseEntity.ok().build();
    }
}
