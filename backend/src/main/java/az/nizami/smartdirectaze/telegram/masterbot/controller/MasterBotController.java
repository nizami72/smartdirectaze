package az.nizami.smartdirectaze.telegram.masterbot.controller;

import az.nizami.smartdirectaze.telegram.masterbot.TelegramApiClient;
import az.nizami.smartdirectaze.telegram.masterbot.service.MasterBotRouter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequestMapping("${app.url.component.webhook.t}")
@Log4j2
public class MasterBotController {

    //<editor-fold desc="Fields">
    private final MasterBotRouter router;
    private final TelegramApiClient telegramClient;
    @Value("${telegram.token}")
    private String masterBotToken;
    //</editor-fold>

    //<editor-fold desc="Constructor">
    public MasterBotController(MasterBotRouter router, TelegramApiClient telegramClient) {
        this.router = router;
        this.telegramClient = telegramClient;
    }
    //</editor-fold>

    @GetMapping(value = "${app.url.component.alive}")
    public ResponseEntity<String> alive() {
        log.debug("Telegram alive called");
        return ResponseEntity.status(HttpStatus.OK).body("<h3>Telegram Master Bot is Here</h3>");
    }

    @PostMapping(value = "${app.url.component.master}")
    public ResponseEntity<Void> receiveUpdate(@RequestBody Update update) {
        log.debug("Telegram receive update called.");
        if (update.getMessage() == null || update.getMessage().getText() == null) {
            log.error("Invalid update received: {}", update);
            return ResponseEntity.ok().build();
        }

        String reply;

        try {
            reply = router.processUpdate(update);
            telegramClient.sendMessage(masterBotToken, update.getMessage().getChatId(), reply);
        } catch (Exception e) {
            log.error("Ошибка при обработке апдейта от Telegram [{}]", update, e);
        }
        return ResponseEntity.ok().build();
    }
}
