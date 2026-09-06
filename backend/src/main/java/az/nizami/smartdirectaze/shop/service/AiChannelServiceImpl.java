package az.nizami.smartdirectaze.shop.service;

import az.nizami.smartdirectaze.shop.dto.channel.WhatsAppQrResponse;
import az.nizami.smartdirectaze.shop.entities.AiChannelEntity;
import az.nizami.smartdirectaze.shop.entities.ChannelStatus;
import az.nizami.smartdirectaze.shop.entities.ChannelType;
import az.nizami.smartdirectaze.shop.entities.ShopEntity;
import az.nizami.smartdirectaze.shop.repositories.AiChannelRepository;
import az.nizami.smartdirectaze.shop.repositories.ShopRepository;
import az.nizami.smartdirectaze.telegram.service.TelegramService;
import az.nizami.smartdirectaze.whatsapp.GreenApiResponseDto;
import az.nizami.smartdirectaze.whatsapp.service.WhatsappService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChannelServiceImpl implements AiChannelService {

    private final AiChannelRepository aiChannelRepository;
    private final ShopRepository shopRepository;
    private final TelegramService telegramService;
    private final WhatsappService whatsappService;

    @Override
    @Transactional
    public void connectTelegram(Long shopId, String token) {
        String botUsername = telegramService.getBotUsername(token);
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + shopId));

        AiChannelEntity channel = aiChannelRepository.findByShopIdAndChannelType(shopId, ChannelType.TELEGRAM)
                .orElse(AiChannelEntity.builder()
                        .shop(shop)
                        .channelType(ChannelType.TELEGRAM)
                        .build());

        channel.setApiToken(token);
        channel.setInstanceExternalId(botUsername);
        channel.setChannelStatus(ChannelStatus.CONNECTED);

        aiChannelRepository.save(channel);
        log.info("Telegram bot @{} connected for shop {}", botUsername, shopId);
    }

    @Override
    @Transactional
    public void initWhatsApp(Long shopId, String instanceId, String token) {
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + shopId));

        AiChannelEntity channel = aiChannelRepository.findByShopIdAndChannelType(shopId, ChannelType.WHATSAPP)
                .orElse(AiChannelEntity.builder()
                        .shop(shop)
                        .channelType(ChannelType.WHATSAPP)
                        .build());

        channel.setInstanceExternalId(instanceId);
        channel.setApiToken(token);
        channel.setChannelStatus(ChannelStatus.WAITING_QR);

        aiChannelRepository.save(channel);
        log.info("WhatsApp instance {} initialized for shop {}", instanceId, shopId);
    }

    @Override
    public WhatsAppQrResponse getWhatsAppQr(Long shopId) {
        log.debug("Get qr code for whatsapp channel activation [{}]", shopId);
        // 1. Быстро читаем данные из БД (транзакция откроется и закроется внутри репозитория)
        var channelOpt = aiChannelRepository.findByShopIdAndChannelType(shopId, ChannelType.WHATSAPP);

        if (channelOpt.isEmpty()) {
            log.debug("Whatsapp channel still null, no instance_id no token [{}]", shopId);
            return WhatsAppQrResponse.builder()
                    .status(ChannelStatus.PENDING_ACTIVATION)
                    .build();
        }

        var aiChannelEntity = channelOpt.get();
        String instanceId = aiChannelEntity.getInstanceExternalId();
        String token = aiChannelEntity.getApiToken();

        if (instanceId == null || instanceId.isBlank() || token == null || token.isBlank()) {
            log.debug("Whatsapp channel not null, but some info still missing [{}]", shopId);
            return WhatsAppQrResponse.builder()
                    .status(ChannelStatus.PENDING_ACTIVATION)
                    .build();
        }

        // 2. ДЕЛАЕМ СЕТЕВЫЕ ВЫЗОВЫ (Соединение с БД СЕЙЧАС НЕ ИСПОЛЬЗУЕТСЯ)
        GreenApiResponseDto responseDto = whatsappService.getQrCode(instanceId, token);
        String type = responseDto.type();
        String message = responseDto.message();

        if ("alreadyLogged".equals(type)) {
            log.info("Already logged in");
            return WhatsAppQrResponse.builder()
                    .status(ChannelStatus.CONNECTED)
                    .build();
        } else if ("qrCode".equals(type)) {
            log.debug("Qr code are received, waiting for client phone to be logged in [{}]", shopId);
            aiChannelEntity.setChannelStatus(ChannelStatus.WAITING_QR);
            return WhatsAppQrResponse.builder()
                    .status(ChannelStatus.WAITING_QR)
                    .qrCode(message)
                    .build();
        } else if ("error".equals(type)) {
            log.error("Error form green api message [{}], shop [{}]", message, shopId);
            return WhatsAppQrResponse.builder()
                    .status(ChannelStatus.PENDING_ACTIVATION)
                    .build();
        }

        return WhatsAppQrResponse.builder()
                .status(ChannelStatus.PENDING_ACTIVATION)
                .build();
    }

    @Override
    public List<AiChannelEntity> createBaseChannels(ShopEntity shopEntity) {
        return List.of(AiChannelEntity.builder()
                .channelType(ChannelType.WHATSAPP)
                .channelStatus(ChannelStatus.WAITING_FOR_INSTANCE)
                .shop(shopEntity)
                .build());
    }


}
