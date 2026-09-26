package az.nizami.smartdirectaze.shop.service;

import az.nizami.smartdirectaze.shop.NotificationService;
import az.nizami.smartdirectaze.shop.entities.AiChannelEntity;
import az.nizami.smartdirectaze.shop.entities.ChannelType;
import az.nizami.smartdirectaze.shop.entities.ConversationEntity;
import az.nizami.smartdirectaze.shop.entities.ConversationStatus;
import az.nizami.smartdirectaze.shop.entities.ShopEntity;
import az.nizami.smartdirectaze.shop.repositories.AiChannelRepository;
import az.nizami.smartdirectaze.shop.repositories.ConversationRepository;
import az.nizami.smartdirectaze.whatsapp.WhatsappSellerMessageEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConversationServiceImplTest {

    private static final String CHAT = "994551112233@c.us";
    private static final Instant START = Instant.parse("2026-09-26T10:00:00Z");

    private final ConversationRepository repository = mock(ConversationRepository.class);
    private final AiChannelRepository aiChannelRepository = mock(AiChannelRepository.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    // In-memory "table" behind the repository mock
    private final Map<String, ConversationEntity> table = new HashMap<>();

    @BeforeEach
    void setUp() {
        when(repository.findByShopIdAndChatId(anyLong(), anyString()))
                .thenAnswer(inv -> Optional.ofNullable(table.get(inv.getArgument(1))));
        when(repository.save(any())).thenAnswer(inv -> {
            ConversationEntity c = inv.getArgument(0);
            table.put(c.getChatId(), c);
            return c;
        });
    }

    private ConversationServiceImpl serviceAt(Instant now) {
        return new ConversationServiceImpl(repository, aiChannelRepository, notificationService, 12, Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    void newChat_ShouldNotBePaused() {
        assertFalse(serviceAt(START).recordIncomingAndCheckPaused(4L, CHAT, "Leyla", "Salam"));
        assertEquals("Leyla", table.get(CHAT).getCustomerName());
    }

    @Test
    void handOver_ShouldPauseAndAlertOnceIn30Minutes() {
        serviceAt(START).recordIncomingAndCheckPaused(4L, CHAT, "Leyla", "Endirim olar?");
        serviceAt(START).handOverToSeller(4L, CHAT, "Хочет скидку");
        serviceAt(START.plusSeconds(10 * 60)).handOverToSeller(4L, CHAT, "Хочет скидку");

        assertTrue(serviceAt(START.plusSeconds(20 * 60)).recordIncomingAndCheckPaused(4L, CHAT, "Leyla", "?"));
        verify(notificationService, times(1)).sendHumanHelpAlert(4L, CHAT, "Leyla", "Хочет скидку", "Endirim olar?");

        serviceAt(START.plusSeconds(40 * 60)).handOverToSeller(4L, CHAT, "Хочет скидку");
        verify(notificationService, times(2)).sendHumanHelpAlert(anyLong(), anyString(), any(), anyString(), any());
    }

    @Test
    void pauseOver_ShouldGiveChatBackToAi() {
        serviceAt(START).handOverToSeller(4L, CHAT, "Клиент просит продавца");

        assertFalse(serviceAt(START.plusSeconds(13 * 3600)).recordIncomingAndCheckPaused(4L, CHAT, "Leyla", "Salam"));
        assertEquals(ConversationStatus.AI, table.get(CHAT).getStatus());
    }

    @Test
    void sellerReplied_ShouldPauseWithoutAlertAndKeepCustomerReason() {
        ShopEntity shop = ShopEntity.builder().id(4L).build();
        when(aiChannelRepository.findByInstanceExternalIdAndChannelType("7107000000", ChannelType.WHATSAPP))
                .thenReturn(Optional.of(AiChannelEntity.builder().shop(shop).build()));
        serviceAt(START).handOverToSeller(4L, CHAT, "Хочет скидку");

        serviceAt(START.plusSeconds(3600)).onSellerMessage(new WhatsappSellerMessageEvent("7107000000", CHAT));

        ConversationEntity c = table.get(CHAT);
        assertEquals(ConversationStatus.HUMAN, c.getStatus());
        assertEquals("Хочет скидку", c.getHandoffReason());
        assertEquals(LocalDateTime.ofInstant(START.plusSeconds(13 * 3600), ZoneOffset.UTC), c.getPausedUntil());
        verify(notificationService, times(1)).sendHumanHelpAlert(anyLong(), anyString(), any(), anyString(), any());
    }

    @Test
    void sellerRepliedInNewChat_ShouldPauseIt() {
        ShopEntity shop = ShopEntity.builder().id(4L).build();
        when(aiChannelRepository.findByInstanceExternalIdAndChannelType("7107000000", ChannelType.WHATSAPP))
                .thenReturn(Optional.of(AiChannelEntity.builder().shop(shop).build()));

        serviceAt(START).onSellerMessage(new WhatsappSellerMessageEvent("7107000000", CHAT));

        assertTrue(serviceAt(START.plusSeconds(60)).recordIncomingAndCheckPaused(4L, CHAT, "Leyla", "Salam"));
        verify(notificationService, never()).sendHumanHelpAlert(anyLong(), anyString(), any(), anyString(), any());
    }
}
