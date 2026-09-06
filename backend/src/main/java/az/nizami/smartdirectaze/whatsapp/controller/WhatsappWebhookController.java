package az.nizami.smartdirectaze.whatsapp.controller;

import az.nizami.smartdirectaze.ai.AiService;
import az.nizami.smartdirectaze.shop.ProductService;
import az.nizami.smartdirectaze.shop.repositories.AiChannelRepository;
import az.nizami.smartdirectaze.whatsapp.dto.WebhookRequest;
import az.nizami.smartdirectaze.whatsapp.service.WhatsappService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${app.url.component.webhook.w}")
@Log4j2
public class WhatsappWebhookController {

    private final String VERIFY_TOKEN;
    private final ProductService productService;
    private final AiService aiService;
    private final WhatsappService whatsappService;
    private final AiChannelRepository aiChannelRepository;

    public WhatsappWebhookController(@Value("${instagram.webhook.verify-token}") String verifyToken, ProductService productService, AiService aiService, WhatsappService whatsappService, AiChannelRepository aiChannelRepository) {
        VERIFY_TOKEN = verifyToken;
        this.productService = productService;
        this.aiService = aiService;
        this.whatsappService = whatsappService;
        this.aiChannelRepository = aiChannelRepository;
    }

    @GetMapping(value = "${app.url.component.alive}")
    public ResponseEntity<String> alive() {
        log.debug("Whatsapp alive called");
        return ResponseEntity.status(HttpStatus.OK).body("<h3>Whatsapp alive id here!</h3>");
    }

    @PostMapping
    public ResponseEntity<Void> handleIncomingMessage(@RequestBody WebhookRequest request) {
        // Only process text messages
        if ("incomingMessageReceived".equals(request.getTypeWebhook()) &&
                request.getMessageData() != null &&
                request.getMessageData().getTextMessageData() != null) {

            String userMessage = request.getMessageData().getTextMessageData().getTextMessage();
            String instanceId = String.valueOf(request.getInstanceData().getIdInstance());
            String chatId = request.getSenderData().getChatId();

            // Direct to AI
//            processWithAi(instanceId, chatId, userMessage);
        }
        return ResponseEntity.ok().build();
    }
//    private void processWithAi(String instanceId, String chatId, String userMessage) {
//        // 1. Find the channel to get the shop context (botUuid)
//        // Note: You might need to add findByInstanceExternalId to AiChannelRepository
//        aiChannelRepository.findByInstanceExternalId(instanceId).ifPresent(channel -> {
//            String botUuid = channel.getInstanceExternalId(); // Or another unique identifier linked to the shop
//
//            // 2. Process query asynchronously
//            aiService.processQuery(botUuid, userMessage)
//                    .thenAccept(aiResponse -> {
//                        // 3. Send the AI's answer back to WhatsApp
//                        whatsappService.sendMessage(instanceId, channel.getApiToken(), chatId, aiResponse.getMessage());
//                    })
//                    .exceptionally(ex -> {
//                        log.error("AI Processing failed", ex);
//                        return null;
//                    });
//        });
//    }


}