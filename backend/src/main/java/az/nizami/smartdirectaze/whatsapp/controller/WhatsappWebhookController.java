package az.nizami.smartdirectaze.whatsapp.controller;

import az.nizami.smartdirectaze.whatsapp.WhatsappMessageReceivedEvent;
import az.nizami.smartdirectaze.whatsapp.WhatsappSellerMessageEvent;
import az.nizami.smartdirectaze.whatsapp.WhatsappStateChangedEvent;
import az.nizami.smartdirectaze.whatsapp.dto.WebhookRequest;
import az.nizami.smartdirectaze.whatsapp.IncomingWhatsappMessageService;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
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
    private final IncomingWhatsappMessageService inbox;

    public WhatsappWebhookController(ApplicationEventPublisher eventPublisher,
                                     @Value("${app.whatsapp.webhook-token:}") String webhookToken,
                                     IncomingWhatsappMessageService inbox, Environment environment) {
        this.eventPublisher = eventPublisher;
        this.webhookToken = webhookToken;
        this.inbox = inbox;
        boolean local = environment.acceptsProfiles(Profiles.of("local", "mock", "test"));
        if (webhookToken.isBlank() && (!local || environment.acceptsProfiles(Profiles.of("prod", "production")))) {
            throw new IllegalStateException("WHATSAPP_WEBHOOK_TOKEN is required outside local/mock/test profiles");
        }
    }

    @PostMapping
    public ResponseEntity<Void> handleIncomingMessage(@RequestBody WebhookRequest request,
                                                      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        if (!isAuthorized(authorization)) {
            log.warn("Whatsapp webhook rejected: invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.debug("WhatsApp webhook received");

        if (request.getInstanceData() == null) {
            return ResponseEntity.ok().build();
        }
        String instanceId = String.valueOf(request.getInstanceData().getIdInstance());
        if ("stateInstanceChanged".equals(request.getTypeWebhook())) {
            eventPublisher.publishEvent(new WhatsappStateChangedEvent(instanceId, request.getStateInstance()));
            return ResponseEntity.ok().build();
        }
        if (request.getSenderData() == null) {
            return ResponseEntity.ok().build();
        }
        String chatId = request.getSenderData().getChatId();
        // Private chats only: groups end with @g.us
        if (chatId == null || chatId.endsWith("@g.us")) {
            return ResponseEntity.ok().build();
        }

        // Answered asynchronously: Green API expects a fast 200, otherwise it retries the webhook
        switch (String.valueOf(request.getTypeWebhook())) {
            case "incomingMessageReceived" -> {
                if (request.getMessageData() == null) {
                    break;
                }
                if (request.getIdMessage() == null || request.getIdMessage().isBlank()
                        || request.getIdMessage().length() > 255 || chatId.length() > 255) {
                    return ResponseEntity.badRequest().build();
                }
                inbox.accept(request.getIdMessage(), new WhatsappMessageReceivedEvent(instanceId, chatId,
                        request.getSenderData().getSenderName(),
                        request.getMessageData().extractText(),
                        request.getMessageData().getTypeMessage()));
            }
            // Sent by the seller from the phone (not by us via API): needs outgoingMessageWebhook enabled in Green API
            case "outgoingMessageReceived" -> eventPublisher.publishEvent(new WhatsappSellerMessageEvent(instanceId, chatId));
            default -> log.debug("Whatsapp webhook {} ignored", request.getTypeWebhook());
        }

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
