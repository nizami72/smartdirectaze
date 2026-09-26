package az.nizami.smartdirectaze.shop.service;

import az.nizami.smartdirectaze.identity.UserService;
import az.nizami.smartdirectaze.shop.entities.AiChannelEntity;
import az.nizami.smartdirectaze.shop.entities.ChannelStatus;
import az.nizami.smartdirectaze.shop.entities.ChannelType;
import az.nizami.smartdirectaze.shop.entities.ShopEntity;
import az.nizami.smartdirectaze.shop.repositories.AiChannelRepository;
import az.nizami.smartdirectaze.shop.repositories.ShopRepository;
import az.nizami.smartdirectaze.telegram.service.TelegramService;
import az.nizami.smartdirectaze.whatsapp.WhatsappStateChangedEvent;
import az.nizami.smartdirectaze.whatsapp.service.WhatsappService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AiChannelServiceImplTest {

    @Mock private AiChannelRepository aiChannelRepository;
    @Mock private ShopRepository shopRepository;
    @Mock private TelegramService telegramService;
    @Mock private WhatsappService whatsappService;
    @Mock private UserService userService;
    @InjectMocks private AiChannelServiceImpl service;

    private final ShopEntity shop = ShopEntity.builder().id(4L).shopName("Çanta Dünyası").ownerId(3L).build();
    private AiChannelEntity channel;

    @BeforeEach
    void setUp() {
        channel = AiChannelEntity.builder().shop(shop).channelType(ChannelType.WHATSAPP)
                .channelStatus(ChannelStatus.WAITING_FOR_INSTANCE).build();
        when(shopRepository.findById(4L)).thenReturn(Optional.of(shop));
        when(aiChannelRepository.findByShopIdAndChannelType(4L, ChannelType.WHATSAPP)).thenReturn(Optional.of(channel));
        when(userService.findById(3L)).thenReturn(Optional.empty());
    }

    @Test
    void bind_InstanceOfAnotherShop_ShouldConflict() {
        ShopEntity other = ShopEntity.builder().id(9L).shopName("Other").build();
        when(aiChannelRepository.findByInstanceExternalIdAndChannelType("7107000000", ChannelType.WHATSAPP))
                .thenReturn(Optional.of(AiChannelEntity.builder().shop(other).build()));

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.bindInstance(4L, "7107000000", "token"));
        assertEquals(HttpStatus.CONFLICT, e.getStatusCode());
        verify(whatsappService, never()).configureWebhooks(anyString(), anyString());
    }

    @Test
    void bind_TokenRejectedByGreenApi_ShouldNotSave() {
        doThrow(new RuntimeException("401")).when(whatsappService).configureWebhooks("7107000000", "bad");

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.bindInstance(4L, "7107000000", "bad"));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        verify(aiChannelRepository, never()).save(any());
    }

    @Test
    void bind_AlreadyConnectedNumber_ShouldBeConnectedWithPhone() {
        when(whatsappService.getStateInstance("7107000000", "token")).thenReturn("authorized");
        when(whatsappService.getSettings("7107000000", "token")).thenReturn("994551234567@c.us");

        var result = service.bindInstance(4L, "7107000000", "token");

        verify(whatsappService).configureWebhooks("7107000000", "token");
        assertEquals("CONNECTED", result.channelStatus());
        assertEquals("994551234567", result.connectedPhone());
        assertEquals("7107000000", result.instanceId());
    }

    @Test
    void state_Authorized_ShouldConnectAndStorePhone() {
        channel.setInstanceExternalId("7107000000");
        channel.setApiToken("token");
        channel.setChannelStatus(ChannelStatus.WAITING_QR);
        when(aiChannelRepository.findByInstanceExternalIdAndChannelType("7107000000", ChannelType.WHATSAPP)).thenReturn(Optional.of(channel));
        when(whatsappService.getSettings("7107000000", "token")).thenReturn("994551234567@c.us");

        service.onStateChanged(new WhatsappStateChangedEvent("7107000000", "authorized"));

        assertEquals(ChannelStatus.CONNECTED, channel.getChannelStatus());
        assertEquals("994551234567", channel.getWid());
        verify(telegramService, never()).notifyAdmin(anyString());
    }

    @Test
    void state_WorkingNumberDropped_ShouldAlertOperator() {
        channel.setInstanceExternalId("7107000000");
        channel.setChannelStatus(ChannelStatus.CONNECTED);
        when(aiChannelRepository.findByInstanceExternalIdAndChannelType("7107000000", ChannelType.WHATSAPP)).thenReturn(Optional.of(channel));

        service.onStateChanged(new WhatsappStateChangedEvent("7107000000", "notAuthorized"));

        assertEquals(ChannelStatus.DISCONNECTED, channel.getChannelStatus());
        verify(telegramService).notifyAdmin(contains("Çanta Dünyası"));
    }

    @Test
    void state_NotAuthorizedWhileWaitingForQr_ShouldNotAlert() {
        channel.setInstanceExternalId("7107000000");
        channel.setChannelStatus(ChannelStatus.WAITING_QR);
        when(aiChannelRepository.findByInstanceExternalIdAndChannelType("7107000000", ChannelType.WHATSAPP)).thenReturn(Optional.of(channel));

        service.onStateChanged(new WhatsappStateChangedEvent("7107000000", "notAuthorized"));

        verify(telegramService, never()).notifyAdmin(anyString());
    }
}
