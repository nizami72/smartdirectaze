package az.nizami.smartdirectaze.ai.internal;

import az.nizami.smartdirectaze.ai.AiService;
import az.nizami.smartdirectaze.shop.ConversationService;
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

    private final ProductService productService;
    private final AiService aiService;
    private final WhatsappService whatsappService;
    private final ConversationService conversationService;

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
        if (HandoffRules.isIgnored(event.text(), event.messageType())) {
            log.debug("WhatsApp {} in chat [{}] ignored", event.messageType(), event.chatId());
            return;
        }

        boolean paused = conversationService.recordIncomingAndCheckPaused(channel.shopId(), event.chatId(),
                event.senderName(), HandoffRules.messageLabel(event.text(), event.messageType()));
        if (paused) {
            log.debug("Chat [{}] of shop [{}] is with the seller, AI is silent", event.chatId(), channel.shopId());
            return;
        }

        Optional<String> handoffReason = HandoffRules.reasonBeforeAi(event.text(), event.messageType());
        if (handoffReason.isPresent()) {
            conversationService.handOverToSeller(channel.shopId(), event.chatId(), handoffReason.get());
            send(channel, event.chatId(), HandoffRules.customerNotice(event.text()));
            return;
        }

        String reply;
        try {
            // Memory per customer: the same shop talks to many customers
            String conversationId = "wa:" + channel.instanceId() + ":" + event.chatId();
            String aiText = aiService.answer(channel.shopId(), conversationId, event.chatId(), event.text());
            reply = WhatsappTextUtils.convertMdToWhatsapp(aiText);
        } catch (Exception e) {
            log.error("Error processing AI query for WhatsApp instance [{}]", event.instanceId(), e);
            // The customer must not be left without an answer: the seller takes over
            conversationService.handOverToSeller(channel.shopId(), event.chatId(), "AI не смог ответить (ошибка сервиса)");
            reply = HandoffRules.customerNotice(event.text());
        }
        send(channel, event.chatId(), reply);
    }

    private void send(WhatsappChannelDto channel, String chatId, String text) {
        whatsappService.sendMessage(channel.instanceId(), channel.apiToken(), chatId, text);
        log.debug("WhatsApp reply sent, shop [{}] chat [{}]", channel.shopId(), chatId);
    }
}
