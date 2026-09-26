package az.nizami.smartdirectaze.shop.service;

import az.nizami.smartdirectaze.shop.ConversationDto;
import az.nizami.smartdirectaze.shop.ConversationService;
import az.nizami.smartdirectaze.shop.NotificationService;
import az.nizami.smartdirectaze.shop.entities.ChannelType;
import az.nizami.smartdirectaze.shop.entities.ConversationEntity;
import az.nizami.smartdirectaze.shop.entities.ConversationStatus;
import az.nizami.smartdirectaze.shop.repositories.AiChannelRepository;
import az.nizami.smartdirectaze.shop.repositories.ConversationRepository;
import az.nizami.smartdirectaze.whatsapp.WhatsappSellerMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ConversationServiceImpl implements ConversationService {

    private static final Duration ALERT_INTERVAL = Duration.ofMinutes(30);
    private static final int MAX_MESSAGE_LENGTH = 500;
    static final String SELLER_REPLIED = "Продавец ответил сам";

    private final ConversationRepository conversationRepository;
    private final AiChannelRepository aiChannelRepository;
    private final NotificationService notificationService;
    private final Duration pause;
    private final Clock clock;

    public ConversationServiceImpl(ConversationRepository conversationRepository,
                                   AiChannelRepository aiChannelRepository,
                                   NotificationService notificationService,
                                   @Value("${app.handoff.pause-hours:12}") long pauseHours,
                                   Clock clock) {
        this.conversationRepository = conversationRepository;
        this.aiChannelRepository = aiChannelRepository;
        this.notificationService = notificationService;
        this.pause = Duration.ofHours(pauseHours);
        this.clock = clock;
    }

    @Override
    @Transactional
    public boolean recordIncomingAndCheckPaused(Long shopId, String chatId, String customerName, String message) {
        ConversationEntity conversation = findOrCreate(shopId, chatId);
        if (customerName != null && !customerName.isBlank()) {
            conversation.setCustomerName(customerName);
        }
        conversation.setLastCustomerMessage(truncate(message));
        conversation.setLastCustomerMessageAt(now());

        if (conversation.getStatus() == ConversationStatus.HUMAN && !now().isBefore(conversation.getPausedUntil())) {
            // The pause is over: the AI answers again
            conversation.setStatus(ConversationStatus.AI);
            conversation.setPausedUntil(null);
            conversation.setHandoffReason(null);
        }
        conversationRepository.save(conversation);
        return conversation.getStatus() == ConversationStatus.HUMAN;
    }

    @Override
    @Transactional
    public void handOverToSeller(Long shopId, String chatId, String reason) {
        ConversationEntity conversation = findOrCreate(shopId, chatId);
        pause(conversation, reason);

        boolean alertedRecently = conversation.getLastAlertAt() != null
                && conversation.getLastAlertAt().isAfter(now().minus(ALERT_INTERVAL));
        if (!alertedRecently) {
            conversation.setLastAlertAt(now());
            notificationService.sendHumanHelpAlert(shopId, chatId, conversation.getCustomerName(), reason,
                    conversation.getLastCustomerMessage());
        }
        conversationRepository.save(conversation);
        log.info("Chat {} of shop {} handed over to the seller: {}", chatId, shopId, reason);
    }

    /**
     * The seller answered from the phone: the AI must not talk over him in this chat.
     */
    @EventListener
    @Transactional
    public void onSellerMessage(WhatsappSellerMessageEvent event) {
        aiChannelRepository.findByInstanceExternalIdAndChannelType(event.instanceId(), ChannelType.WHATSAPP)
                .ifPresent(channel -> {
                    Long shopId = channel.getShop().getId();
                    ConversationEntity conversation = findOrCreate(shopId, event.chatId());
                    pause(conversation, SELLER_REPLIED);
                    conversationRepository.save(conversation);
                    log.info("Seller replied in chat {} of shop {}: AI paused", event.chatId(), shopId);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDto> findWaitingForSeller(Long shopId) {
        return conversationRepository
                .findByShopIdAndStatusAndPausedUntilAfterOrderByLastCustomerMessageAtDesc(shopId, ConversationStatus.HUMAN, now())
                .stream()
                .map(c -> new ConversationDto(c.getId(), c.getChatId().replaceAll("@.*$", ""), c.getCustomerName(),
                        c.getHandoffReason(), c.getLastCustomerMessage(), c.getLastCustomerMessageAt(), c.getPausedUntil()))
                .toList();
    }

    @Override
    @Transactional
    public void resumeAi(Long shopId, Long conversationId) {
        ConversationEntity conversation = conversationRepository.findByIdAndShopId(conversationId, shopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
        conversation.setStatus(ConversationStatus.AI);
        conversation.setPausedUntil(null);
        conversation.setHandoffReason(null);
        conversationRepository.save(conversation);
    }

    private void pause(ConversationEntity conversation, String reason) {
        conversation.setStatus(ConversationStatus.HUMAN);
        conversation.setPausedUntil(now().plus(pause));
        // Keep the customer's reason when the seller then answers the handed-over chat himself
        if (!(SELLER_REPLIED.equals(reason) && conversation.getHandoffReason() != null)) {
            conversation.setHandoffReason(reason);
        }
    }

    private ConversationEntity findOrCreate(Long shopId, String chatId) {
        return conversationRepository.findByShopIdAndChatId(shopId, chatId)
                .orElseGet(() -> ConversationEntity.builder().shopId(shopId).chatId(chatId).build());
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private static String truncate(String message) {
        if (message == null || message.length() <= MAX_MESSAGE_LENGTH) {
            return message;
        }
        return message.substring(0, MAX_MESSAGE_LENGTH) + "…";
    }
}
