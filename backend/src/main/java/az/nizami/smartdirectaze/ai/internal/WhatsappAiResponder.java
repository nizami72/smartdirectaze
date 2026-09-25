package az.nizami.smartdirectaze.ai.internal;

import az.nizami.smartdirectaze.ai.AiService;
import az.nizami.smartdirectaze.shop.ProductService;
import az.nizami.smartdirectaze.shop.WhatsappChannelDto;
import az.nizami.smartdirectaze.util.WhatsappTextUtils;
import az.nizami.smartdirectaze.whatsapp.WhatsappMessageReceivedEvent;
import az.nizami.smartdirectaze.whatsapp.service.WhatsappService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * AI seller for WhatsApp: finds the shop by Green API instance, asks the agent and sends the answer back.
 */
@Component
@RequiredArgsConstructor
@Log4j2
class WhatsappAiResponder {

    private static final String FALLBACK_MESSAGE = "Извините, сервис временно недоступен. Попробуйте позже.";

    private final ProductService productService;
    private final AiService aiService;
    private final WhatsappService whatsappService;

    @Async
    @EventListener
    public void onMessage(WhatsappMessageReceivedEvent event) {
        Optional<WhatsappChannelDto> channelOp = productService.findWhatsappChannel(event.instanceId());
        if (channelOp.isEmpty()) {
            log.error("No shop connected to WhatsApp instance [{}]", event.instanceId());
            return;
        }
        WhatsappChannelDto channel = channelOp.get();
        if (!channel.aiAnswers(event.chatId())) {
            log.debug("AI is {} for shop [{}], chat [{}] is not answered", channel.aiMode(), channel.shopId(), event.chatId());
            return;
        }

        String reply;
        try {
            // Memory per customer: the same shop talks to many customers
            String conversationId = "wa:" + channel.instanceId() + ":" + event.chatId();
            String aiText = aiService.answer(channel.shopId(), conversationId, event.text());
            reply = WhatsappTextUtils.convertMdToWhatsapp(aiText);
        } catch (Exception e) {
            log.error("Error processing AI query for WhatsApp instance [{}]", event.instanceId(), e);
            reply = FALLBACK_MESSAGE;
        }

        whatsappService.sendMessage(channel.instanceId(), channel.apiToken(), event.chatId(), reply);
        log.debug("WhatsApp reply sent, shop [{}] chat [{}]", channel.shopId(), event.chatId());
    }
}
