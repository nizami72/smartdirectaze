package az.nizami.smartdirectaze.whatsapp.controller;

import az.nizami.smartdirectaze.whatsapp.WhatsappMessageReceivedEvent;
import az.nizami.smartdirectaze.whatsapp.dto.WebhookRequest;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("${app.url.component.webhook.w}")
@Log4j2
public class WhatsappWebhookController {

    private final ApplicationEventPublisher eventPublisher;
    // webhookUrlToken of the Green API instances; Green API sends it in the Authorization header
    private final String webhookToken;

    public WhatsappWebhookController(ApplicationEventPublisher eventPublisher,
                                     @Value("${app.whatsapp.webhook-token:}") String webhookToken) {
        this.eventPublisher = eventPublisher;
        this.webhookToken = webhookToken;
    }

    @PostConstruct
    void warnIfOpen() {
        if (webhookToken.isBlank()) {
            log.warn("WHATSAPP_WEBHOOK_TOKEN is not set: the WhatsApp webhook accepts requests from anyone. Set it before going live.");
        }
    }

    @GetMapping(value = "${app.url.component.alive}")
    public ResponseEntity<String> alive() {
        log.debug("Whatsapp alive called");
        return ResponseEntity.status(HttpStatus.OK).body("<h3>Whatsapp alive id here!</h3>");
    }

    @PostMapping
    public ResponseEntity<Void> handleIncomingMessage(@RequestBody WebhookRequest request,
                                                      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        if (!isAuthorized(authorization)) {
            log.warn("Whatsapp webhook rejected: invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
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

    private boolean isAuthorized(String authorization) {
        if (webhookToken.isBlank()) {
            return true;
        }
        if (authorization == null) {
            return false;
        }
        String received = authorization.startsWith("Bearer ") ? authorization.substring("Bearer ".length()) : authorization;
        return MessageDigest.isEqual(received.trim().getBytes(StandardCharsets.UTF_8), webhookToken.getBytes(StandardCharsets.UTF_8));
    }
}
