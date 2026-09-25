package az.nizami.smartdirectaze.whatsapp.controller;

import az.nizami.smartdirectaze.whatsapp.WhatsappMessageReceivedEvent;
import az.nizami.smartdirectaze.whatsapp.dto.WebhookRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${app.url.component.webhook.w}")
@RequiredArgsConstructor
@Log4j2
public class WhatsappWebhookController {

    private final ApplicationEventPublisher eventPublisher;

    @GetMapping(value = "${app.url.component.alive}")
    public ResponseEntity<String> alive() {
        log.debug("Whatsapp alive called");
        return ResponseEntity.status(HttpStatus.OK).body("<h3>Whatsapp alive id here!</h3>");
    }

    @PostMapping
    public ResponseEntity<Void> handleIncomingMessage(@RequestBody WebhookRequest request) {
        log.debug("Whatsapp webhook received: {}", request);

        // Only process incoming text messages from private chats
        if (!"incomingMessageReceived".equals(request.getTypeWebhook())
                || request.getMessageData() == null
                || request.getInstanceData() == null
                || request.getSenderData() == null) {
            return ResponseEntity.ok().build();
        }

        String userMessage = request.getMessageData().extractText();
        String chatId = request.getSenderData().getChatId();
        if (userMessage == null || userMessage.isBlank() || chatId == null || chatId.endsWith("@g.us")) {
            return ResponseEntity.ok().build();
        }

        String instanceId = String.valueOf(request.getInstanceData().getIdInstance());
        // Answer asynchronously: Green API expects a fast 200, otherwise it retries the webhook
        eventPublisher.publishEvent(new WhatsappMessageReceivedEvent(instanceId, chatId, userMessage));

        return ResponseEntity.ok().build();
    }
}
