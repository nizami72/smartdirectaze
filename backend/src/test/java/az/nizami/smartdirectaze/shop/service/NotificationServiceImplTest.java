package az.nizami.smartdirectaze.shop.service;

import az.nizami.smartdirectaze.shop.OrderDTO;
import az.nizami.smartdirectaze.shop.ProductService;
import az.nizami.smartdirectaze.shop.entities.AiChannelEntity;
import az.nizami.smartdirectaze.shop.entities.ChannelType;
import az.nizami.smartdirectaze.shop.repositories.AiChannelRepository;
import az.nizami.smartdirectaze.whatsapp.service.WhatsappService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceImplTest {

    private final ProductService productService = mock(ProductService.class);
    private final AiChannelRepository aiChannelRepository = mock(AiChannelRepository.class);
    private final WhatsappService whatsappService = mock(WhatsappService.class);
    private NotificationServiceImpl notificationService;

    private final OrderDTO order = OrderDTO.builder()
            .id(12L).shopId(4L).customerName("Leyla").phoneNumber("+994551112233")
            .deliveryAddress("Nizami küç. 5").itemsSummary("Qara dəri çanta x1").paymentMethod("CASH")
            .build();

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(productService, aiChannelRepository, whatsappService, "https://app.example");
    }

    private void whatsappChannel(String notificationPhone) {
        AiChannelEntity channel = AiChannelEntity.builder()
                .channelType(ChannelType.WHATSAPP).instanceExternalId("7107000000").apiToken("token")
                .notificationPhone(notificationPhone).build();
        when(aiChannelRepository.findByShopIdAndChannelType(4L, ChannelType.WHATSAPP)).thenReturn(Optional.of(channel));
    }

    @Test
    void whatsappShop_WithNotificationPhone_ShouldSendThere() {
        whatsappChannel("994701234567");

        notificationService.sendNewOrderAlertToOwner(4L, order);

        ArgumentCaptor<String> text = ArgumentCaptor.forClass(String.class);
        verify(whatsappService).sendMessage(eq("7107000000"), eq("token"), eq("994701234567@c.us"), text.capture());
        assertTrue(text.getValue().contains("Новый заказ #12"));
        assertTrue(text.getValue().contains("https://app.example/shops/4/orders"));
        verify(whatsappService, never()).getSettings(anyString(), anyString());
    }

    @Test
    void whatsappShop_WithoutNotificationPhone_ShouldSendToOwnNumber() {
        whatsappChannel(null);
        when(whatsappService.getSettings("7107000000", "token")).thenReturn("994551234567@c.us");

        notificationService.sendNewOrderAlertToOwner(4L, order);

        verify(whatsappService).sendMessage(eq("7107000000"), eq("token"), eq("994551234567@c.us"), anyString());
    }

    @Test
    void shopWithoutWhatsapp_ShouldNotUseWhatsapp() {
        when(aiChannelRepository.findByShopIdAndChannelType(4L, ChannelType.WHATSAPP)).thenReturn(Optional.empty());

        notificationService.sendNewOrderAlertToOwner(4L, order);

        verify(whatsappService, never()).sendMessage(anyString(), anyString(), anyString(), anyString());
        verify(productService).getShopById(4L);
    }
}
