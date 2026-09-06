package az.nizami.smartdirectaze.shop.service;

import az.nizami.smartdirectaze.shop.dto.channel.WhatsAppQrResponse;
import az.nizami.smartdirectaze.shop.entities.AiChannelEntity;
import az.nizami.smartdirectaze.shop.entities.ChannelStatus;
import az.nizami.smartdirectaze.shop.entities.ChannelType;
import az.nizami.smartdirectaze.shop.entities.ShopEntity;
import az.nizami.smartdirectaze.telegram.service.TelegramService;
import az.nizami.smartdirectaze.whatsapp.service.WhatsappService;
import az.nizami.smartdirectaze.shop.repositories.AiChannelRepository;
import az.nizami.smartdirectaze.shop.repositories.ShopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiChannelServiceTest {

    @Mock
    private AiChannelRepository aiChannelRepository;
    @Mock
    private ShopRepository shopRepository;
    @Mock
    private TelegramService telegramService;
    @Mock
    private WhatsappService whatsappService;

    private AiChannelService aiChannelService;

    @BeforeEach
    void setUp() {
        aiChannelService = new AiChannelServiceImpl(aiChannelRepository, shopRepository, telegramService, whatsappService);
    }

    @Test
    void connectTelegram_ShouldSaveChannel() {
        Long shopId = 1L;
        String token = "test_token";
        String botUsername = "test_bot";
        ShopEntity shop = ShopEntity.builder().id(shopId).build();

        when(telegramService.getBotUsername(token)).thenReturn(botUsername);
        when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop));
        when(aiChannelRepository.findByShopIdAndChannelType(shopId, ChannelType.TELEGRAM)).thenReturn(Optional.empty());

        aiChannelService.connectTelegram(shopId, token);

        ArgumentCaptor<AiChannelEntity> captor = ArgumentCaptor.forClass(AiChannelEntity.class);
        verify(aiChannelRepository).save(captor.capture());
        
        AiChannelEntity saved = captor.getValue();
        assertEquals(ChannelType.TELEGRAM, saved.getChannelType());
        assertEquals(ChannelStatus.CONNECTED, saved.getChannelStatus());
        assertEquals(botUsername, saved.getInstanceExternalId());
        assertEquals(token, saved.getApiToken());
    }

    @Test
    void initWhatsApp_ShouldSaveChannel() {
        Long shopId = 1L;
        String instanceId = "12345";
        String token = "wa_token";
        ShopEntity shop = ShopEntity.builder().id(shopId).build();

        when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop));
        when(aiChannelRepository.findByShopIdAndChannelType(shopId, ChannelType.WHATSAPP)).thenReturn(Optional.empty());

        aiChannelService.initWhatsApp(shopId, instanceId, token);

        ArgumentCaptor<AiChannelEntity> captor = ArgumentCaptor.forClass(AiChannelEntity.class);
        verify(aiChannelRepository).save(captor.capture());

        AiChannelEntity saved = captor.getValue();
        assertEquals(ChannelType.WHATSAPP, saved.getChannelType());
        assertEquals(ChannelStatus.WAITING_QR, saved.getChannelStatus());
        assertEquals(instanceId, saved.getInstanceExternalId());
        assertEquals(token, saved.getApiToken());
    }

    @Test
    void getWhatsAppQr_ShouldReturnPendingActivation_WhenTokensMissing() {
        Long shopId = 1L;
        AiChannelEntity channel = AiChannelEntity.builder()
                .channelType(ChannelType.WHATSAPP)
                .build();
        
        when(aiChannelRepository.findByShopIdAndChannelType(shopId, ChannelType.WHATSAPP))
                .thenReturn(Optional.of(channel));

        WhatsAppQrResponse response = aiChannelService.getWhatsAppQr(shopId);

        assertEquals(ChannelStatus.PENDING_ACTIVATION, response.getStatus());
        verify(whatsappService, never()).getStateInstance(any(), any());
    }

    @Test
    void getWhatsAppQr_ShouldReturnConnected_WhenAuthorized() {
        Long shopId = 1L;
        AiChannelEntity channel = AiChannelEntity.builder()
                .channelType(ChannelType.WHATSAPP)
                .instanceExternalId("inst123")
                .apiToken("tok123")
                .build();
        
        when(aiChannelRepository.findByShopIdAndChannelType(shopId, ChannelType.WHATSAPP))
                .thenReturn(Optional.of(channel));
        when(whatsappService.getStateInstance("inst123", "tok123")).thenReturn("authorized");

        WhatsAppQrResponse response = aiChannelService.getWhatsAppQr(shopId);

        assertEquals(ChannelStatus.CONNECTED, response.getStatus());
        assertEquals(ChannelStatus.CONNECTED, channel.getChannelStatus());
        verify(aiChannelRepository).save(channel);
    }

    @Test
    void getWhatsAppQr_ShouldReturnWaitingQr_WhenNotAuthorized() {
        Long shopId = 1L;
        String mockQr = "mock_qr_string";
        az.nizami.smartdirectaze.whatsapp.GreenApiResponseDto mockResponse = az.nizami.smartdirectaze.whatsapp.GreenApiResponseDto.builder()
                .message(mockQr)
                .build();
        AiChannelEntity channel = AiChannelEntity.builder()
                .channelType(ChannelType.WHATSAPP)
                .instanceExternalId("inst123")
                .apiToken("tok123")
                .build();
        
        when(aiChannelRepository.findByShopIdAndChannelType(shopId, ChannelType.WHATSAPP))
                .thenReturn(Optional.of(channel));
        when(whatsappService.getStateInstance("inst123", "tok123")).thenReturn("notAuthorized");
        when(whatsappService.getQrCode("inst123", "tok123")).thenReturn(mockResponse);

        WhatsAppQrResponse response = aiChannelService.getWhatsAppQr(shopId);

        assertEquals(ChannelStatus.WAITING_QR, response.getStatus());
        assertEquals(mockQr, response.getQrCode());
        assertEquals(ChannelStatus.WAITING_QR, channel.getChannelStatus());
        verify(aiChannelRepository).save(channel);
    }

    @Test
    void getWhatsAppQr_ShouldReturnPendingActivation_WhenChannelNotFound() {
        Long shopId = 8L;
        when(aiChannelRepository.findByShopIdAndChannelType(shopId, ChannelType.WHATSAPP))
                .thenReturn(Optional.empty());

        WhatsAppQrResponse response = aiChannelService.getWhatsAppQr(shopId);

        assertEquals(ChannelStatus.PENDING_ACTIVATION, response.getStatus());
    }
}
